package ru.tggc.telegrambotcore.exception

import com.pengrad.telegrambot.model.Chat
import com.pengrad.telegrambot.model.User
import com.pengrad.telegrambot.request.SendMessage
import io.github.oshai.kotlinlogging.KotlinLogging
import ru.tggc.telegrambotcore.dto.Response

/** Logs the error without exposing internal exception details to the user. */
class DefaultExceptionHandler : ExceptionHandler {
    private val log = KotlinLogging.logger {}

    override fun handleException(e: Exception, chat: Chat, from: User): Response {
        log.error(e) { "Telegram handler failed for chat ${chat.id()}, user ${from.id()}" }
        return Response.of(SendMessage(chat.id(), "Не удалось выполнить команду. Попробуйте ещё раз."))
    }

    override fun buildMessageToAdmin(s: String, chat: Chat, from: User): String =
        "Chat ${chat.id()}, user ${from.id()}: $s"
}
