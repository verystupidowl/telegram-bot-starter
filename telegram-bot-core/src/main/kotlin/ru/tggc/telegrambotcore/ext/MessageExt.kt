@file:JvmName("TelegramMessageUtils")

package ru.tggc.telegrambotcore.ext

import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.model.MessageEntity
import java.util.regex.Matcher
import java.util.regex.Pattern

private val COMMAND_PATTERN: Pattern = Pattern.compile("^/(?<command>[a-zA-Z0-9_]+)(?:@\\w+)?(?:\\s.*)?$")

fun Message.hasText(): Boolean = this.text() != null

fun Message.hasEntities(): Boolean = this.entities() != null && !this.entities().isEmpty()

fun Message.hasPhoto(): Boolean = this.photo() != null && this.photo().size != 0

fun Message.command(): String {
    if (this.hasText() && this.hasEntities() && this.hasCommand()) {
        val matcher: Matcher = COMMAND_PATTERN.matcher(this.text().lowercase())

        if (matcher.matches()) {
            return matcher.group("command")
        }
    }
    return ""
}

fun Message.hasCommand(): Boolean = this.entities()?.any { it.type() == MessageEntity.Type.bot_command } ?: false
