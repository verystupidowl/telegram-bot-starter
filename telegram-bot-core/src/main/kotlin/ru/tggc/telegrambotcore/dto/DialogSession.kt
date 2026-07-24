package ru.tggc.telegrambotcore.dto

@JvmRecord
data class DialogSession(val state: HistoryKey, val data: Map<String, String>)