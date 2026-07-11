package ru.tggc.telegrambotcore.dto

import java.util.*

@JvmRecord
data class UserDto(
    val userId: Long,
    val username: String
) {
    override fun equals(other: Any?): Boolean {
        if (other == null || javaClass != other.javaClass) return false
        val userDto = other as UserDto
        return userId == userDto.userId
    }

    override fun hashCode(): Int {
        return Objects.hashCode(userId)
    }
}
