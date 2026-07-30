package ru.tggc.telegrambotcore.registry.resolver

import org.springframework.stereotype.Component
import ru.tggc.telegrambotcore.annotation.params.Ctx
import ru.tggc.telegrambotcore.dto.UpdateContext
import java.lang.reflect.Parameter

@Component
class CtxResolver : ParameterResolver<UpdateContext?> {
    override fun supports(p: Parameter?): Boolean? = p?.isAnnotationPresent(Ctx::class.java)

    override fun resolve(parameter: Parameter?, ctx: HandlerCtx): UpdateContext = UpdateContext(
        ctx.chat!!.id(),
        ctx.from!!.id(),
        ctx.messageId
    )
}
