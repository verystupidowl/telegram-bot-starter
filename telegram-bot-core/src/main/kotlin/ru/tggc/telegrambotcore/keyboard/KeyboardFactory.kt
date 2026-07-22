package ru.tggc.telegrambotcore.keyboard

import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import org.springframework.stereotype.Component

@Component
class KeyboardFactory(keyboardInlineCreators: MutableList<AbstractInlineKeyboardCreator<*>>) {
    private val keyboardInlineCreators: Map<KeyboardKey<*>, AbstractInlineKeyboardCreator<*>> =
        keyboardInlineCreators.associateBy { it.keyboardKey }

    @Suppress("UNCHECKED_CAST")
    fun <T> getKeyboardCreator(key: KeyboardKey<T>): AbstractInlineKeyboardCreator<T> {
        return keyboardInlineCreators[key] as AbstractInlineKeyboardCreator<T>
    }

    fun <T> getKeyboardInline(type: KeyboardKey<T>, data: T?): InlineKeyboardMarkup {
        val creator: AbstractInlineKeyboardCreator<T> = getKeyboardCreator(type)
        return creator.create(data)
    }

    fun getKeyboardInline(type: KeyboardKey<Void>): InlineKeyboardMarkup {
        return getKeyboardInline(type, null)
    }
}