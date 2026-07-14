package ru.tggc.telegrambotcore.dto

import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup

@JvmRecord
data class PhotoDto @JvmOverloads constructor(
    val url: String? = null,
    val caption: String? = null,
    val chatId: Long = 0,
    val markup: InlineKeyboardMarkup? = null
) {
    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder() {
        var url: String? = null
        var caption: String? = null
        var chatId: Long = 0
        var markup: InlineKeyboardMarkup? = null
        fun url(url: String): Builder = apply { this.url = url }
        fun caption(caption: String): Builder = apply { this.caption = caption }
        fun chatId(chatId: Long): Builder = apply { this.chatId = chatId }
        fun markup(markup: InlineKeyboardMarkup): Builder = apply { this.markup = markup }
        fun build(): PhotoDto = PhotoDto(url, caption, chatId, markup)
    }
}
