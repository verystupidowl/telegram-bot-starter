package ru.tggc.telegrambotcore.dto

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import com.pengrad.telegrambot.model.request.InputMediaPhoto
import com.pengrad.telegrambot.model.request.ParseMode
import com.pengrad.telegrambot.request.DeleteMessage
import com.pengrad.telegrambot.request.EditMessageCaption
import com.pengrad.telegrambot.request.EditMessageMedia
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
            val sm = SendMessage(chatId!!, text).parseMode(ParseMode.HTML)
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
                .parseMode(ParseMode.HTML)
            photo.caption?.let { sp.caption = it }
            photo.markup?.let { sp.replyMarkup = it }
            bot.executeAsync(sp)
        }
    }

    fun photos(photos: Collection<PhotoDto>): ResponseBuilder = apply {
        photos.forEach { this.photo(it) }
    }

    fun edit(photos: List<PhotoDto>, messageId: Int, chatId: Long): ResponseBuilder = apply {
        if (photos.isNotEmpty()) {
            actions += Response.create { it.executeAsync(DeleteMessage(chatId, messageId)) }
            photos(photos)
        }
    }

    @JvmOverloads
    fun edit(messageId: Int, newText: String?, markup: InlineKeyboardMarkup? = null): ResponseBuilder = apply {
        actions += Response.create { bot: TelegramBot ->
            val ed = EditMessageCaption(chatId, messageId)
                .parseMode(ParseMode.HTML)
            ed.caption(newText)
            markup?.let { ed.replyMarkup(it) }
            bot.executeAsync(ed)
        }
    }

    fun editPhoto(
        messageId: Int,
        photoUrl: String?,
        caption: String?,
        markup: InlineKeyboardMarkup? = null
    ): ResponseBuilder = apply {
        val media = InputMediaPhoto(photoUrl).caption(caption)
            .parseMode(ParseMode.HTML)
        val emm = EditMessageMedia(chatId, messageId, media)
        markup?.let { emm.replyMarkup(it) }
        actions += Response.create { bot -> bot.executeAsync(emm) }
    }

    fun delete(chatId: Long = this.chatId!!, messageId: Int): ResponseBuilder = apply {
        val deleteMessage = DeleteMessage(chatId, messageId)
        actions += Response.create { bot: TelegramBot -> bot.executeAsync(deleteMessage) }
    }

    fun build(): Response = Response.ofAllResponses(actions)

    companion object {
        @JvmStatic
        fun to(chatId: Long): ResponseBuilder = ResponseBuilder(chatId)

        @JvmStatic
        fun create(): ResponseBuilder = ResponseBuilder(null)
    }
}
