package ru.tggc.telegrambotspringbootstarter

import com.github.benmanes.caffeine.cache.Cache
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.Chat
import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.model.User
import com.pengrad.telegrambot.request.AnswerCallbackQuery
import com.pengrad.telegrambot.request.DeleteMessage
import com.pengrad.telegrambot.request.SendMessage
import com.pengrad.telegrambot.response.BaseResponse
import com.pengrad.telegrambot.response.SendResponse
import com.pengrad.telegrambot.utility.BotUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.timeout
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import ru.tggc.telegrambotcore.annotation.handle.BotHandler
import ru.tggc.telegrambotcore.annotation.handle.CallbackHandle
import ru.tggc.telegrambotcore.annotation.params.Ctx
import ru.tggc.telegrambotcore.dto.Response
import ru.tggc.telegrambotcore.dto.UpdateContext
import ru.tggc.telegrambotcore.exception.ExceptionHandler
import ru.tggc.telegrambotcore.router.TelegramUpdateRouter
import ru.tggc.telegrambotcore.service.UserRateLimiterService
import ru.tggc.telegrambotspringbootstarter.autoconfigure.TelegramBotAutoConfiguration
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class AsyncBotTests {
    private fun bot(): TelegramBot {
        val bot = mock(TelegramBot::class.java)
        val sent = mock(SendResponse::class.java)
        val message = mock(Message::class.java)
        `when`(message.messageId()).thenReturn(7)
        `when`(sent.message()).thenReturn(message)
        `when`(sent.isOk).thenReturn(true)
        `when`(bot.execute(any(SendMessage::class.java))).thenReturn(sent)
        val ok = mock(BaseResponse::class.java)
        `when`(ok.isOk).thenReturn(true)
        `when`(bot.execute(any(DeleteMessage::class.java))).thenReturn(ok)
        `when`(bot.execute(any(AnswerCallbackQuery::class.java))).thenReturn(ok)
        return bot
    }

    private fun runner(bot: TelegramBot) = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(TelegramBotAutoConfiguration::class.java))
        .withBean(TelegramBot::class.java, { bot })
        .withUserConfiguration(Handler::class.java)

    private fun click(id: Int) = BotUtils.parseUpdate(
        """
        {"update_id":$id,"callback_query":{"id":"callback-$id","chat_instance":"test","data":"load",
        "from":{"id":42,"is_bot":false,"first_name":"Alex"},
        "message":{"message_id":1,"date":1,"chat":{"id":42,"type":"private","first_name":"Alex"}}}}
    """.trimIndent()
    )

    @Test
    fun `callback is acknowledged and concurrent responses start one service request`() {
        val bot = bot()
        runner(bot).run { context ->
            assertThat(context).hasNotFailed()
            val handler = context.getBean(Handler::class.java)
            val router = context.getBean(TelegramUpdateRouter::class.java)
            val limiter = context.getBean(UserRateLimiterService::class.java)
            // Both updates passed routing before either response began executing.
            val first = router.route(click(1))!!
            val second = router.route(click(2))!!
            val completion = first.accept(bot)
            assertThat(handler.started.await(3, TimeUnit.SECONDS)).isTrue()
            second.accept(bot).get(3, TimeUnit.SECONDS)
            assertThat(handler.calls.get()).isEqualTo(1)
            assertThat(limiter.isLocked(42)).isTrue()
            // Simulate expiry/eviction of the legacy 10-second cache without sleeping.
            val field = UserRateLimiterService::class.java.getDeclaredField("lockCache").apply { isAccessible = true }
            (field.get(limiter) as Cache<*, *>).invalidateAll()
            assertThat(limiter.isLocked(42)).isTrue()
            router.route(click(3))!!.accept(bot).get(3, TimeUnit.SECONDS)
            assertThat(handler.calls.get()).isEqualTo(1)
            verify(bot, timeout(3000).times(3)).execute(any(AnswerCallbackQuery::class.java))
            handler.pending.complete("Готово")
            completion.get(3, TimeUnit.SECONDS)
            assertThat(limiter.isLocked(42)).isFalse()
            val sends = ArgumentCaptor.forClass(SendMessage::class.java)
            verify(bot, times(2)).execute(sends.capture())
            assertThat(sends.allValues.map { it.parameters["text"] }).containsExactly("Загружаю", "Готово")
            verify(bot).execute(any(DeleteMessage::class.java))
        }
    }

    @Test
    fun `future failure uses the existing exception handler and releases lock`() {
        val bot = bot()
        val observed = AtomicReference<Exception>()
        val errors = object : ExceptionHandler {
            override fun handleException(e: Exception, chat: Chat, from: User): Response {
                observed.set(e)
                return Response.empty()
            }

            override fun buildMessageToAdmin(s: String, chat: Chat, from: User): String = s
        }
        runner(bot).withBean(ExceptionHandler::class.java, { errors }).run { context ->
            val handler = context.getBean(Handler::class.java)
            val completion = context.getBean(TelegramUpdateRouter::class.java).route(click(1))!!.accept(bot)
            assertThat(handler.started.await(3, TimeUnit.SECONDS)).isTrue()
            handler.pending.completeExceptionally(IllegalStateException("server failed"))
            completion.get(3, TimeUnit.SECONDS)
            assertThat(observed.get()).isInstanceOf(IllegalStateException::class.java).hasMessage("server failed")
            assertThat(context.getBean(UserRateLimiterService::class.java).isLocked(42)).isFalse()
            verify(bot).execute(any(DeleteMessage::class.java))
        }
    }

    @Test
    fun `unmatched typed error reaches application handler and releases lock`() {
        val bot = bot()
        val observed = AtomicReference<Exception>()
        val errors = object : ExceptionHandler {
            override fun handleException(e: Exception, chat: Chat, from: User): Response {
                observed.set(e)
                return Response.empty()
            }

            override fun buildMessageToAdmin(s: String, chat: Chat, from: User): String = s
        }
        runner(bot).withBean(ExceptionHandler::class.java, { errors }).run { context ->
            val handler = context.getBean(Handler::class.java)
            handler.typedErrorsOnly = true
            val completion = context.getBean(TelegramUpdateRouter::class.java).route(click(1))!!.accept(bot)
            assertThat(handler.started.await(3, TimeUnit.SECONDS)).isTrue()
            handler.pending.completeExceptionally(IllegalStateException("Unhandled service failure"))
            completion.get(3, TimeUnit.SECONDS)
            assertThat(observed.get()).isInstanceOf(IllegalStateException::class.java)
                .hasMessage("Unhandled service failure")
            assertThat(context.getBean(UserRateLimiterService::class.java).isLocked(42)).isFalse()
            verify(bot).execute(any(DeleteMessage::class.java))
            verify(bot).execute(any(SendMessage::class.java))
        }
    }

    @Test
    fun `timeout releases lock and next request can finish without a stale response`() {
        val bot = bot()
        runner(bot).run { context ->
            val handler = context.getBean(Handler::class.java)
            handler.waitTimeout = Duration.ofMillis(100)
            handler.handleErrors = true
            val router = context.getBean(TelegramUpdateRouter::class.java)
            val late = handler.pending
            router.route(click(1))!!.accept(bot).get(3, TimeUnit.SECONDS)
            assertThat(context.getBean(UserRateLimiterService::class.java).isLocked(42)).isFalse()
            handler.pending = CompletableFuture.completedFuture("Новый результат")
            router.route(click(2))!!.accept(bot).get(3, TimeUnit.SECONDS)
            late.complete("Старый результат")
            assertThat(handler.successes.get()).isEqualTo(1)
            assertThat(handler.calls.get()).isEqualTo(2)
            val sends = ArgumentCaptor.forClass(SendMessage::class.java)
            verify(bot, times(4)).execute(sends.capture())
            assertThat(sends.allValues.map { it.parameters["text"] })
                .containsExactly("Загружаю", "Ошибка", "Загружаю", "Новый результат")
        }
    }

    @Test
    fun `cancelling tracked response releases lock and removes loader`() {
        val bot = bot()
        runner(bot).run { context ->
            val handler = context.getBean(Handler::class.java)
            val completion = context.getBean(TelegramUpdateRouter::class.java).route(click(1))!!.accept(bot)
            assertThat(handler.started.await(3, TimeUnit.SECONDS)).isTrue()
            completion.cancel(true)
            verify(bot, timeout(3000)).execute(any(DeleteMessage::class.java))
            assertThat(context.getBean(UserRateLimiterService::class.java).isLocked(42)).isFalse()
            handler.pending.complete("Поздно")
            assertThat(handler.successes.get()).isEqualTo(0)
        }
    }

    @BotHandler
    class Handler {
        var pending = CompletableFuture<String>()
        val started = CountDownLatch(1)
        val calls = AtomicInteger()
        val successes = AtomicInteger()
        var waitTimeout: Duration = Duration.ofSeconds(30)
        var handleErrors = false
        var typedErrorsOnly = false

        @CallbackHandle(value = "load", canPrivate = true)
        fun load(@Ctx ctx: UpdateContext): Response {
            val response = ctx.await {
                calls.incrementAndGet()
                started.countDown()
                pending
            }.loading("Загружаю").timeout(waitTimeout).onSuccess {
                successes.incrementAndGet()
                ctx.send(it)
            }
            return when {
                typedErrorsOnly -> response.onError(IllegalArgumentException::class.java) { ctx.send("Некорректный запрос") }
                handleErrors -> response.onError { ctx.send("Ошибка") }
                else -> response
            }
        }
    }
}
