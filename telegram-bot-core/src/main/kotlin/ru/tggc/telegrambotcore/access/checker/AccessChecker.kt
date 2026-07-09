package ru.tggc.telegrambotcore.access.checker

import com.pengrad.telegrambot.model.Chat
import com.pengrad.telegrambot.model.User
import ru.tggc.telegrambotcore.dto.AccessResult
import java.lang.reflect.Method

interface AccessChecker {
    fun check(from: User, method: Method, chat: Chat): AccessResult
}
