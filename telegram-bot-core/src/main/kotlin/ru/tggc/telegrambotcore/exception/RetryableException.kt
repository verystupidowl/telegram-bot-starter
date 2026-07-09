package ru.tggc.telegrambotcore.exception

open class RetryableException @JvmOverloads constructor(
    message: String?,
    val retryMillis: Int = 2000
) : RuntimeException(message)
