package ru.tggc.telegrambotcore;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.tggc.telegrambotcore.dto.AskReply;
import ru.tggc.telegrambotcore.dto.DialogSession;
import ru.tggc.telegrambotcore.dto.HistoryKey;
import ru.tggc.telegrambotcore.dto.Response;
import ru.tggc.telegrambotcore.dto.SendReply;
import ru.tggc.telegrambotcore.dto.UpdateContext;
import ru.tggc.telegrambotcore.service.defaults.DefaultHistoryService;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class FluentReplyTest {
    private final UpdateContext ctx = new UpdateContext(42L, 99L, 1);
    private final InlineKeyboardMarkup cancel = keyboard("cancel");
    private TelegramBot bot;
    private DefaultHistoryService history;

    enum State implements HistoryKey {
        BET, EXISTING;

        @NotNull
        public String getLabel() {
            return name();
        }
    }

    @BeforeEach
    void setUp() {
        history = new DefaultHistoryService();
        history.removeFromHistory(ctx);
        bot = mock(TelegramBot.class);
        var sent = mock(SendResponse.class);
        var message = mock(Message.class);
        when(message.messageId()).thenReturn(123);
        when(sent.message()).thenReturn(message);
        when(sent.isOk()).thenReturn(true);
        when(bot.execute(any(SendMessage.class))).thenReturn(sent);
    }

    @AfterEach
    void clearHistory() {
        history.removeFromHistory(ctx);
    }

    private static InlineKeyboardMarkup keyboard(String data) {
        return new InlineKeyboardMarkup(new InlineKeyboardButton("Кнопка").callbackData(data));
    }

    private static String callbackData(SendMessage message) {
        return ((InlineKeyboardMarkup) message.getParameters().get("reply_markup"))
                .inlineKeyboard()[0][0].callbackData();
    }

    @Test
    void sendChainNeedsNoBuildAndMatchesLegacyMessage() throws Exception {
        Response fluent = ctx.send("<b>Бой начался</b>").keyboard(cancel);
        verifyNoInteractions(bot);
        fluent.accept(bot).get(3, TimeUnit.SECONDS);
        ctx.send("<b>Бой начался</b>", cancel, 42L).accept(bot).get(3, TimeUnit.SECONDS);
        var requests = ArgumentCaptor.forClass(SendMessage.class);
        verify(bot, times(2)).execute(requests.capture());
        var first = requests.getAllValues().get(0);
        var second = requests.getAllValues().get(1);
        assertThat(first.getParameters().get("chat_id")).isEqualTo(42L);
        assertThat(first.getParameters().get("text")).isEqualTo("<b>Бой начался</b>");
        assertThat(first.getParameters().get("parse_mode")).isNotNull().isEqualTo(second.getParameters().get("parse_mode"));
        assertThat(callbackData(first)).isEqualTo("cancel#u:99").isEqualTo(callbackData(second));
        assertThat(cancel.inlineKeyboard()[0][0].callbackData()).isEqualTo("cancel");
    }

    @Test
    void askChainSavesPromptAfterSendingAndBuildHasNoSideEffects() throws Exception {
        var fallbackCalls = new AtomicInteger();
        Response prompt = ctx.ask("Введите ставку", State.BET)
                .keyboard(cancel).fallback(session -> fallbackCalls.incrementAndGet()).build();
        verifyNoInteractions(bot);
        assertThat(history.contains(ctx)).isFalse();
        prompt.accept(bot).get(3, TimeUnit.SECONDS);
        assertThat(history.getFromHistory(ctx)).isEqualTo(State.BET);
        assertThat(history.getPromptMessageId(ctx)).isEqualTo(123);
        assertThat(fallbackCalls).hasValue(0);
        var request = ArgumentCaptor.forClass(SendMessage.class);
        verify(bot).execute(request.capture());
        assertThat(callbackData(request.getValue())).isEqualTo("cancel#u:99");
    }

    @Test
    void fallbackReceivesExistingSessionAndDoesNotReplaceIt() throws Exception {
        history.setHistory(ctx, State.EXISTING, 77, session -> {
        });
        var existing = history.getSession(ctx);
        var observed = new AtomicReference<DialogSession>();
        Response prompt = ctx.ask("Введите ставку", State.BET)
                .fallback(observed::set).keyboard(cancel);
        assertThat(observed.get()).isNull();
        prompt.accept(bot).get(3, TimeUnit.SECONDS);
        assertThat(observed.get()).isSameAs(existing);
        assertThat(history.getFromHistory(ctx)).isEqualTo(State.EXISTING);
        assertThat(history.getPromptMessageId(ctx)).isEqualTo(77);
    }

    @Test
    void sendFailureDoesNotStartDialogOrCallConflictFallback() {
        when(bot.execute(any(SendMessage.class))).thenThrow(new IllegalStateException("send failed"));
        var fallbackCalls = new AtomicInteger();
        var pending = ctx.ask("Введите ставку", State.BET)
                .fallback(session -> fallbackCalls.incrementAndGet()).accept(bot);
        assertThatThrownBy(() -> pending.get(3, TimeUnit.SECONDS)).hasRootCauseMessage("send failed");
        assertThat(history.contains(ctx)).isFalse();
        assertThat(fallbackCalls).hasValue(0);
    }

    @Test
    void sendBranchesKeepTheirOwnKeyboardsAndBuildIsOptional() throws Exception {
        var base = ctx.send("Выберите действие");
        var withCancel = base.keyboard(cancel);
        var withFight = base.keyboard(keyboard("fight"));
        withCancel.build().accept(bot).get(3, TimeUnit.SECONDS);
        withFight.accept(bot).get(3, TimeUnit.SECONDS);
        base.accept(bot).get(3, TimeUnit.SECONDS);
        var requests = ArgumentCaptor.forClass(SendMessage.class);
        verify(bot, times(3)).execute(requests.capture());
        assertThat(callbackData(requests.getAllValues().get(0))).isEqualTo("cancel#u:99");
        assertThat(callbackData(requests.getAllValues().get(1))).isEqualTo("fight#u:99");
        assertThat(requests.getAllValues().get(2).getParameters()).doesNotContainKey("reply_markup");
    }

    @Test
    void askBranchesKeepIndependentFallbacksAndLegacyArgumentsStillWork() throws Exception {
        history.setHistory(ctx, State.EXISTING, 77, session -> {
        });
        var first = new AtomicInteger();
        var second = new AtomicInteger();
        var base = ctx.ask("Введите ставку", State.BET, cancel);
        base.fallback(session -> first.incrementAndGet()).accept(bot).get(3, TimeUnit.SECONDS);
        base.fallback(session -> second.incrementAndGet()).accept(bot).get(3, TimeUnit.SECONDS);
        base.accept(bot).get(3, TimeUnit.SECONDS);
        ctx.ask("Введите ставку", State.BET, cancel, session -> first.incrementAndGet()).accept(bot).get(3, TimeUnit.SECONDS);
        assertThat(first).hasValue(2);
        assertThat(second).hasValue(1);
        assertThat(history.getPromptMessageId(ctx)).isEqualTo(77);
    }

    @Test
    void originalJvmDescriptorsStillLinkForAlreadyCompiledJavaCallers() throws Throwable {
        var lookup = MethodHandles.publicLookup();
        var send = lookup.findVirtual(UpdateContext.class, "send", MethodType.methodType(Response.class, String.class));
        var sendKeyboard = lookup.findVirtual(UpdateContext.class, "send",
                MethodType.methodType(Response.class, String.class, InlineKeyboardMarkup.class));
        var ask = lookup.findVirtual(UpdateContext.class, "ask",
                MethodType.methodType(Response.class, String.class, HistoryKey.class));
        var askKeyboard = lookup.findVirtual(UpdateContext.class, "ask",
                MethodType.methodType(Response.class, String.class, HistoryKey.class, InlineKeyboardMarkup.class));
        assertThat((Response) send.invokeExact(ctx, "Привет")).isInstanceOf(SendReply.class);
        ((Response) sendKeyboard.invokeExact(ctx, "Привет", cancel)).accept(bot).get(3, TimeUnit.SECONDS);
        assertThat((Response) ask.invokeExact(ctx, "Ставка", (HistoryKey) State.BET)).isInstanceOf(AskReply.class);
        ((Response) askKeyboard.invokeExact(ctx, "Ставка", (HistoryKey) State.BET, cancel)).accept(bot).get(3, TimeUnit.SECONDS);
        assertThat(history.getFromHistory(ctx)).isEqualTo(State.BET);
    }

    @Test
    void originalKotlinDefaultArgumentDescriptorsStillExecute() throws Throwable {
        var lookup = MethodHandles.publicLookup();
        var sendDefault = lookup.findStatic(UpdateContext.class, "send$default",
                MethodType.methodType(Response.class, UpdateContext.class, String.class, InlineKeyboardMarkup.class,
                        long.class, int.class, Object.class));
        Response reply = (Response) sendDefault.invokeExact(ctx, "Привет", (InlineKeyboardMarkup) null, 0L, 6, (Object) null);
        reply.accept(bot).get(3, TimeUnit.SECONDS);
        var askDefault = lookup.findStatic(UpdateContext.class, "ask$default",
                MethodType.methodType(Response.class, UpdateContext.class, String.class, HistoryKey.class,
                        InlineKeyboardMarkup.class, Consumer.class, int.class, Object.class));
        Response prompt = (Response) askDefault.invokeExact(ctx, "Ставка", (HistoryKey) State.BET,
                (InlineKeyboardMarkup) null, (Consumer<DialogSession>) null, 12, (Object) null);
        prompt.accept(bot).get(3, TimeUnit.SECONDS);
        assertThat(history.getFromHistory(ctx)).isEqualTo(State.BET);
    }
}
