package ru.tggc.telegrambotcore.dto

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import com.pengrad.telegrambot.response.BaseResponse
import ru.tggc.telegrambotcore.ext.bindToUser
import java.util.concurrent.CompletableFuture

/** An immutable photo send or media edit. A null caption means no caption. */
class PhotoReply internal constructor(
    private val context: UpdateContext,
    private val photo: PhotoDto,
    private val messageId: Int? = null
) : Response {
    fun caption(caption: String?): PhotoReply = PhotoReply(context, photo.copy(caption = caption), messageId)

    fun keyboard(markup: InlineKeyboardMarkup?): PhotoReply = PhotoReply(context, photo.copy(markup = markup), messageId)

    fun build(): Response = this

    override fun accept(bot: TelegramBot): CompletableFuture<BaseResponse?> {
        val markup = photo.markup?.bindToUser(context.userId)
        val builder = ResponseBuilder.to(photo.chatId)
        if (messageId == null) {
            builder.photo(photo.copy(markup = markup))
        } else {
            builder.editPhoto(messageId, photo.url, photo.caption, markup)
        }
        return builder.build().accept(bot)
    }
}
