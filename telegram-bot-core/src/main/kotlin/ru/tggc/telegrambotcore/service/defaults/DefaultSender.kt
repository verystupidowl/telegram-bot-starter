package ru.tggc.telegrambotcore.service.defaults

import com.pengrad.telegrambot.TelegramBot
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.scheduling.TaskScheduler
import org.springframework.stereotype.Service
import ru.tggc.telegrambotcore.dto.Response
import ru.tggc.telegrambotcore.dto.ResponseBuilder
import ru.tggc.telegrambotcore.exception.RetryableException
import ru.tggc.telegrambotcore.exception.RetryableWithSecsException
import ru.tggc.telegrambotcore.service.TelegramBotSender
import java.time.Instant
import java.util.function.Consumer

@Service
open class DefaultSender(
    private val telegramBot: TelegramBot,
    private val taskScheduler: TaskScheduler
) : TelegramBotSender {
    @Value($$"${telegram.admin-id}")
    private val adminId: Long = 0

    private val log = KotlinLogging.logger {}

    @Retryable(
        retryFor = [RetryableWithSecsException::class],
        maxAttempts = 1,
        backoff = Backoff(delay = 5000, multiplier = 2.0)
    )
    override fun send(response: Response) {
        try {
            response.accept(telegramBot)
                .exceptionally { e: Throwable? ->
                    log.error { "error $e" }
                    null
                }
        } catch (e: RetryableException) {
            taskScheduler.schedule(
                { send(response) },
                Instant.now().plusSeconds(e.retryMillis.toLong())
            )
        }
    }

    override fun sendToAdmin(text: String) {
        val response: Response = ResponseBuilder.to(adminId)
            .message(text, null)
            .build()
        response.accept(telegramBot)
    }

    override fun sendDelayed(task: Consumer<TelegramBot>, delayMillis: Long) {
        taskScheduler.schedule(
            { task.accept(telegramBot) },
            Instant.now().plusMillis(delayMillis)
        )
    }

    override fun sendDelayed(response: Response, delayMillis: Long) {
        taskScheduler.schedule(
            { response.accept(telegramBot) },
            Instant.now().plusMillis(delayMillis)
        )
    }

    @Recover
    open fun recover(e: RetryableWithSecsException, response: Response?) {
        sendToAdmin("Сообщение для пользователя не отправилось с ошибкой " + e.message)
        log.error { "Попытки отправить сообщение исчерпаны $e" }
    }
}