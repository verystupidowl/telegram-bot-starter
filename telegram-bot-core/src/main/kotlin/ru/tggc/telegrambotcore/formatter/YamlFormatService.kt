package ru.tggc.telegrambotcore.formatter

import io.github.oshai.kotlinlogging.KotlinLogging
import tools.jackson.databind.ObjectMapper
import java.text.MessageFormat

class YamlFormatService(
    private val messages: Map<String, Any>,
    private val mapper: ObjectMapper
) : FormatService {
    private val log = KotlinLogging.logger {}

    init {
        log.info { messages.keys }
    }

    override fun get(key: MsgKey, vararg args: Any): String {
        val value = find(key.key)

        require(value is String)

        return MessageFormat.format(value, *args)
    }

    override fun random(key: MsgKey, vararg args: Any): String {
        val value = find(key.key)

        require(value is List<*>)

        val text = value.random() as String

        return MessageFormat.format(text, *args)
    }

    override fun <T : Any> getObject(key: MsgKey, clazz: Class<T>): T {
        val value = find(key.key)
        return mapper.convertValue(value, clazz)
    }

    override fun <T : Any> getList(key: MsgKey, clazz: Class<T>): List<T> {
        val value = find(key.key)
        return (value as List<*>)
            .map { mapper.convertValue(it, clazz) }
    }

    override fun <T : Any> randomObject(key: MsgKey, clazz: Class<T>): T {
        val list = getList(key, clazz)

        return list.random()
    }

    override fun exists(key: MsgKey): Boolean {
        try {
            find(key.key)
            return true
        } catch (_: Exception) {
            return false
        }
    }


    private fun find(key: String): Any {
        var current: Any = messages

        key.split(".").forEach { part ->
            current = (current as Map<*, *>)[part]
                ?: error("message: $part not found")
        }

        return current
    }
}