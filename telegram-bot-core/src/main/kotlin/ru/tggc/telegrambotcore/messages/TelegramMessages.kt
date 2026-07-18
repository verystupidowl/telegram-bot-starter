package ru.tggc.telegrambotcore.messages

import ru.tggc.telegrambotcore.formatter.MsgKey

interface TelegramMessages {
    fun get(key: MsgKey, vararg args: Any): String

    fun random(key: MsgKey, vararg args: Any): String
}