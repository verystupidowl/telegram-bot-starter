package ru.tggc.telegrambotcore.dto

import java.util.*

@JvmRecord
data class UpdateContext(
    val chatId: Long,
    val userId: Long,
    val messageId: Int = 0
) {
    override fun hashCode(): Int {
        return Objects.hash(chatId, userId)
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || javaClass != other.javaClass) return false
        val that = other as UpdateContext
        return chatId == that.chatId && userId == that.userId
    }
}
