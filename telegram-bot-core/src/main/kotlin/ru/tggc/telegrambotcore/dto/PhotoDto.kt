package ru.tggc.telegrambotcore.dto

import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup

@JvmRecord
data class PhotoDto @JvmOverloads constructor(
    val url: String? = null,
    val caption: String? = null,
    val chatId: Long = 0,
    val markup: InlineKeyboardMarkup? = null
)
