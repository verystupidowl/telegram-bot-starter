package ru.tggc.telegrambotcore.annotation.handle

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class BotAddedHandle(val value: String = "bot_added")
