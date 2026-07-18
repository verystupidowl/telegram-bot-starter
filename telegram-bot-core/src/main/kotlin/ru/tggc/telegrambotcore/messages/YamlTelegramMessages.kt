package ru.tggc.telegrambotcore.messages

import org.springframework.stereotype.Component
import ru.tggc.telegrambotcore.formatter.MsgKey
import java.text.MessageFormat

@Component
class YamlTelegramMessages(private val messages: Map<String, Any>) : TelegramMessages {
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

    private fun find(key: String): Any {
        var current: Any = messages

        key.split(".").forEach { part ->
            current = (current as Map<*, *>)[part]
                ?: error("message: $part not found")
        }

        return current
    }
}