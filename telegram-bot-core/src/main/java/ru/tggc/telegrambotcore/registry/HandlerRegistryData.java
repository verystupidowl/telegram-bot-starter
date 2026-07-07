package ru.tggc.telegrambotcore.registry;

import java.lang.reflect.Method;
import java.util.Map;

public record HandlerRegistryData(
        Map<String, RegisteredHandler> registeredHandlers,
        Method defaultMethod,
        Object defaultBean
) {
}
