package ru.tggc.telegrambotcore.dto

import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup

/**
 * Reply signatures retained for binary compatibility. UpdateContext's covariant overrides
 * generate JVM bridges returning Response, so previously compiled callers still link.
 */
interface ReplyOperations {
    fun send(text: String): Response
    fun send(text: String, markup: InlineKeyboardMarkup?): Response
    fun send(photo: PhotoDto): Response
    fun edit(caption: String): Response
    fun edit(caption: String, markup: InlineKeyboardMarkup?): Response
    fun edit(caption: String, markup: InlineKeyboardMarkup?, chatId: Long): Response
    fun edit(photoUrl: String?, caption: String?): Response
    fun edit(photoUrl: String?, caption: String?, markup: InlineKeyboardMarkup?): Response
    fun edit(photoUrl: String?, caption: String?, markup: InlineKeyboardMarkup?, chatId: Long): Response
    fun edit(photo: PhotoDto): Response
    fun ask(text: String, historyKey: HistoryKey): Response
    fun ask(text: String, historyKey: HistoryKey, markup: InlineKeyboardMarkup?): Response
}
