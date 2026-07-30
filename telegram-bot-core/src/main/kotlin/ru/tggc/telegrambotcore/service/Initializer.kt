package ru.tggc.telegrambotcore.service

import org.springframework.stereotype.Component
import ru.tggc.telegrambotcore.dto.UpdateBuilder
import ru.tggc.telegrambotcore.dto.UpdateContext
import ru.tggc.telegrambotcore.keyboard.KeyboardFactory

@Component
class Initializer(private val historyService: HistoryService, private val keyboardFactory: KeyboardFactory) {
    init {
        UpdateBuilder.keyboardFactory = keyboardFactory
        UpdateContext.historyService = historyService
    }
}
