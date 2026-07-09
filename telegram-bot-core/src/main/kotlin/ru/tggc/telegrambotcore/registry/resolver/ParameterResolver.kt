package ru.tggc.telegrambotcore.registry.resolver

import java.lang.reflect.Parameter

interface ParameterResolver<P> {
    fun supports(p: Parameter?): Boolean?

    fun resolve(parameter: Parameter?, ctx: HandlerCtx): P?
}
