package ru.tggc.telegrambotcore.annotation.handle

import ru.tggc.telegrambotcore.dto.Access

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class TextHandle(
    val access: Access = Access.OWNER_ONLY
)
