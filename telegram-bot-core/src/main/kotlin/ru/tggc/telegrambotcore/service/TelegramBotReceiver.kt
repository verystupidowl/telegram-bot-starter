package ru.tggc.telegrambotcore.service

import com.pengrad.telegrambot.model.Update

/**
 * Сервис для отлавливания сообщений
 */
interface TelegramBotReceiver {
    fun receiveUpdate(update: Update)
}
