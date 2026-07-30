package ru.tggc.telegrambotcore.keyboard

import com.pengrad.telegrambot.model.request.Keyboard

interface KeyboardCreator<T, K : Keyboard> {
    fun create(t: T?): K

    val keyboardKey: KeyboardKey<T>
}