package ru.tggc.telegrambotcore.router

import com.pengrad.telegrambot.model.Update
import org.springframework.stereotype.Service
import ru.tggc.telegrambotcore.dto.Response
import ru.tggc.telegrambotcore.registry.HandleRegistry

@Service
class TelegramUpdateRouterImpl(private val handlers: MutableList<HandleRegistry>) : TelegramUpdateRouter {
    override fun route(update: Update): Response? = handlers
        .firstOrNull { it.canHandle(update) }
        ?.dispatch(update)
        ?: Response.empty()
}
