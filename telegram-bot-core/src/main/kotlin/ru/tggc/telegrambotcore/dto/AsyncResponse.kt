package ru.tggc.telegrambotcore.dto

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.request.DeleteMessage
import com.pengrad.telegrambot.request.SendMessage
import com.pengrad.telegrambot.response.BaseResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.future.await
import kotlinx.coroutines.future.future
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import ru.tggc.telegrambotcore.ext.executeAsync
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeoutException
import java.util.function.Function
import java.util.function.Supplier

/**
 * Lazy asynchronous work with optional progress and error responses.
 * Configuration is immutable. The timeout covers the service call, not Telegram requests.
 */
class AsyncResponse<T> private constructor(
    private val chatId: Long,
    private val task: Supplier<out CompletableFuture<T>>,
    private val loadingText: String?,
    private val waitTimeout: Duration,
    private val success: Function<in T, out Response>?,
    private val failure: Function<in Throwable, out Response>?,
    private val typedFailures: Map<Class<out Throwable>, Function<Throwable, Response>>
) : CompletionAwareResponse {
    internal constructor(
        chatId: Long,
        task: Supplier<out CompletableFuture<T>>,
        loadingText: String? = null,
        waitTimeout: Duration = Duration.ofSeconds(30),
        success: Function<in T, out Response>? = null,
        failure: Function<in Throwable, out Response>? = null
    ) : this(chatId, task, loadingText, waitTimeout, success, failure, emptyMap())

    fun loading(text: String): AsyncResponse<T> {
        require(text.isNotBlank()) { "Loading text must not be blank." }
        return AsyncResponse(chatId, task, text, waitTimeout, success, failure, typedFailures)
    }

    fun timeout(duration: Duration): AsyncResponse<T> {
        require(duration.toMillis() > 0) { "Async timeout must be at least 1 millisecond." }
        return AsyncResponse(chatId, task, loadingText, duration, success, failure, typedFailures)
    }

    fun onSuccess(action: Function<in T, out Response>): AsyncResponse<T> =
        AsyncResponse(chatId, task, loadingText, waitTimeout, action, failure, typedFailures)

    /** Fallback used only when no typed error handler matches. */
    fun onError(action: Function<in Throwable, out Response>): AsyncResponse<T> =
        AsyncResponse(chatId, task, loadingText, waitTimeout, success, action, typedFailures)

    /**
     * Handles this error type and its subclasses. The most specific match wins, regardless of order.
     * Registering the same type again replaces its handler in the returned immutable response.
     */
    fun <E : Throwable> onError(type: Class<E>, action: Function<in E, out Response>): AsyncResponse<T> =
        AsyncResponse(
            chatId, task, loadingText, waitTimeout, success, failure,
            typedFailures + (type to Function { error -> action.apply(type.cast(error)) })
        )

    override fun accept(bot: TelegramBot): CompletableFuture<BaseResponse?> = asyncResponseScope.future {
        val render = requireNotNull(success) { "Configure ctx.await(...).onSuccess(...) before sending." }
        var loadingMessageId: Int? = null
        try {
            if (loadingText != null) {
                try {
                    withContext(NonCancellable) {
                        loadingMessageId = bot.executeAsync(SendMessage(chatId, loadingText)).message()?.messageId()
                    }
                } catch (e: Exception) {
                    currentCoroutineContext().ensureActive()
                    log.warn(e) { "Could not display loading message for chat $chatId" }
                }
            }
            currentCoroutineContext().ensureActive()
            val response = try {
                val value = try {
                    withTimeout(waitTimeout.toMillis()) {
                        task.get().copy().await()
                    }
                } catch (e: TimeoutCancellationException) {
                    throw TimeoutException("Service did not respond within $waitTimeout").apply { initCause(e) }
                }
                render.apply(value)
            } catch (e: Exception) {
                currentCoroutineContext().ensureActive()
                val cause = unwrap(e)
                handleFailure(cause)
            }
            response.accept(bot).await()
        } finally {
            loadingMessageId?.let { messageId ->
                withContext(NonCancellable) {
                    try {
                        bot.executeAsync(DeleteMessage(chatId, messageId))
                    } catch (e: Exception) {
                        log.warn(e) { "Could not remove loading message $messageId in chat $chatId" }
                    }
                }
            }
        }
    }

    private fun handleFailure(cause: Throwable): Response {
        var selected: Map.Entry<Class<out Throwable>, Function<Throwable, Response>>? = null
        for (handler in typedFailures.entries) {
            if (handler.key.isInstance(cause) &&
                (selected == null || selected.key.isAssignableFrom(handler.key))
            ) {
                selected = handler
            }
        }
        if (selected != null) return selected.value.apply(cause)
        return failure?.apply(cause) ?: throw cause
    }

    private fun unwrap(error: Throwable): Throwable {
        var cause = error
        while ((cause is CompletionException || cause is ExecutionException) && cause.cause != null) {
            cause = cause.cause!!
        }
        return cause
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}
