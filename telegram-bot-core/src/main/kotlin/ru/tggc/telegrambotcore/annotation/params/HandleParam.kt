package ru.tggc.telegrambotcore.annotation.params

/**
 * Аннотация для внедрения собственной строки через regexp-выражение
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class HandleParam(val value: String)
