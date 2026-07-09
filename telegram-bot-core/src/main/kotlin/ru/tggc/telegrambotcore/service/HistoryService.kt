package ru.tggc.telegrambotcore.service

import ru.tggc.telegrambotcore.dto.UpdateContext

interface HistoryService {
    fun contains(ctx: UpdateContext?): Boolean
}
