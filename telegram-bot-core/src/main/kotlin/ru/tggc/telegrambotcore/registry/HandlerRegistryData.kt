package ru.tggc.telegrambotcore.registry

data class HandlerRegistryData(
    val registeredHandlers: MutableMap<String, RegisteredHandler>,
)
