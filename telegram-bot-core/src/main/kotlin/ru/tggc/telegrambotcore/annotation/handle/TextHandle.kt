package ru.tggc.telegrambotcore.annotation.handle

import ru.tggc.telegrambotcore.dto.Access

/**
 * Аннотация для метода-хэндлера, отлавливающего текстовые сообщения с определенным historyKey
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class TextHandle(
    /**
     * historyKey
     */
    val value: String,
    val access: Access = Access.OWNER_ONLY,
    val deleteAfterHandle: Boolean = true,
)
