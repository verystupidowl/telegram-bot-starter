package ru.tggc.telegrambotcore.dto

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import com.pengrad.telegrambot.response.BaseResponse
import java.util.concurrent.CompletableFuture

/** An immutable, deferred text or caption edit. Already usable as a Response. */
class EditReply internal constructor(
    private val createResponse: (InlineKeyboardMarkup?) -> Response,
    private val markup: InlineKeyboardMarkup? = null
) : Response {
    fun keyboard(markup: InlineKeyboardMarkup?): EditReply = EditReply(createResponse, markup)

    fun build(): Response = this

    override fun accept(bot: TelegramBot): CompletableFuture<BaseResponse?> =
        createResponse(markup).accept(bot)
}
