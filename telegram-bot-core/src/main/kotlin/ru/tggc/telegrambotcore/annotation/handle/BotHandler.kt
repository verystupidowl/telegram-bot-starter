package ru.tggc.telegrambotcore.annotation.handle

import org.springframework.stereotype.Component

/**
 * Аннотация для класса-хэндлера
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Component
annotation class BotHandler 
