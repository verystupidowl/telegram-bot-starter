package ru.tggc.telegrambotcore.formatter

import org.apache.logging.log4j.util.Supplier

class MessageBuilder private constructor() {
    private val lines = mutableListOf<String>()

    fun empty() = line("")

    fun line(text: String) = apply {
        lines += text
    }

    fun line(condition: Boolean, text: () -> String) = apply {
        if (condition) {
            lines += text()
        }
    }

    fun lines(condition: Boolean, vararg text: Supplier<String>) = apply {
        if (condition) {
            text.forEach { lines += it.get() }
        }
    }

    fun build() = lines.joinToString("\n")

    companion object {
        @JvmStatic
        fun create(): MessageBuilder = MessageBuilder()
    }
}