package ru.tggc.telegrambotspringbootstarter.autoconfigure

import com.pengrad.telegrambot.TelegramBot
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import ru.tggc.telegrambotspringbootstarter.TelegramProperties

@AutoConfiguration
@ComponentScan("ru.tggc.telegrambotcore")
@EnableConfigurationProperties(TelegramProperties::class)
class TelegramBotAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun telegramBot(properties: TelegramProperties): TelegramBot = TelegramBot(properties.token)

}
