package ru.tggc.telegrambotspringbootstarter

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.UpdatesListener
import com.pengrad.telegrambot.request.SendMessage
import com.pengrad.telegrambot.response.SendResponse
import com.pengrad.telegrambot.utility.BotUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.timeout
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.scheduling.TaskScheduler
import ru.tggc.telegrambotcore.annotation.handle.BotAddedHandle
import ru.tggc.telegrambotcore.annotation.handle.BotHandler
import ru.tggc.telegrambotcore.annotation.handle.CommandHandle
import ru.tggc.telegrambotcore.annotation.params.ChatId
import ru.tggc.telegrambotcore.dto.Response
import ru.tggc.telegrambotcore.dto.UserRole
import ru.tggc.telegrambotcore.exception.DefaultExceptionHandler
import ru.tggc.telegrambotcore.exception.ExceptionHandler
import ru.tggc.telegrambotcore.formatter.FormatService
import ru.tggc.telegrambotcore.service.TelegramBotSender
import ru.tggc.telegrambotcore.service.UserService
import ru.tggc.telegrambotcore.service.defaults.NoOpUserService
import ru.tggc.telegrambotspringbootstarter.autoconfigure.LifecycleAutoConfiguration
import ru.tggc.telegrambotspringbootstarter.autoconfigure.LongPollingAutoConfiguration
import ru.tggc.telegrambotspringbootstarter.autoconfigure.TelegramBotAutoConfiguration
import ru.tggc.telegrambotspringbootstarter.autoconfigure.WebhookAutoConfiguration
import ru.tggc.telegrambotspringbootstarter.runner.TelegramBotRunner

class MinimalBotTests {
    private val runner = ApplicationContextRunner()
        .withConfiguration(
            AutoConfigurations.of(
                TelegramBotAutoConfiguration::class.java,
                LongPollingAutoConfiguration::class.java,
                WebhookAutoConfiguration::class.java
            )
        )

    @Test
    fun `token alone supplies all infrastructure without message files`() {
        runner.withPropertyValues("telegram.token=123:test").run { context ->
            assertThat(context).hasNotFailed()
            assertThat(context).hasSingleBean(TelegramBot::class.java)
            assertThat(context).hasSingleBean(TelegramBotRunner::class.java)
            assertThat(context).hasSingleBean(TaskScheduler::class.java)
            assertThat(context).hasSingleBean(FormatService::class.java)
            assertThat(context.getBean(UserService::class.java)).isInstanceOf(NoOpUserService::class.java)
            assertThat(context.getBean(ExceptionHandler::class.java)).isInstanceOf(DefaultExceptionHandler::class.java)
            assertThat(context).doesNotHaveBean(WebhookAutoConfiguration::class.java)
        }
    }

    @Test
    fun `polling lifecycle delivers start from a private chat without username`() {
        val bot = mock(TelegramBot::class.java)
        val sent = mock(SendResponse::class.java)
        `when`(sent.isOk).thenReturn(true)
        `when`(bot.execute(any(SendMessage::class.java))).thenReturn(sent)
        runner.withPropertyValues("telegram.token=123:test")
            .withBean(TelegramBot::class.java, { bot })
            .withUserConfiguration(StartHandler::class.java)
            .withConfiguration(AutoConfigurations.of(LifecycleAutoConfiguration::class.java))
            .run { context ->
                assertThat(context).hasNotFailed()
                val listener = ArgumentCaptor.forClass(UpdatesListener::class.java)
                verify(bot).setUpdatesListener(listener.capture())
                val update = BotUtils.parseUpdate(
                    """
                    {"update_id":1,"message":{"message_id":1,"date":1,
                    "chat":{"id":42,"type":"private","first_name":"Alex"},
                    "from":{"id":42,"is_bot":false,"first_name":"Alex"},
                    "text":"/start","entities":[{"type":"bot_command","offset":0,"length":6}]}}
                """.trimIndent()
                )
                assertThat(listener.value.process(listOf(update))).isEqualTo(UpdatesListener.CONFIRMED_UPDATES_ALL)
                val request = ArgumentCaptor.forClass(SendMessage::class.java)
                verify(bot, timeout(3000)).execute(request.capture())
                assertThat(request.value.parameters).containsEntry("chat_id", 42L).containsEntry("text", "привет")
            }
    }

