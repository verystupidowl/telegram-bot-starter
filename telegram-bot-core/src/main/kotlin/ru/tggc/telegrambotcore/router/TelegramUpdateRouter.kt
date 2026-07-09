package ru.tggc.telegrambotcore.router

import com.pengrad.telegrambot.model.Update
import ru.tggc.telegrambotcore.dto.Response

interface TelegramUpdateRouter {
    fun route(update: Update): Response?
}
