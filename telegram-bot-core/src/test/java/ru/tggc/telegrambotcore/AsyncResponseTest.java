package ru.tggc.telegrambotcore;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.SendResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.tggc.telegrambotcore.dto.CompletionAwareResponse;
import ru.tggc.telegrambotcore.dto.Response;
import ru.tggc.telegrambotcore.dto.ResponseBuilder;
import ru.tggc.telegrambotcore.dto.UpdateContext;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AsyncResponseTest {
    private final UpdateContext ctx = new UpdateContext(42L, 42L, 1);
    private TelegramBot bot;
    private SendResponse sent;
    private CompletableFuture<String> service;
    private CountDownLatch started;

    @BeforeEach
    void setUp() {
        bot = mock(TelegramBot.class);
        sent = mock(SendResponse.class);
        Message message = mock(Message.class);
        when(message.messageId()).thenReturn(7);
        when(sent.isOk()).thenReturn(true);
        when(sent.message()).thenReturn(message);
        when(bot.execute(any(SendMessage.class))).thenReturn(sent);
        BaseResponse deleted = mock(BaseResponse.class);
        when(deleted.isOk()).thenReturn(true);
        when(bot.execute(any(DeleteMessage.class))).thenReturn(deleted);
        service = new CompletableFuture<>();
        started = new CountDownLatch(1);
    }

    private CompletableFuture<String> request() {
        started.countDown();
        return service;
    }

    @Test
    void javaApiIsLazyShowsLoadingThenSendsResultAndCleansUp() throws Exception {
        Response response = ctx.await(this::request)
                .loading("Загружаю…")
                .onSuccess(ctx::send);
        assertThat(started.getCount()).isEqualTo(1);
        CompletableFuture<BaseResponse> completion = response.accept(bot);
        assertThat(started.await(3, TimeUnit.SECONDS)).isTrue();
        assertThat(completion).isNotDone();
        verify(bot).execute(any(SendMessage.class));
        service.complete("Готово");
        assertThat(completion.get(3, TimeUnit.SECONDS)).isSameAs(sent);
        var requests = ArgumentCaptor.forClass(SendMessage.class);
        verify(bot, times(2)).execute(requests.capture());
        assertThat(requests.getAllValues()).extracting(r -> r.getParameters().get("text"))
                .containsExactly("Загружаю…", "Готово");
        var deletion = ArgumentCaptor.forClass(DeleteMessage.class);
        verify(bot).execute(deletion.capture());
        assertThat(deletion.getValue().getParameters()).containsEntry("chat_id", 42L).containsEntry("message_id", 7);
    }

    @Test
    void serviceFailureReachesErrorHandlerAndRemovesLoader() throws Exception {
        var error = new IllegalStateException("server failed");
        AtomicReference<Throwable> observed = new AtomicReference<>();
        var completion = ctx.await(this::request).loading("Ждите")
                .onSuccess(ctx::send)
                .onError(e -> {
                    observed.set(e);
                    return ctx.send("Ошибка");
                })
                .accept(bot);
        assertThat(started.await(3, TimeUnit.SECONDS)).isTrue();
        service.completeExceptionally(error);
        completion.get(3, TimeUnit.SECONDS);
        assertThat(observed.get()).isInstanceOf(IllegalStateException.class).hasMessage("server failed");
        verify(bot).execute(any(DeleteMessage.class));
    }

    @Test
    void timeoutSendsOneErrorWithoutMutatingTheServiceFutureOrSendingLateResult() throws Exception {
        AtomicReference<Throwable> error = new AtomicReference<>();
        AtomicInteger success = new AtomicInteger();
        var completion = ctx.await(this::request).loading("Ждите")
                .timeout(Duration.ofMillis(100))
                .onSuccess(value -> {
                    success.incrementAndGet();
                    return ctx.send(value);
                })
                .onError(e -> {
                    error.set(e);
                    return ctx.send("Время вышло");
                })
                .accept(bot);
        completion.get(3, TimeUnit.SECONDS);
        assertThat(error.get()).isInstanceOf(TimeoutException.class);
        assertThat(service).isNotDone();
        service.complete("Поздний результат");
        assertThat(success).hasValue(0);
        verify(bot, times(2)).execute(any(SendMessage.class));
        verify(bot).execute(any(DeleteMessage.class));
    }

    @Test
    void synchronousServiceExceptionIsHandledToo() throws Exception {
        var response = ctx.<String>await(() -> {
                    throw new IllegalStateException("before future");
                })
                .onSuccess(ctx::send).onError(e -> ctx.send(e.getMessage()));
        response.accept(bot).get(3, TimeUnit.SECONDS);
        verify(bot).execute(any(SendMessage.class));
        verify(bot, never()).execute(any(DeleteMessage.class));
    }

    @Test
    void cancellationCleansUpAndDoesNotCancelSharedServiceFuture() throws Exception {
        AtomicInteger success = new AtomicInteger();
        var completion = ctx.await(this::request).loading("Ждите")
                .onSuccess(value -> {
                    success.incrementAndGet();
                    return ctx.send(value);
                }).accept(bot);
        assertThat(started.await(3, TimeUnit.SECONDS)).isTrue();
        assertThat(completion.cancel(true)).isTrue();
        verify(bot, timeout(3000)).execute(any(DeleteMessage.class));
        assertThat(service).isNotDone();
        service.complete("Поздний результат");
        assertThat(success).hasValue(0);
        verify(bot).execute(any(SendMessage.class));
    }

    @Test
    void failedLoadingDoesNotPreventTheServiceRequest() throws Exception {
        when(bot.execute(any(SendMessage.class))).thenThrow(new IllegalStateException("loader failed")).thenReturn(sent);
        var completion = ctx.await(this::request).loading("Ждите").onSuccess(ctx::send).accept(bot);
        assertThat(started.await(3, TimeUnit.SECONDS)).isTrue();
        service.complete("Готово");
        assertThat(completion.get(3, TimeUnit.SECONDS)).isSameAs(sent);
        verify(bot, times(2)).execute(any(SendMessage.class));
        verify(bot, never()).execute(any(DeleteMessage.class));
    }

    @Test
    void failedCleanupDoesNotReplaceSuccessfulResult() throws Exception {
        when(bot.execute(any(DeleteMessage.class))).thenThrow(new IllegalStateException("delete failed"));
        service.complete("Готово");
        var completion = ctx.await(this::request).loading("Ждите").onSuccess(ctx::send).accept(bot);
        assertThat(completion.get(3, TimeUnit.SECONDS)).isSameAs(sent);
    }

    @Test
    void failedResultSendingStillCleansUpAndDoesNotRunServiceAgain() {
        when(bot.execute(any(SendMessage.class))).thenReturn(sent).thenThrow(new IllegalStateException("send failed"));
        AtomicInteger requests = new AtomicInteger();
        var completion = ctx.await(() -> {
                    requests.incrementAndGet();
                    return CompletableFuture.completedFuture("Готово");
                })
                .loading("Ждите").onSuccess(ctx::send).accept(bot);
        assertThatThrownBy(() -> completion.get(3, TimeUnit.SECONDS)).hasRootCauseMessage("send failed");
        assertThat(requests).hasValue(1);
        verify(bot).execute(any(DeleteMessage.class));
    }

    @Test
    void newResponseAndThenWaitsForTheActualResult() throws Exception {
        AtomicInteger followup = new AtomicInteger();
        Response after = bot -> {
            followup.incrementAndGet();
            return CompletableFuture.completedFuture(null);
        };
        var completion = ctx.await(this::request).onSuccess(ctx::send).andThen(after).accept(bot);
        assertThat(started.await(3, TimeUnit.SECONDS)).isTrue();
        assertThat(followup).hasValue(0);
        service.complete("Готово");
        completion.get(3, TimeUnit.SECONDS);
        assertThat(followup).hasValue(1);
    }

    @Test
    void legacyResponseAndThenKeepsItsExistingBehaviour() {
        var pending = new CompletableFuture<BaseResponse>();
        AtomicInteger followup = new AtomicInteger();
        Response legacy = bot -> pending;
        Response after = bot -> {
            followup.incrementAndGet();
            return CompletableFuture.completedFuture(null);
        };
        assertThat(legacy.andThen(after).accept(bot)).isCompleted();
        assertThat(pending).isNotDone();
        assertThat(followup).hasValue(1);
    }

    @Test
    void asyncResponseKeepsCompletionTrackingInsideExistingCombinators() throws Exception {
        Response async = ctx.await(this::request).onSuccess(ctx::send);
        assertThat(Response.empty().andThen(async)).isInstanceOf(CompletionAwareResponse.class);
        Response combined = ResponseBuilder.to(42L).add(async).build();
        assertThat(combined).isInstanceOf(CompletionAwareResponse.class);
        var completion = combined.accept(bot);
        assertThat(started.await(3, TimeUnit.SECONDS)).isTrue();
        assertThat(completion).isNotDone();
        service.complete("Готово");
        completion.get(3, TimeUnit.SECONDS);
        verify(bot).execute(any(SendMessage.class));
    }

    @Test
    void typedErrorExposesDtoAndUnwrapsNestedFutureExceptions() throws Exception {
        service.completeExceptionally(new CompletionException(new ExecutionException(
                new GeniusResponse(new ErrorDto("Нет доступа к отчёту")))));
        AtomicInteger fallback = new AtomicInteger();
        ctx.await(this::request)
                .onError(e -> {
                    fallback.incrementAndGet();
                    return ctx.send("Общая ошибка");
                })
                .onError(GeniusResponse.class, ex -> ctx.send(ex.getDTO().getMessage()))
                .loading("Загружаю")
                .timeout(Duration.ofSeconds(3))
                .onSuccess(ctx::send)
                .accept(bot).get(3, TimeUnit.SECONDS);
        var sends = ArgumentCaptor.forClass(SendMessage.class);
        verify(bot, times(2)).execute(sends.capture());
        assertThat(sends.getAllValues()).extracting(r -> r.getParameters().get("text"))
                .containsExactly("Загружаю", "Нет доступа к отчёту");
        assertThat(fallback).hasValue(0);
        verify(bot).execute(any(DeleteMessage.class));
    }

    @Test
    void mostSpecificErrorTypeWinsInEitherRegistrationOrderAndIncludesSubclasses() throws Exception {
        service.completeExceptionally(new DetailedGeniusResponse(new ErrorDto("Детали")));
        AtomicInteger specific = new AtomicInteger();
        AtomicInteger broad = new AtomicInteger();
        var base = ctx.await(this::request).onSuccess(ctx::send);
        var specificLast = base
                .onError(Throwable.class, e -> {
                    broad.incrementAndGet();
                    return Response.empty();
                })
                .onError(RuntimeException.class, e -> {
                    broad.incrementAndGet();
                    return Response.empty();
                })
                .onError(GeniusResponse.class, e -> {
                    specific.incrementAndGet();
                    return Response.empty();
                });
        var specificFirst = base
                .onError(GeniusResponse.class, e -> {
                    specific.incrementAndGet();
                    return Response.empty();
                })
                .onError(RuntimeException.class, e -> {
                    broad.incrementAndGet();
                    return Response.empty();
                })
                .onError(Throwable.class, e -> {
                    broad.incrementAndGet();
                    return Response.empty();
                });
        specificLast.accept(bot).get(3, TimeUnit.SECONDS);
        specificFirst.accept(bot).get(3, TimeUnit.SECONDS);
        assertThat(specific).hasValue(2);
        assertThat(broad).hasValue(0);
    }

    @Test
    void unmatchedTypeUsesExistingCatchAll() throws Exception {
        service.completeExceptionally(new IllegalStateException("Другая ошибка"));
        AtomicInteger specific = new AtomicInteger();
        AtomicReference<Throwable> fallback = new AtomicReference<>();
        ctx.await(this::request).onSuccess(ctx::send)
                .onError(GeniusResponse.class, e -> {
                    specific.incrementAndGet();
                    return Response.empty();
                })
                .onError(e -> {
                    fallback.set(e);
                    return Response.empty();
                })
                .accept(bot).get(3, TimeUnit.SECONDS);
        assertThat(specific).hasValue(0);
        assertThat(fallback.get()).isInstanceOf(IllegalStateException.class).hasMessage("Другая ошибка");
    }

    @Test
    void unmatchedTypeWithoutCatchAllPropagatesAndCleansUp() {
        service.completeExceptionally(new IllegalStateException("Не перехватывать"));
        var completion = ctx.await(this::request).loading("Ждите").onSuccess(ctx::send)
                .onError(GeniusResponse.class, e -> ctx.send("Только Genius"))
                .accept(bot);
        assertThatThrownBy(() -> completion.get(3, TimeUnit.SECONDS)).hasRootCauseMessage("Не перехватывать");
        verify(bot).execute(any(SendMessage.class));
        verify(bot).execute(any(DeleteMessage.class));
    }

    @Test
    void timeoutCanBeHandledSeparately() throws Exception {
        AtomicInteger timeout = new AtomicInteger();
        AtomicInteger fallback = new AtomicInteger();
        ctx.await(this::request).timeout(Duration.ofMillis(100)).onSuccess(ctx::send)
                .onError(TimeoutException.class, e -> {
                    timeout.incrementAndGet();
                    return Response.empty();
                })
                .onError(e -> {
                    fallback.incrementAndGet();
                    return Response.empty();
                })
                .accept(bot).get(3, TimeUnit.SECONDS);
        assertThat(timeout).hasValue(1);
        assertThat(fallback).hasValue(0);
        assertThat(service).isNotDone();
    }

    @Test
    void replacingTypedHandlerDoesNotMutateOriginalResponse() throws Exception {
        service.completeExceptionally(new GeniusResponse(new ErrorDto("Ошибка")));
        AtomicInteger original = new AtomicInteger();
        AtomicInteger replacement = new AtomicInteger();
        var base = ctx.await(this::request).onSuccess(ctx::send)
                .onError(GeniusResponse.class, e -> {
                    original.incrementAndGet();
                    return Response.empty();
                });
        var changed = base.onError(GeniusResponse.class, e -> {
            replacement.incrementAndGet();
            return Response.empty();
        });
        changed.accept(bot).get(3, TimeUnit.SECONDS);
        base.accept(bot).get(3, TimeUnit.SECONDS);
        assertThat(original).hasValue(1);
        assertThat(replacement).hasValue(1);
    }

    @Test
    void failureInsideTypedHandlerIsNotFedIntoAnotherLocalErrorHandler() {
        service.completeExceptionally(new GeniusResponse(new ErrorDto("Ошибка")));
        AtomicInteger fallback = new AtomicInteger();
        var completion = ctx.await(this::request).loading("Ждите").onSuccess(ctx::send)
                .onError(GeniusResponse.class, e -> {
                    throw new IllegalArgumentException("Обработчик сломался");
                })
                .onError(e -> {
                    fallback.incrementAndGet();
                    return Response.empty();
                })
                .accept(bot);
        assertThatThrownBy(() -> completion.get(3, TimeUnit.SECONDS)).hasRootCauseMessage("Обработчик сломался");
        assertThat(fallback).hasValue(0);
        verify(bot).execute(any(DeleteMessage.class));
    }

    private static class GeniusResponse extends RuntimeException {
        private final ErrorDto dto;

        GeniusResponse(ErrorDto dto) {
            super(dto.getMessage());
            this.dto = dto;
        }

        public ErrorDto getDTO() {
            return dto;
        }
    }

    private static final class DetailedGeniusResponse extends GeniusResponse {
        DetailedGeniusResponse(ErrorDto dto) {
            super(dto);
        }
    }

    private record ErrorDto(String message) {
        public String getMessage() {
            return message;
        }
    }
}
