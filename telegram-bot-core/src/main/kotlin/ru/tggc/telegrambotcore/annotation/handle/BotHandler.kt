package ru.tggc.telegrambotcore.annotation.handle

import org.springframework.stereotype.Component

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Component
annotation class BotHandler 
