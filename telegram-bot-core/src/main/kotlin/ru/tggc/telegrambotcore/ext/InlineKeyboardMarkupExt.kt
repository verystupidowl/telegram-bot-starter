package ru.tggc.telegrambotcore.ext

import com.pengrad.telegrambot.model.request.InlineKeyboardButton
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup

fun InlineKeyboardMarkup.bindToUser(userId: Long): InlineKeyboardMarkup {
    val rows = this.inlineKeyboard() ?: return this
    val newRows = rows.map { row ->
        row.map { button ->
            val rawData = button.callbackData
            if (rawData != null && !rawData.contains("#u:")) {
                InlineKeyboardButton(button.text).callbackData("$rawData#u:$userId")
            } else {
                button
            }
        }.toTypedArray()
    }.toTypedArray()
    return InlineKeyboardMarkup(*newRows)
}