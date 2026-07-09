package ru.tggc.telegrambotcore.service

import com.pengrad.telegrambot.TelegramBot
import ru.tggc.telegrambotcore.dto.Response
import java.util.function.Consumer

interface TelegramBotSender {
    fun send(response: Response)

    fun sendDelayed(task: Consumer<TelegramBot>, delayMillis: Long)

    fun sendToAdmin(text: String)
}
