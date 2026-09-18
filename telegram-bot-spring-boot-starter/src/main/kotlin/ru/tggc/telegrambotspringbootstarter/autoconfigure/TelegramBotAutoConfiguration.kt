package ru.tggc.telegrambotspringbootstarter.autoconfigure

import com.pengrad.telegrambot.TelegramBot
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.core.io.ResourceLoader
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import ru.tggc.telegrambotcore.exception.DefaultExceptionHandler
import ru.tggc.telegrambotcore.exception.ExceptionHandler
import ru.tggc.telegrambotcore.service.UserService
import ru.tggc.telegrambotcore.service.defaults.NoOpUserService
import ru.tggc.telegrambotcore.formatter.MessageLoader
import ru.tggc.telegrambotcore.formatter.FormatService
import ru.tggc.telegrambotcore.formatter.YamlFormatService
import ru.tggc.telegrambotspringbootstarter.TelegramProperties
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.json.JsonMapper

@AutoConfiguration
@ComponentScan("ru.tggc.telegrambotcore")
@EnableConfigurationProperties(TelegramProperties::class)
open class TelegramBotAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    open fun telegramBot(properties: TelegramProperties): TelegramBot {
        val token = properties.token
        require(!token.isNullOrBlank()) { "Set telegram.token (for example, via TELEGRAM_TOKEN)." }
        return TelegramBot(token)
    }

    @Bean
    @ConditionalOnMissingBean(UserService::class)
    open fun telegramUserService(): UserService = NoOpUserService()

    @Bean
    @ConditionalOnMissingBean(ExceptionHandler::class)
    open fun telegramExceptionHandler(): ExceptionHandler = DefaultExceptionHandler()

    @Bean
    @ConditionalOnMissingBean(TaskScheduler::class)
    open fun telegramTaskScheduler(): ThreadPoolTaskScheduler = ThreadPoolTaskScheduler().apply {
        poolSize = 1
        setThreadNamePrefix("telegram-scheduler-")
        isRemoveOnCancelPolicy = true
    }

    @Bean
    @ConditionalOnMissingBean
    open fun messageLoader(resourceLoader: ResourceLoader): MessageLoader = MessageLoader(resourceLoader)

    @Bean
    @ConditionalOnMissingBean
    open fun telegramMessages(
        properties: TelegramProperties,
        messageLoader: MessageLoader,
        mapper: ObjectProvider<ObjectMapper>
    ): FormatService {
        val messages = messageLoader.load(properties.baseNames)

        return YamlFormatService(messages, mapper.getIfAvailable { JsonMapper.builder().build() })
    }

}
