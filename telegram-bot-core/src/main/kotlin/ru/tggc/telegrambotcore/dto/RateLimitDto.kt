package ru.tggc.telegrambotcore.dto

data class RateLimitDto(
    val text: String,
    val retryAfter: Long? = null
)
