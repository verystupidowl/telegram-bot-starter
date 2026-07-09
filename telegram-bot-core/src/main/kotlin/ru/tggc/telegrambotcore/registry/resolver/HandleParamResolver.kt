package ru.tggc.telegrambotcore.registry.resolver

import org.springframework.stereotype.Component
import ru.tggc.telegrambotcore.annotation.params.HandleParam
import ru.tggc.telegrambotcore.util.ParamConverter
import java.lang.reflect.Parameter

@Component
class HandleParamResolver : ParameterResolver<Any?> {
    override fun supports(p: Parameter?): Boolean? = p?.isAnnotationPresent(HandleParam::class.java)

    override fun resolve(parameter: Parameter?, ctx: HandlerCtx): Any? =
        ctx.matcher
            ?.takeIf { it.matches() }
            ?.let { matcher ->
                val name = parameter
                    ?.getAnnotation(HandleParam::class.java)
                    ?.value
                    ?: return null

                ParamConverter.convert(
                    matcher.group(name),
                    parameter.type
                )
            }
}
