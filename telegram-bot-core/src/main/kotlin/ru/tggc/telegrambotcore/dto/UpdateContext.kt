package ru.tggc.telegrambotcore.dto

import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import java.util.*

@JvmRecord
data class UpdateContext(
    val chatId: Long,
    val userId: Long,
    val messageId: Int = 0
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
        chatId: Long = this.chatId,
        messageId: Int = this.messageId,
    ): Response = ResponseBuilder.to(chatId)
        .editPhoto(messageId, photoUrl, caption)
        .build()

}
