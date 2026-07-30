package ru.tggc.telegrambotcore.keyboard

import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup
import java.util.function.Function
import java.util.function.Supplier

abstract class AbstractKeyboardCreator<T> protected constructor(override val keyboardKey: KeyboardKey<T>) :
    KeyboardCreator<T, ReplyKeyboardMarkup> {
    protected open val rowsFunction: Function<T?, Array<Array<String>>>? = null

    protected open val rowsSupplier: Supplier<Array<Array<String>>>? = null

    override fun create(t: T?): ReplyKeyboardMarkup {
        val rows: Array<Array<String>> = rowsSupplier?.get()
            ?: rowsFunction?.apply(t)
            ?: throw RuntimeException("Логика кнопок не определена для ${keyboardKey.keyboardId}")
        return ReplyKeyboardMarkup(*rows)
    }
}
