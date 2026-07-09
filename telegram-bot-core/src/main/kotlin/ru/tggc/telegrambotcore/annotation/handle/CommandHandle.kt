package ru.tggc.telegrambotcore.annotation.handle

import ru.tggc.telegrambotcore.dto.UserRole

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class CommandHandle(
    val value: String,
    val requiredRoles: Array<UserRole> = [],
    val canPrivate: Boolean = false,
    val canPublic: Boolean = true
)
