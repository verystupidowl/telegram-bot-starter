package ru.tggc.telegrambotcore.dto

import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup

data class PhotoDto(
    val url: String? = null,
    val caption: String? = null,
    val chatId: Long = 0,
    val markup: InlineKeyboardMarkup? = null
)
