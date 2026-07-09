package ru.tggc.telegrambotcore.formatter

interface FormatService {
    fun getMessage(key: MsgKey, vararg args: Any?): String
}
