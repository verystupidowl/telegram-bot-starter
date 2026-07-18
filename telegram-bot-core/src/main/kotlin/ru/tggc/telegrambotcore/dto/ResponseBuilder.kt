package ru.tggc.telegrambotcore.dto

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import com.pengrad.telegrambot.request.DeleteMessage
import com.pengrad.telegrambot.request.EditMessageCaption
import com.pengrad.telegrambot.request.SendMessage
import com.pengrad.telegrambot.request.SendPhoto
import ru.tggc.telegrambotcore.ext.executeAsync

class ResponseBuilder internal constructor(private var chatId: Long?) {
    private val actions: MutableList<Response> = ArrayList()

    fun add(action: Response): ResponseBuilder = apply {
        this.actions.add(action)
    }


    fun addAll(action: Collection<Response>): ResponseBuilder = apply {
        this.actions.addAll(action)
    }


    @JvmOverloads
    fun message(text: String, markup: InlineKeyboardMarkup? = null): ResponseBuilder = apply {
        actions += Response.create { bot: TelegramBot ->
            val sm = SendMessage(chatId!!, text)
            markup?.let { sm.replyMarkup = markup }
            bot.executeAsync(sm)
        }
    }

    fun messages(messages: Collection<String>): ResponseBuilder = apply {
        messages.forEach { this.message(it) }
    }

    fun photo(photo: PhotoDto): ResponseBuilder = apply {
        actions += Response.create { bot: TelegramBot ->
            val sp = SendPhoto(photo.chatId, photo.url!!)
            photo.caption?.let { sp.caption = it }
            photo.markup?.let { sp.replyMarkup = it }
            bot.executeAsync(sp)
        }
    }

    fun photos(photos: Collection<PhotoDto>): ResponseBuilder = apply {
        photos.forEach { this.photo(it) }
    }

    fun edit(photos: List<PhotoDto>, messageId: Int): ResponseBuilder = apply {
        if (photos.isNotEmpty()) {
            actions += Response.create { it.executeAsync(DeleteMessage(chatId, messageId)) }
            photos(photos)
        }
    }

    @JvmOverloads
    fun edit(messageId: Int, newText: String?, markup: InlineKeyboardMarkup? = null): ResponseBuilder = apply {
        actions += Response.create { bot: TelegramBot ->
            val ed = EditMessageCaption(chatId, messageId)
            ed.caption(newText)
            markup?.let { ed.replyMarkup(it) }
            bot.executeAsync(ed)
        }
    }

    fun editPhoto(messageId: Int, photoUrl: String?, caption: String?): ResponseBuilder = apply {
        actions += Response.create { it.executeAsync(DeleteMessage(chatId, messageId)) }
        val photo = PhotoDto(
            url = photoUrl,
            caption = caption,
            chatId = chatId!!
        )
        photo(photo)
    }

    fun build(): Response = Response.ofAllResponses(actions)

    companion object {
        @JvmStatic
        fun to(chatId: Long): ResponseBuilder = ResponseBuilder(chatId)

        @JvmStatic
        fun create(): ResponseBuilder = ResponseBuilder(null)
    }
}
