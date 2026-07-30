package ru.tggc.telegrambotcore.dto

@JvmRecord
data class DialogSession @JvmOverloads constructor(
    val state: HistoryKey,
    val data: MutableMap<String, String>,
    val promptMessageId: Int? = null
)