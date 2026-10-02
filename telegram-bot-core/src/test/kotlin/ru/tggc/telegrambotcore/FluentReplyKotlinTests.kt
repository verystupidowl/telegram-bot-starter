package ru.tggc.telegrambotcore

import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import com.pengrad.telegrambot.model.request.InlineKeyboardButton
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.tggc.telegrambotcore.dto.*

class FluentReplyKotlinTests {
    @Test
    fun `media chains coexist with named legacy edit arguments`() {
        val ctx = UpdateContext(42, 99, 7)
        val keyboard = InlineKeyboardMarkup(InlineKeyboardButton("Бой").callbackData("fight"))
        assertThat(ctx.edit("Подпись").keyboard(keyboard)).isInstanceOf(EditReply::class.java)
        assertThat(ctx.editText("Текст").keyboard(keyboard)).isInstanceOf(EditReply::class.java)
        assertThat(ctx.sendPhoto("file-id").caption("Подпись").keyboard(keyboard)).isInstanceOf(PhotoReply::class.java)
        assertThat(ctx.editPhoto("file-id").keyboard(keyboard).caption(null)).isInstanceOf(PhotoReply::class.java)
        assertThat(ctx.edit(photoUrl = "file-id", caption = null).keyboard(keyboard)).isInstanceOf(PhotoReply::class.java)
        assertThat(ctx.edit(caption = "Подпись", messageId = 123)).isInstanceOf(Response::class.java)
        assertThat(ctx.edit(photoUrl = "file-id", caption = null, messageId = 123)).isInstanceOf(Response::class.java)
    }

    private object Bet : HistoryKey {
        override fun name(): String = "BET"
        override fun getLabel(): String = "Ставка"
    }

    @Test
    fun `fluent overloads and existing named default arguments remain usable from Kotlin`() {
        val ctx = UpdateContext(42, 99)
        val keyboard = InlineKeyboardMarkup(InlineKeyboardButton("Отмена").callbackData("cancel"))
        val reply: Response = ctx.send("Привет").keyboard(keyboard)
        val prompt: Response = ctx.ask("Введите ставку", Bet).keyboard(keyboard).fallback { }
        assertThat(reply).isInstanceOf(SendReply::class.java)
        assertThat(prompt).isInstanceOf(AskReply::class.java)
        assertThat(ctx.send(text = "Другой чат", chatId = 123)).isInstanceOf(Response::class.java)
        assertThat(ctx.ask(text = "Ставка", historyKey = Bet, failAction = { })).isInstanceOf(Response::class.java)
    }
}
