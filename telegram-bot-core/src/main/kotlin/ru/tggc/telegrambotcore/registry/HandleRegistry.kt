package ru.tggc.telegrambotcore.registry

import com.pengrad.telegrambot.model.Update
import ru.tggc.telegrambotcore.dto.Response

interface HandleRegistry {
    fun dispatch(update: Update): Response?

    fun canHandle(update: Update): Boolean
}
