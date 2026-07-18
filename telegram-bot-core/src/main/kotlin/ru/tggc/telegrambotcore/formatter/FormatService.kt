package ru.tggc.telegrambotcore.formatter

@Deprecated("Use TelegramMessages instead", ReplaceWith("TelegramMessages"))
interface FormatService {
    fun get(key: MsgKey, vararg args: Any): String
}
