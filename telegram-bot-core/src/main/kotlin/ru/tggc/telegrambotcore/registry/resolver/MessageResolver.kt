package ru.tggc.telegrambotcore.registry.resolver

import com.pengrad.telegrambot.model.Message
import org.springframework.stereotype.Component
import java.lang.reflect.Parameter

@Component
class MessageResolver : ParameterResolver<Message?> {
    override fun supports(p: Parameter?): Boolean? = p?.getType() == Message::class.java

    override fun resolve(parameter: Parameter?, ctx: HandlerCtx): Message? = ctx.update?.message()
}
