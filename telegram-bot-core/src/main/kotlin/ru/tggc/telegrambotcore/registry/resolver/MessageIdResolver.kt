package ru.tggc.telegrambotcore.registry.resolver

import org.springframework.stereotype.Component
import ru.tggc.telegrambotcore.annotation.params.MessageId
import java.lang.reflect.Parameter

@Component
class MessageIdResolver : ParameterResolver<Int?> {
    override fun supports(p: Parameter?): Boolean? = p?.isAnnotationPresent(MessageId::class.java)

    override fun resolve(parameter: Parameter?, ctx: HandlerCtx): Int? = ctx.messageId
}
