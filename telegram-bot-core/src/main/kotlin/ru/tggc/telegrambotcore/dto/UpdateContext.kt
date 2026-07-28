package ru.tggc.telegrambotcore.dto

import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.model.request.ChatAction
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import com.pengrad.telegrambot.model.request.ParseMode
import com.pengrad.telegrambot.request.DeleteMessage
import com.pengrad.telegrambot.request.SendChatAction
import com.pengrad.telegrambot.request.SendMessage
import com.pengrad.telegrambot.request.SendPhoto
import com.pengrad.telegrambot.response.SendResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import org.apache.logging.log4j.util.Supplier
import ru.tggc.telegrambotcore.ext.bindToUser
import ru.tggc.telegrambotcore.ext.executeAsync
import ru.tggc.telegrambotcore.service.HistoryService
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

@JvmRecord
data class UpdateContext(
    val chatId: Long,
    val userId: Long,
    val messageId: Int = 0,
) {
    companion object {
        @JvmStatic
        internal lateinit var historyService: HistoryService
    }

    override fun hashCode(): Int = Objects.hash(chatId, userId)

    override fun equals(other: Any?): Boolean {
        if (other == null || javaClass != other.javaClass) return false
        val that = other as UpdateContext
        return chatId == that.chatId && userId == that.userId
    }

    fun send(photo: PhotoDto): Response {
        val photoDto = PhotoDto(
            url = photo.url,
            caption = photo.caption,
            chatId = photo.chatId,
            markup = photo.markup?.bindToUser(userId),
        )
        return ResponseBuilder.create()
            .photo(photoDto)
            .build()
    }


    fun sendWithDelete(photo: PhotoDto): Response {
        return send(photo)
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
        .message(text, markup?.bindToUser(userId))
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
        .edit(photos, messageId, chatId = this.chatId)
        .build()


    @JvmOverloads
    fun edit(
        photoUrl: String?,
        caption: String?,
        markup: InlineKeyboardMarkup? = null,
        chatId: Long = this.chatId,
        messageId: Int = this.messageId,
    ): Response = ResponseBuilder.to(chatId)
        .editPhoto(messageId, photoUrl, caption, markup?.bindToUser(userId))
        .build()

    fun edit(photo: PhotoDto): Response = edit(
        caption = photo.caption!!,
        markup = photo.markup,
        chatId = photo.chatId,
        photoUrl = photo.url
    )

    @JvmOverloads
    fun delete(messageId: Int = this.messageId, chatId: Long = this.chatId): Response =
        ResponseBuilder.to(chatId)
            .delete(messageId = messageId)
            .build()

    @JvmOverloads
    fun sendNonNull(text: String, markup: InlineKeyboardMarkup? = null): Response {
        return Response.create { bot ->
            val sm = SendMessage(this.chatId, text)
            markup?.let { sm.replyMarkup = it.bindToUser(userId) }
            bot.executeAsync(sm)
        }
    }

    @JvmOverloads
    fun askPhoto(
        photoUrl: String,
        caption: String,
        historyKey: HistoryKey,
        markup: InlineKeyboardMarkup? = null,
        failAction: Consumer<DialogSession> = Consumer {}
    ): Response {
        return Response.create { bot ->
            val sp = SendPhoto(this.chatId, photoUrl).parseMode(ParseMode.HTML).caption(caption)
            markup?.let { sp.replyMarkup = markup.bindToUser(userId) }

            val sendResponse: SendResponse = bot.executeAsync(sp)
            val promptMessageId = sendResponse.message().messageId()

            historyService.setHistory(this, historyKey, promptMessageId, failAction)

            return@create sendResponse
        }
    }

    @JvmOverloads
    fun ask(
        text: String,
        historyKey: HistoryKey,
        markup: InlineKeyboardMarkup? = null,
        failAction: Consumer<DialogSession> = Consumer {}
    ): Response {
        return Response.create { bot ->
            val sm = SendMessage(this.chatId, text).parseMode(ParseMode.HTML)
            markup?.let { sm.replyMarkup = markup.bindToUser(userId) }

            val sendResponse: SendResponse = bot.executeAsync(sm)
            val promptMessageId = sendResponse.message().messageId()

            historyService.setHistory(this, historyKey, promptMessageId, failAction)

            return@create sendResponse
        }
    }

    fun cleanPromptAndInput(): Response {
        val promptMessageId = historyService.getPromptMessageId(this)

        historyService.removeFromHistory(this)

        val builder = ResponseBuilder.to(this.chatId)

        if (promptMessageId != null) {
            builder.delete(chatId = this.chatId, messageId = promptMessageId)
        }

        if (this.messageId > 0) {
            builder.delete(chatId = this.chatId, messageId = this.messageId)
        }

        return builder.build()
    }

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

            send.accept(bot).await()!!
        }
    }
}
