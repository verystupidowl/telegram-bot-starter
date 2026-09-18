package ru.tggc.telegrambotspringbootstarter.autoconfigure

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.request.SendMessage
import com.pengrad.telegrambot.request.SetWebhook
import io.github.oshai.kotlinlogging.KotlinLogging
import lombok.extern.slf4j.Slf4j
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import ru.tggc.telegrambotspringbootstarter.TelegramProperties
import ru.tggc.telegrambotspringbootstarter.runner.TelegramBotRunner

@AutoConfiguration(after = [TelegramBotAutoConfiguration::class])
@ConditionalOnProperty(prefix = "telegram", name = ["mode"], havingValue = "webhook")
@Slf4j
open class WebhookAutoConfiguration {
    private val log = KotlinLogging.logger {}

    @Bean
    open fun telegramBotRunner(bot: TelegramBot, telegramProperties: TelegramProperties): TelegramBotRunner =
        TelegramBotRunner {
            log.info { "Starting telegram bot via webhook" }
            val url = telegramProperties.webhook.url
            require(!url.isNullOrBlank()) { "Set telegram.webhook.url when telegram.mode=webhook." }
            val response = bot.execute(SetWebhook().url(url))

            log.info { "Webhook info $response" }
            telegramProperties.adminId?.let { adminId ->
                bot.execute(SendMessage(adminId, "Webhook has been set $response"))
            }
        }
}
