package ru.tggc.telegrambotcore.registry

import java.lang.reflect.Method

data class HandlerRegistryData(
    val registeredHandlers: MutableMap<String, RegisteredHandler>,
    val defaultMethod: Method?,
    val defaultBean: Any?
)
