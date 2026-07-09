package ru.tggc.telegrambotcore.registry.resolver

import org.springframework.stereotype.Component
import java.lang.reflect.Method
import java.lang.reflect.Parameter

@Component
class HandlerArgumentResolver(private val parameterResolvers: MutableList<ParameterResolver<*>>) {

    fun resolve(method: Method, ctx: HandlerCtx): Array<Any?> =
        method.parameters
            .map { resolveParameter(it, ctx) }
            .toTypedArray()

    private fun resolveParameter(parameter: Parameter?, ctx: HandlerCtx): Any? =
        parameterResolvers
            .firstOrNull { it.supports(parameter) == true }
            ?.resolve(parameter, ctx)

}
