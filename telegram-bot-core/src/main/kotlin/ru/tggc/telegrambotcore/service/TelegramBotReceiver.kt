package ru.tggc.telegrambotcore.service

import com.pengrad.telegrambot.model.Update

interface TelegramBotReceiver {
    fun receiveUpdate(update: Update?)
}
