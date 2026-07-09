package ru.tggc.telegrambotcore.annotation.params

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class HandleParam(val value: String)
