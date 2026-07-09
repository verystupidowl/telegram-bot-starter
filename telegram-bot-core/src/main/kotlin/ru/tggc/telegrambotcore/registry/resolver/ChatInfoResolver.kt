package ru.tggc.telegrambotcore.registry.resolver

import org.springframework.stereotype.Component
import ru.tggc.telegrambotcore.annotation.params.ChatInfo
import ru.tggc.telegrambotcore.dto.ChatDto
import java.lang.reflect.Parameter

@Component
class ChatInfoResolver : ParameterResolver<ChatDto?> {
    override fun supports(p: Parameter?): Boolean? =
        p?.isAnnotationPresent(ChatInfo::class.java) == true && p.getType() == ChatDto::class.java

    override fun resolve(parameter: Parameter?, ctx: HandlerCtx): ChatDto =
        ChatDto(ctx.chat!!.id(), ctx.chat.title())

}
