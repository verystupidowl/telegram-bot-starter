package ru.tggc.telegrambotcore.service

import ru.tggc.telegrambotcore.dto.DialogSession
import ru.tggc.telegrambotcore.dto.HistoryKey
import ru.tggc.telegrambotcore.dto.UpdateContext
import java.util.*
import java.util.function.Consumer

interface HistoryService {
    fun setHistory(ctx: UpdateContext, historyType: HistoryKey, failAction: Consumer<DialogSession>)
    fun setHistory(ctx: UpdateContext, historyType: HistoryKey)
    fun setHistory(ctx: UpdateContext, historyType: HistoryKey, key: String, value: String)
    fun putData(ctx: UpdateContext, key: String, value: String)
    fun isEmpty(ctx: UpdateContext): Boolean
    fun getData(ctx: UpdateContext, key: String): Optional<String>
    fun isInHistory(ctx: UpdateContext, historyType: HistoryKey): Boolean
    fun contains(ctx: UpdateContext?): Boolean
    fun removeFromHistory(ctx: UpdateContext)
    fun getFromHistory(ctx: UpdateContext): HistoryKey?
}
