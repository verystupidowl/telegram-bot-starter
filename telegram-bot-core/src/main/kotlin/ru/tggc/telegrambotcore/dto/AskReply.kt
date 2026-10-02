package ru.tggc.telegrambotcore.dto

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import com.pengrad.telegrambot.response.BaseResponse
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

/** A configurable prompt; history and conflict handling use the existing ask implementation. */
class AskReply internal constructor(
    private val context: UpdateContext,
    private val text: String,
    private val historyKey: HistoryKey,
    private val markup: InlineKeyboardMarkup? = null,
    private val failAction: Consumer<DialogSession> = Consumer {}
) : Response {
    fun keyboard(markup: InlineKeyboardMarkup?): AskReply = AskReply(context, text, historyKey, markup, failAction)

    /** Called when a dialog already exists, not when Telegram fails to send the prompt. */
    fun fallback(action: Consumer<DialogSession>): AskReply = AskReply(context, text, historyKey, markup, action)

    /** Optional: this object already implements Response. */
    fun build(): Response = this

    override fun accept(bot: TelegramBot): CompletableFuture<BaseResponse?> =
        context.ask(text, historyKey, markup, failAction).accept(bot)
}
