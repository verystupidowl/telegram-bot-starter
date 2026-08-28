package ru.tggc.telegrambotcore.dto

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.request.BaseRequest
import com.pengrad.telegrambot.response.BaseResponse
import com.pengrad.telegrambot.response.SendResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.future.await
import kotlinx.coroutines.future.future
import ru.tggc.telegrambotcore.ext.executeAsync
import java.util.concurrent.CompletableFuture
import java.util.function.BiConsumer
import java.util.function.Consumer
import java.util.function.Function

/**
 * Базовый интерфейс для отправки сообщений
 */
fun interface Response {
    /**
     * Выполнить несколько действий последовательно
     */
    fun andThen(after: Response): Response =
        Response {
            this.accept(it)
            after.accept(it)
        }

    /**
     * Выполнить action с результатом отправки сообщения
     *
     * ВАЖНО!!! Возможно только с текстовыми сообщениями
     */
    fun then(action: Consumer<SendResponse>): Response =
        Response { bot ->
            accept(bot).thenApply { result ->
                if (result is SendResponse) {
                    action.accept(result)
                }
                result
            }
        }

    fun accept(bot: TelegramBot): CompletableFuture<BaseResponse?>

    companion object {
        private val botResponseScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        @JvmStatic
        fun <Rq, Rs> ofAll(requests: List<Rq>): Response where Rq : BaseRequest<Rq, Rs>, Rs : BaseResponse =
            Response { bot ->
                botResponseScope.future {
                    var result: BaseResponse? = null
                    requests.forEach { request ->
                        result = bot.executeAsync(request)
                    }
                    result
                }
            }

        @JvmStatic
        fun <Rq, Rs> ofAll(vararg requests: Rq): Response where Rq : BaseRequest<Rq, Rs>, Rs : BaseResponse =
            Response { bot ->
                botResponseScope.future {
                    var result: BaseResponse? = null
                    requests.forEach { request ->
                        result = bot.executeAsync(request)
                    }
                    result
                }
            }


        @JvmStatic
        fun <Rq, Rs> of(request: Rq): Response where Rq : BaseRequest<Rq, Rs>, Rs : BaseResponse =
            Response { bot ->
                botResponseScope.future {
                    return@future bot.executeAsync(request)
                }
            }

        @JvmStatic
        fun of(consumer: Consumer<TelegramBot>): Response =
            Response { bot ->
                botResponseScope.future {
                    consumer.accept(bot)
                    return@future null
                }
            }

        @JvmStatic
        fun of(function: Function<TelegramBot, BaseResponse>): Response =
            Response { bot ->
                botResponseScope.future {
                    return@future function.apply(bot)
                }
            }

        @JvmStatic
        fun ofAllConsumers(consumers: List<Consumer<TelegramBot>>): Response =
            Response { bot ->
                botResponseScope.future {
                    consumers.forEach { it.accept(bot) }
                    return@future null
                }
            }

        @JvmStatic
        fun ofAllResponses(responses: List<Response>): Response =
            Response { bot ->
                botResponseScope.future {
                    var result: BaseResponse? = null
                    responses.forEach { response ->
                        result = response.accept(bot).await()
                    }
                    result
                }
            }

        @JvmStatic
        fun <T> of(consumer: BiConsumer<TelegramBot, T?>, request: T?): Response =
            Response {
                botResponseScope.future {
                    consumer.accept(it, request)
                    return@future null
                }
            }

        @JvmStatic
        fun create(block: suspend (TelegramBot) -> BaseResponse): Response =
            Response { bot ->
                botResponseScope.future {
                    return@future block(bot)
                }
            }

        @JvmStatic
        fun empty(): Response = Response { _ -> botResponseScope.future { null } }
    }
}