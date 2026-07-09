package ru.tggc.telegrambotcore.registry.resolver

import org.springframework.stereotype.Component
import ru.tggc.telegrambotcore.annotation.params.Username
import java.lang.reflect.Parameter

@Component
class UsernameResolver : ParameterResolver<String?> {
    override fun supports(p: Parameter?): Boolean? = p?.isAnnotationPresent(Username::class.java)

    override fun resolve(parameter: Parameter?, ctx: HandlerCtx): String? = ctx.from!!.username()
}