    @Test
    fun `custom infrastructure replaces defaults`() {
        val users = mock(UserService::class.java)
        val errors = mock(ExceptionHandler::class.java)
        val scheduler = mock(TaskScheduler::class.java)
        runner.withPropertyValues("telegram.token=123:test")
            .withBean(UserService::class.java, { users })
            .withBean(ExceptionHandler::class.java, { errors })
            .withBean(TaskScheduler::class.java, { scheduler })
            .run { context ->
                assertThat(context).hasNotFailed()
                assertThat(context).hasSingleBean(UserService::class.java)
                assertThat(context).hasSingleBean(ExceptionHandler::class.java)
                assertThat(context).hasSingleBean(TaskScheduler::class.java)
                assertThat(context.getBean(UserService::class.java)).isSameAs(users)
                assertThat(context.getBean(ExceptionHandler::class.java)).isSameAs(errors)
                assertThat(context.getBean(TaskScheduler::class.java)).isSameAs(scheduler)
            }
    }

    @Test
    fun `missing token has an actionable error`() {
        runner.run { context ->
            assertThat(context).hasFailed()
            assertThat(context.startupFailure).hasStackTraceContaining("Set telegram.token")
        }
    }

    @Test
    fun `explicitly configured missing messages are not silently ignored`() {
        runner.withPropertyValues("telegram.token=123:test", "telegram.base-names[0]=missing-messages")
            .run { context ->
                assertThat(context).hasFailed()
                assertThat(context.startupFailure).hasStackTraceContaining("missing-messages.yml")
            }
    }

    @Test
    fun `admin notifications do nothing when no admin is configured`() {
        val bot = mock(TelegramBot::class.java)
        runner.withBean(TelegramBot::class.java, { bot }).run { context ->
            assertThat(context).hasNotFailed()
            context.getBean(TelegramBotSender::class.java).sendToAdmin("test")
            verifyNoInteractions(bot)
        }
    }

    @Test
    fun `default user service never grants required roles`() {
        val users = NoOpUserService()
        assertThat(users.checkRoles(42, emptyArray())).isTrue()
        UserRole.entries.forEach { role ->
            assertThat(users.checkRoles(42, arrayOf(role))).isFalse()
        }
    }

    @Test
    fun `bot id is required only when a bot added handler is registered`() {
        runner.withPropertyValues("telegram.token=123:test")
            .withUserConfiguration(AddedHandler::class.java)
            .run { context ->
                assertThat(context).hasFailed()
                assertThat(context.startupFailure).hasStackTraceContaining("Set telegram.bot-id when using @BotAddedHandle")
            }
        runner.withPropertyValues("telegram.token=123:test", "telegram.bot-id=123")
            .withUserConfiguration(AddedHandler::class.java)
            .run { context -> assertThat(context).hasNotFailed() }
    }

    @Test
    fun `explicit webhook selects only webhook runner`() {
        runner.withPropertyValues("telegram.token=123:test", "telegram.mode=webhook")
            .run { context ->
                assertThat(context).hasNotFailed()
                assertThat(context).hasSingleBean(TelegramBotRunner::class.java)
                assertThat(context).doesNotHaveBean(LongPollingAutoConfiguration::class.java)
                assertThat(context).hasSingleBean(WebhookAutoConfiguration::class.java)
            }
    }

    @BotHandler
    class StartHandler {
        @CommandHandle("start")
        fun start(@ChatId chatId: Long): Response = Response.of(SendMessage(chatId, "привет"))
    }

    @BotHandler
    class AddedHandler {
        @BotAddedHandle
        fun added(): Response = Response.empty()
    }
}
