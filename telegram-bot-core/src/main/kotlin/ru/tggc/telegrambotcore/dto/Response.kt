package ru.tggc.telegrambotcore.dto

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.request.BaseRequest
import com.pengrad.telegrambot.response.BaseResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.future.await
import kotlinx.coroutines.future.future
import ru.tggc.telegrambotcore.ext.executeAsync
import java.util.concurrent.CompletableFuture
import java.util.function.BiConsumer
import java.util.function.Consumer

fun interface Response {
    fun andThen(after: Response): Response =
        Response {
            this.accept(it)
            after.accept(it)
        }

    fun accept(bot: TelegramBot): CompletableFuture<Void?>

    suspend fun acceptAsync(bot: TelegramBot) {
        accept(bot).await()
    }

    companion object {
        private val botResponseScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        @JvmStatic
        fun <Rq, Rs> ofAll(requests: List<Rq>): Response where Rq : BaseRequest<Rq, Rs>, Rs : BaseResponse =
            Response { bot ->
                botResponseScope.future {
                    requests.forEach { bot.executeAsync(it) }
                    return@future null
                }
            }

        @JvmStatic
        fun <Rq, Rs> ofAll(vararg requests: Rq): Response where Rq : BaseRequest<Rq, Rs>, Rs : BaseResponse =
            Response { bot ->
                botResponseScope.future {
                    requests.forEach { bot.executeAsync(it) }
                    return@future null
                }
            }


        @JvmStatic
        fun <Rq, Rs> of(request: Rq): Response where Rq : BaseRequest<Rq, Rs>, Rs : BaseResponse =
            Response { bot ->
                botResponseScope.future {
                    bot.executeAsync(request)
                    return@future null
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
        fun ofAllConsumers(consumers: List<Consumer<TelegramBot>>): Response =
            Response { bot ->
                botResponseScope.future {
                    consumers.forEach { it.accept(bot) }
                    return@future null
                }
            }

        @JvmStatic
        fun ofAllResponses(responses: List<Response>): Response =
            Response { bot: TelegramBot ->
                botResponseScope.future {
                    responses.forEach { it.accept(bot) }
                    return@future null
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
        fun create(block: suspend (TelegramBot) -> Unit): Response =
            Response { bot ->
                botResponseScope.future {
                    block(bot)
                    return@future null
                }
            }

        @JvmStatic
        fun empty(): Response = Response { _ -> botResponseScope.future { null } }
    }
}