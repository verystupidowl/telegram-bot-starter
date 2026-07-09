package ru.tggc.telegrambotcore.registry.resolver

import org.springframework.stereotype.Component
import ru.tggc.telegrambotcore.annotation.params.ChatId
import java.lang.reflect.Parameter

@Component
class ChatIdResolver : ParameterResolver<Long?> {
    override fun supports(p: Parameter?): Boolean? = p?.isAnnotationPresent(ChatId::class.java)

    override fun resolve(parameter: Parameter?, ctx: HandlerCtx): Long? = ctx.chat!!.id()

}
