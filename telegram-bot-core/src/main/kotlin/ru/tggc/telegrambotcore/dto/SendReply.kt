package ru.tggc.telegrambotcore.dto

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import com.pengrad.telegrambot.response.BaseResponse
import java.util.concurrent.CompletableFuture

/** A configurable text response. Creating or building it does not send a message. */
class SendReply internal constructor(
    private val context: UpdateContext,
    private val text: String,
    private val markup: InlineKeyboardMarkup? = null
) : Response {
    fun keyboard(markup: InlineKeyboardMarkup?): SendReply = SendReply(context, text, markup)

    /** Optional: this object already implements Response. */
    fun build(): Response = this

    override fun accept(bot: TelegramBot): CompletableFuture<BaseResponse?> =
        context.send(text, markup, context.chatId).accept(bot)
}
