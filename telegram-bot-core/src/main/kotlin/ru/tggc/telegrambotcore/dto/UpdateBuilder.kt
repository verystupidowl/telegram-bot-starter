package ru.tggc.telegrambotcore.dto

import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import com.pengrad.telegrambot.model.request.InputMediaPhoto
import com.pengrad.telegrambot.request.EditMessageMedia
import com.pengrad.telegrambot.request.EditMessageText
import com.pengrad.telegrambot.request.SendMessage
import com.pengrad.telegrambot.request.SendPhoto
import ru.tggc.telegrambotcore.ext.bindToUser
import ru.tggc.telegrambotcore.ext.executeAsync
import ru.tggc.telegrambotcore.keyboard.KeyboardFactory
import ru.tggc.telegrambotcore.keyboard.KeyboardKey

class UpdateBuilder(val chatId: Long, val userId: Long, val messageId: Int? = null) {
    var responseType: ResponseType? = null
    var text: String? = null
    var photo: String? = null
    var keyboard: InlineKeyboardMarkup? = null

    companion object {
        internal lateinit var keyboardFactory: KeyboardFactory
    }

    fun message(text: String): UpdateBuilder = apply {
        this.text = text
    }

    fun responseType(responseType: ResponseType): UpdateBuilder = apply {
        this.responseType = responseType
    }

    fun photo(photo: String): UpdateBuilder = apply {
        this.photo = photo
    }

    @JvmOverloads
    fun <T> keyboard(keyboard: KeyboardKey<T>, data: T? = null): UpdateBuilder = apply {
        this.keyboard = keyboardFactory.getKeyboardInline(keyboard, data)
    }

    fun send(): Response {
        this.responseType = if (photo != null) ResponseType.SEND_PHOTO else ResponseType.SEND_MESSAGE
        return sendWithType()
    }

    fun edit(): Response {
        this.responseType = if (photo != null) ResponseType.EDIT_PHOTO else ResponseType.EDIT_MESSAGE
        return sendWithType()
    }

    fun sendWithType(): Response {
        return when (responseType) {
            ResponseType.SEND_MESSAGE -> return Response.create { bot ->
                val sm = SendMessage(chatId, text!!)
                keyboard?.let { sm.replyMarkup = it.bindToUser(userId) }

                bot.executeAsync(sm)
            }

            ResponseType.SEND_PHOTO -> return Response.create { bot ->
                val sp = SendPhoto(chatId, photo!!)
                keyboard?.let { sp.replyMarkup = it.bindToUser(userId) }
                text?.let { sp.caption = it }

                bot.executeAsync(sp)
            }

            ResponseType.EDIT_MESSAGE -> return Response.create { bot ->
                val emt = EditMessageText(chatId, messageId!!, text!!)

                keyboard?.let { emt.replyMarkup(it.bindToUser(userId)) }

                bot.executeAsync(emt)
            }

            ResponseType.EDIT_PHOTO -> return Response.create { bot ->
                val imp = InputMediaPhoto(photo!!)
                text?.let { imp.caption(it) }
                val emm = EditMessageMedia(chatId, messageId!!, imp)

                keyboard?.let { emm.replyMarkup(it.bindToUser(userId)) }

                bot.executeAsync(emm)
            }

            else -> Response.empty()
        }
    }
}