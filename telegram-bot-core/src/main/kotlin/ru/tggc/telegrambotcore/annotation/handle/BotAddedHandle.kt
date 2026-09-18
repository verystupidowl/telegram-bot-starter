package ru.tggc.telegrambotcore.annotation.handle

/**
 * Аннотация для метода-хэндлера, отлавливающего добавление бота в беседу
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class BotAddedHandle(val value: String = "bot_added")
