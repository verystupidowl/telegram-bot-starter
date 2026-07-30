package ru.tggc.telegrambotcore.keyboard

import com.pengrad.telegrambot.model.request.InlineKeyboardButton
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import java.util.function.Function
import java.util.function.Supplier

abstract class AbstractInlineKeyboardCreator<T>(override val keyboardKey: KeyboardKey<T>) :
    KeyboardCreator<T, InlineKeyboardMarkup> {
    protected open val rowsFunction: Function<T?, MutableList<MutableList<InlineKeyboardButton>>>? = null

    protected open val rowsSupplier: Supplier<MutableList<MutableList<InlineKeyboardButton>>>? = null

    protected fun singleBtn(button: InlineKeyboardButton): MutableList<MutableList<InlineKeyboardButton>> {
        return mutableListOf(mutableListOf(button))
    }

    protected fun rows(vararg buttons: InlineKeyboardButton): MutableList<MutableList<InlineKeyboardButton>> {
        return buttons
            .map { e1: InlineKeyboardButton? -> mutableListOf(e1!!) }
            .toMutableList()
    }

    protected fun btn(text: String?, callbackData: String): InlineKeyboardButton {
        return InlineKeyboardButton(text).callbackData(callbackData)
    }

    override fun create(t: T?): InlineKeyboardMarkup {
        val list: MutableList<MutableList<InlineKeyboardButton>> = rowsSupplier?.get()
            ?: rowsFunction?.apply(t)
            ?: throw RuntimeException("Логика кнопок не определена для ${keyboardKey.keyboardId}")
        val rows = list
            .map { it.toTypedArray<InlineKeyboardButton>() }
            .toTypedArray()
        return InlineKeyboardMarkup(*rows)
    }
}
