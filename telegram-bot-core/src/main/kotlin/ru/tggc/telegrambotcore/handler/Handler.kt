package ru.tggc.telegrambotcore.handler

import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import ru.tggc.telegrambotcore.dto.PhotoDto
import ru.tggc.telegrambotcore.dto.Response
import ru.tggc.telegrambotcore.dto.ResponseBuilder

@Deprecated(message = "Use ctx instead")
abstract class Handler() {
    fun sendSimplePhoto(photo: PhotoDto): Response {
        return ResponseBuilder.create()
            .photo(photo)
            .build()
    }

    fun sendSimplePhotos(photos: MutableList<PhotoDto>): Response {
        return ResponseBuilder.create()
            .photos(photos)
            .build()
    }

    @JvmOverloads
    fun sendSimpleMessage(chatId: Long, text: String, markup: InlineKeyboardMarkup? = null): Response {
        return ResponseBuilder.to(chatId)
            .message(text, markup)
            .build()
    }

    fun sendSimpleMessages(chatId: Long, texts: MutableList<String>): Response {
        return ResponseBuilder.to(chatId)
            .messages(texts)
            .build()
    }

    fun editMessageCaption(chatId: Long, messageId: Int, caption: String?, markup: InlineKeyboardMarkup?): Response {
        return ResponseBuilder.to(chatId)
            .edit(messageId, caption, markup)
            .build()
    }

    fun editPhotos(messageId: Int, photos: MutableList<PhotoDto>): Response {
        return ResponseBuilder.create()
            .edit(photos, messageId)
            .build()
    }

    fun editPhoto(chatId: Long, messageId: Int, photoUrl: String?, caption: String?): Response {
        return ResponseBuilder.to(chatId)
            .editPhoto(messageId, photoUrl, caption)
            .build()
    }

    @JvmOverloads
    fun editSimpleMessage(chatId: Long, messageId: Int, text: String?, markup: InlineKeyboardMarkup? = null): Response {
        return ResponseBuilder.to(chatId)
            .edit(messageId, text, markup)
            .build()
    }
}
