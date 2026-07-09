package ru.tggc.telegrambotcore.exception

import com.pengrad.telegrambot.model.Chat
import com.pengrad.telegrambot.model.User
import ru.tggc.telegrambotcore.dto.Response

interface ExceptionHandler {
    fun handleException(e: Exception, chat: Chat, from: User): Response

    fun buildMessageToAdmin(s: String, chat: Chat, from: User): String
}
