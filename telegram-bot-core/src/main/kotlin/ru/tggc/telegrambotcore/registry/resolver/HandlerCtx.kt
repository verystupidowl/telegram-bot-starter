package ru.tggc.telegrambotcore.registry.resolver

import com.pengrad.telegrambot.model.Chat
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.model.User
import java.util.regex.Matcher

data class HandlerCtx(
    val update: Update?,
    val chat: Chat?,
    val from: User?,
    val messageId: Int,
    val matcher: Matcher?
)
