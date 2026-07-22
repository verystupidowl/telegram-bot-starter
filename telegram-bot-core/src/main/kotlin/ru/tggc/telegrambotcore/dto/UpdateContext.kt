package ru.tggc.telegrambotcore.dto

import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.model.request.ChatAction
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import com.pengrad.telegrambot.request.DeleteMessage
import com.pengrad.telegrambot.request.SendChatAction
import com.pengrad.telegrambot.request.SendMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.logging.log4j.util.Supplier
import ru.tggc.telegrambotcore.ext.executeAsync
import java.util.*
import java.util.concurrent.CompletableFuture

@JvmRecord
data class UpdateContext(
    val chatId: Long,
    val userId: Long,
    val messageId: Int = 0,
) {
    override fun hashCode(): Int = Objects.hash(chatId, userId)

    override fun equals(other: Any?): Boolean {
        if (other == null || javaClass != other.javaClass) return false
        val that = other as UpdateContext
        return chatId == that.chatId && userId == that.userId
    }

    fun send(photo: PhotoDto): Response = ResponseBuilder.create()
        .photo(photo)
        .build()


    fun sendWithDelete(photo: PhotoDto): Response {
        return this.send(photo)
            .andThen { bot ->
                bot.execute(DeleteMessage(this.chatId, this.messageId))
                return@andThen CompletableFuture.completedFuture(null)
            }
    }

    @JvmOverloads
    fun send(
        text: String,
        markup: InlineKeyboardMarkup? = null,
        chatId: Long = this.chatId
    ): Response = ResponseBuilder.to(chatId)
        .message(text, markup)
        .build()

    @JvmOverloads
    fun send(entities: List<Any>, chatId: Long = this.chatId): Response {
        val first = entities.firstOrNull() ?: return Response.empty()

        return when (first) {
            is PhotoDto -> {
                @Suppress("UNCHECKED_CAST")
                ResponseBuilder.to(chatId)
                    .photos(entities as List<PhotoDto>)
                    .build()
            }

            is String -> {
                @Suppress("UNCHECKED_CAST")
                ResponseBuilder.to(chatId)
                    .messages(entities as List<String>)
                    .build()
            }

            else -> throw IllegalArgumentException("Unsupported list element type: ${first.javaClass}")
        }
    }

    @JvmOverloads
    fun sendWithDelete(text: String, markup: InlineKeyboardMarkup? = null, chatId: Long = this.chatId): Response {
        return this.send(text = text, chatId = chatId, markup = markup)
            .andThen { bot ->
                bot.execute(DeleteMessage(chatId, this.messageId))
                return@andThen CompletableFuture.completedFuture(null)
            }
    }

    @JvmOverloads
    fun edit(
        caption: String,
        markup: InlineKeyboardMarkup? = null,
        chatId: Long = this.chatId,
        messageId: Int = this.messageId,
    ): Response = ResponseBuilder.to(chatId)
        .edit(messageId, caption, markup)
        .build()


    @JvmOverloads
    fun edit(photos: List<PhotoDto>, messageId: Int = this.messageId): Response = ResponseBuilder.create()
        .edit(photos, messageId)
        .build()


    @JvmOverloads
    fun edit(
        photoUrl: String?,
        caption: String?,
        markup: InlineKeyboardMarkup? = null,
        chatId: Long = this.chatId,
        messageId: Int = this.messageId,
    ): Response = ResponseBuilder.to(chatId)
        .editPhoto(messageId, photoUrl, caption, markup)
        .build()

    fun edit(photo: PhotoDto): Response = edit(
        caption = photo.caption!!,
        markup = photo.markup,
        chatId = photo.chatId,
        photoUrl = photo.url
    )

    @JvmOverloads
    fun sendWithLoader(textSupplier: Supplier<PhotoDto>, isDelete: Boolean = false, text: String? = null): Response {
        return Response.create { bot ->
            var message: Message? = null
            if (text != null) {
                message = bot.executeAsync(SendMessage(this.chatId, text)).message()
            }
            bot.executeAsync(SendChatAction(this.chatId, ChatAction.upload_photo))

            val photo = withContext(Dispatchers.IO) {
                textSupplier.get()
            }

            var send = if (isDelete) {
                sendWithDelete(photo)
            } else {
                send(photo)
            }

            if (message != null) {
                send = send.andThen { bot ->
                    bot.execute(DeleteMessage(chatId, message.messageId()))
                    CompletableFuture.completedFuture(null)
                }
            }

            send.accept(bot)
        }
    }
}
