package ru.tggc.telegrambotcore.registry.resolver

import com.pengrad.telegrambot.model.Update
import org.springframework.stereotype.Component
import java.lang.reflect.Parameter

@Component
class UpdateResolver : ParameterResolver<Update?> {
    override fun supports(p: Parameter?): Boolean = p?.getType() == Update::class.java

    override fun resolve(parameter: Parameter?, ctx: HandlerCtx): Update? = ctx.update
}
