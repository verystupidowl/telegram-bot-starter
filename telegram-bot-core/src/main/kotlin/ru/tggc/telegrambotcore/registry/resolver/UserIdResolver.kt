package ru.tggc.telegrambotcore.registry.resolver

import org.springframework.stereotype.Component
import ru.tggc.telegrambotcore.annotation.params.UserId
import java.lang.reflect.Parameter

@Component
class UserIdResolver : ParameterResolver<Long?> {
    override fun supports(p: Parameter?): Boolean? = p?.isAnnotationPresent(UserId::class.java)

    override fun resolve(parameter: Parameter?, ctx: HandlerCtx): Long? = ctx.from?.id()
}
