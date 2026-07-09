package ru.tggc.telegrambotcore.registry

import java.lang.reflect.Method
import java.util.regex.Pattern

data class RegisteredHandler(
    var method: Method? = null,
    var bean: Any? = null,
    var pattern: Pattern? = null
)
