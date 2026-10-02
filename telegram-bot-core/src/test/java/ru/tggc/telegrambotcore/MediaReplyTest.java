package ru.tggc.telegrambotcore;

import com.google.gson.Gson;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.SendResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.tggc.telegrambotcore.dto.*;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class MediaReplyTest {
    private final UpdateContext ctx = new UpdateContext(42L, 99L, 7);
    private final InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup(
        new InlineKeyboardButton("Бой").callbackData("fight"));
    private TelegramBot bot;

    @BeforeEach
    void setUp() {
        bot = mock(TelegramBot.class);
        var sent = mock(SendResponse.class);
        var edited = mock(BaseResponse.class);
        when(sent.isOk()).thenReturn(true);
        when(edited.isOk()).thenReturn(true);
        when(bot.execute(any(SendPhoto.class))).thenReturn(sent);
        when(bot.execute(any(EditMessageText.class))).thenReturn(edited);
        when(bot.execute(any(EditMessageCaption.class))).thenReturn(edited);
        when(bot.execute(any(EditMessageMedia.class))).thenReturn(edited);
    }

    private void execute(Response response) throws Exception {
        response.accept(bot).get(3, TimeUnit.SECONDS);
    }

    private String callback(BaseRequest<?, ?> request) {
        return ((InlineKeyboardMarkup) request.getParameters().get("reply_markup"))
            .inlineKeyboard()[0][0].callbackData();
    }

    private void assertPhoto(EditMessageMedia request, String url, String caption) {
        var media = new Gson().toJsonTree(request.getParameters().get("media")).getAsJsonObject();
        assertThat(media.get("type").getAsString()).isEqualTo("photo");
        assertThat(media.get("media").getAsString()).isEqualTo(url);
        assertThat(media.get("parse_mode").getAsString()).isEqualTo("HTML");
        if (caption == null) {
            assertThat(media.has("caption") && !media.get("caption").isJsonNull()).isFalse();
        } else {
            assertThat(media.get("caption").getAsString()).isEqualTo(caption);
        }
    }

    @Test
    void legacyEditStillEditsCaptionAndExplicitTextEditUsesTextRequest() throws Exception {
        Response caption = ctx.edit("<b>Подпись</b>").keyboard(keyboard).build();
        Response text = ctx.editText("<b>Текст</b>").keyboard(keyboard);
        verifyNoInteractions(bot);
        execute(caption);
        execute(text);
        var captions = ArgumentCaptor.forClass(EditMessageCaption.class);
        var texts = ArgumentCaptor.forClass(EditMessageText.class);
        verify(bot).execute(captions.capture());
        verify(bot).execute(texts.capture());
        assertThat(captions.getValue().getParameters()).containsEntry("caption", "<b>Подпись</b>")
            .containsEntry("chat_id", 42L).containsEntry("message_id", 7);
        assertThat(texts.getValue().getParameters()).containsEntry("text", "<b>Текст</b>")
            .containsEntry("chat_id", 42L).containsEntry("message_id", 7);
        assertThat(texts.getValue().getParameters().get("parse_mode").toString()).isEqualTo("HTML");
        assertThat(callback(captions.getValue())).isEqualTo("fight#u:99");
        assertThat(callback(texts.getValue())).isEqualTo("fight#u:99");
        assertThat(keyboard.inlineKeyboard()[0][0].callbackData()).isEqualTo("fight");
    }

    @Test
    void sendPhotoUsesCurrentChatAndIndependentBranches() throws Exception {
        var base = ctx.sendPhoto("file-id");
        var first = base.caption("<b>Бой</b>").keyboard(keyboard);
        var second = base.keyboard(keyboard).caption("Другая подпись").keyboard(null);
        verifyNoInteractions(bot);
        execute(first.build());
        execute(second);
        execute(base);
        var requests = ArgumentCaptor.forClass(SendPhoto.class);
        verify(bot, times(3)).execute(requests.capture());
        assertThat(requests.getAllValues().get(0).getParameters()).containsEntry("chat_id", 42L)
            .containsEntry("photo", "file-id").containsEntry("caption", "<b>Бой</b>");
        assertThat(callback(requests.getAllValues().get(0))).isEqualTo("fight#u:99");
        assertThat(requests.getAllValues().get(1).getParameters()).containsEntry("caption", "Другая подпись")
            .doesNotContainKey("reply_markup");
        assertThat(requests.getAllValues().get(2).getParameters()).doesNotContainKeys("caption", "reply_markup");
    }

    @Test
    void editPhotoReplacesMediaWithCaptionAndKeyboardWithoutSendingNewMessage() throws Exception {
        var base = ctx.editPhoto("https://example.com/photo.jpg").caption("<b>Новая подпись</b>");
        var configured = base.keyboard(keyboard);
        verifyNoInteractions(bot);
        execute(configured);
        execute(base.caption(null).build());
        var requests = ArgumentCaptor.forClass(EditMessageMedia.class);
        verify(bot, times(2)).execute(requests.capture());
        var first = requests.getAllValues().getFirst();
        assertThat(first.getParameters()).containsEntry("chat_id", 42L).containsEntry("message_id", 7);
        assertPhoto(first, "https://example.com/photo.jpg", "<b>Новая подпись</b>");
        assertThat(callback(first)).isEqualTo("fight#u:99");
        assertPhoto(requests.getAllValues().get(1), "https://example.com/photo.jpg", null);
        assertThat(requests.getAllValues().get(1).getParameters()).doesNotContainKey("reply_markup");
        verify(bot, never()).execute(any(SendPhoto.class));
        verify(bot, never()).execute(any(DeleteMessage.class));
    }

    @Test
    void dtoRepliesKeepDestinationAndAllowPhotosWithoutCaption() throws Exception {
        var photo = new PhotoDto("file-id", null, 123L, keyboard);
        execute(ctx.send(photo).caption("Подпись"));
        execute(ctx.edit(photo).keyboard(keyboard));
        var sent = ArgumentCaptor.forClass(SendPhoto.class);
        var edited = ArgumentCaptor.forClass(EditMessageMedia.class);
        verify(bot).execute(sent.capture());
        verify(bot).execute(edited.capture());
        assertThat(sent.getValue().getParameters()).containsEntry("chat_id", 123L).containsEntry("caption", "Подпись");
        assertThat(edited.getValue().getParameters()).containsEntry("chat_id", 123L).containsEntry("message_id", 7);
        assertPhoto(edited.getValue(), "file-id", null);
        assertThat(callback(sent.getValue())).isEqualTo("fight#u:99");
        assertThat(callback(edited.getValue())).isEqualTo("fight#u:99");
        assertThat(photo.caption()).isNull();
        assertThat(Objects.requireNonNull(photo.markup()).inlineKeyboard()[0][0].callbackData()).isEqualTo("fight");
    }

    @Test
    void captionBranchesAndLegacyExplicitTargetsRemainIndependent() throws Exception {
        var base = ctx.editCaption("Подпись");
        execute(base.keyboard(keyboard));
        execute(base);
        execute(ctx.edit("Подпись", keyboard, 123L, 456));
        execute(ctx.edit("file-id", "Подпись", keyboard, 123L, 456));
        var captions = ArgumentCaptor.forClass(EditMessageCaption.class);
        verify(bot, times(3)).execute(captions.capture());
        assertThat(callback(captions.getAllValues().get(0))).isEqualTo("fight#u:99");
        assertThat(captions.getAllValues().get(1).getParameters()).doesNotContainKey("reply_markup");
        assertThat(captions.getAllValues().get(2).getParameters()).containsEntry("chat_id", 123L)
            .containsEntry("message_id", 456);
        var media = ArgumentCaptor.forClass(EditMessageMedia.class);
        verify(bot).execute(media.capture());
        assertThat(media.getValue().getParameters()).containsEntry("chat_id", 123L).containsEntry("message_id", 456);
    }

    @Test
    void oldJavaJvmDescriptorsStillLink() throws Throwable {
        var lookup = MethodHandles.publicLookup();
        Class<?>[][] captionParameters = {
            {String.class}, {String.class, InlineKeyboardMarkup.class},
            {String.class, InlineKeyboardMarkup.class, long.class}
        };
        for (var parameters : captionParameters) {
            assertThat(lookup.findVirtual(UpdateContext.class, "edit", MethodType.methodType(Response.class, parameters))).isNotNull();
        }
        Class<?>[][] mediaParameters = {
            {String.class, String.class}, {String.class, String.class, InlineKeyboardMarkup.class},
            {String.class, String.class, InlineKeyboardMarkup.class, long.class}, {PhotoDto.class}
        };
        for (var parameters : mediaParameters) {
            assertThat(lookup.findVirtual(UpdateContext.class, "edit", MethodType.methodType(Response.class, parameters))).isNotNull();
        }
        var send = lookup.findVirtual(UpdateContext.class, "send", MethodType.methodType(Response.class, PhotoDto.class));
        execute((Response) send.invokeExact(ctx, new PhotoDto("file-id", null, 42L, null)));
    }

    @Test
    void oldKotlinDefaultBridgesStillExecuteWithCorrectTargets() throws Throwable {
        var lookup = MethodHandles.publicLookup();
        var caption = lookup.findStatic(UpdateContext.class, "edit$default", MethodType.methodType(Response.class,
            UpdateContext.class, String.class, InlineKeyboardMarkup.class, long.class, int.class, int.class, Object.class));
        execute((Response) caption.invokeExact(ctx, "Подпись", (InlineKeyboardMarkup) null, 0L, 0, 14, (Object) null));
        var media = lookup.findStatic(UpdateContext.class, "edit$default", MethodType.methodType(Response.class,
            UpdateContext.class, String.class, String.class, InlineKeyboardMarkup.class, long.class, int.class, int.class, Object.class));
        execute((Response) media.invokeExact(ctx, "file-id", "Подпись", (InlineKeyboardMarkup) null, 0L, 0, 28, (Object) null));
        var captions = ArgumentCaptor.forClass(EditMessageCaption.class);
        var photos = ArgumentCaptor.forClass(EditMessageMedia.class);
        verify(bot).execute(captions.capture());
        verify(bot).execute(photos.capture());
        assertThat(captions.getValue().getParameters()).containsEntry("chat_id", 42L).containsEntry("message_id", 7);
        assertThat(photos.getValue().getParameters()).containsEntry("chat_id", 42L).containsEntry("message_id", 7);
    }

    @Test
    void telegramFailurePropagatesThroughPhotoReply() {
        when(bot.execute(any(EditMessageMedia.class))).thenThrow(new IllegalStateException("edit failed"));
        assertThatThrownBy(() -> execute(ctx.editPhoto("file-id").caption("Подпись")))
            .hasRootCauseMessage("edit failed");
    }
}
