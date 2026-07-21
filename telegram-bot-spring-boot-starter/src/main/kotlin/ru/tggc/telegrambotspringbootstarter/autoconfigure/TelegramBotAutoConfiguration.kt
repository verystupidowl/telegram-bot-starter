package ru.tggc.telegrambotspringbootstarter.autoconfigure

import com.pengrad.telegrambot.TelegramBot
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.core.io.ResourceLoader
import ru.tggc.telegrambotcore.formatter.MessageLoader
import ru.tggc.telegrambotcore.formatter.FormatService
import ru.tggc.telegrambotcore.formatter.YamlFormatService
import ru.tggc.telegrambotspringbootstarter.TelegramProperties
import tools.jackson.databind.ObjectMapper

@AutoConfiguration
@ComponentScan("ru.tggc.telegrambotcore")
@EnableConfigurationProperties(TelegramProperties::class)
open class TelegramBotAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    open fun telegramBot(properties: TelegramProperties): TelegramBot = TelegramBot(properties.token)

    @Bean
    open fun messageLoader(resourceLoader: ResourceLoader): MessageLoader = MessageLoader(resourceLoader)

    @Bean
    @ConditionalOnMissingBean
    open fun telegramMessages(
        properties: TelegramProperties,
        resourceLoader: ResourceLoader,
        messageLoader: MessageLoader,
        mapper: ObjectMapper
    ): FormatService {
        val messages = messageLoader.load(properties.baseNames)

        return YamlFormatService(messages, mapper)
    }

}
