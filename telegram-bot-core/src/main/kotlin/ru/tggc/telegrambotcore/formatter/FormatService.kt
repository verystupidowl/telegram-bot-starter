package ru.tggc.telegrambotcore.formatter

interface FormatService {
    fun get(key: MsgKey, vararg args: Any): String

    fun random(key: MsgKey, vararg args: Any): String

    fun <T : Any> getObject(key: MsgKey, clazz: Class<T>): T

    fun <T : Any> getList(key: MsgKey, clazz: Class<T>): List<T>

    fun <T : Any> randomObject(key: MsgKey, clazz: Class<T>): T
}