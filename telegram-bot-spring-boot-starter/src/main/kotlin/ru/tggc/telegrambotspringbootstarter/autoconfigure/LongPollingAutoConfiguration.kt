package ru.tggc.telegrambotspringbootstarter.autoconfigure

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.UpdatesListener
import com.pengrad.telegrambot.model.Update
import io.github.oshai.kotlinlogging.KotlinLogging
import lombok.extern.slf4j.Slf4j
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import ru.tggc.telegrambotcore.service.TelegramBotReceiver
import ru.tggc.telegrambotspringbootstarter.runner.TelegramBotRunner

@Slf4j
@AutoConfiguration(after = [TelegramBotAutoConfiguration::class])
@ConditionalOnProperty(prefix = "telegram", name = ["mode"], havingValue = "longpolling", matchIfMissing = true)
class LongPollingAutoConfiguration {
    private val log = KotlinLogging.logger {}

    @Bean
    fun telegramBotRunner(telegramBot: TelegramBot, receiver: TelegramBotReceiver): TelegramBotRunner =
        TelegramBotRunner {
            log.info { "Starting telegram bot via longPolling" }
            telegramBot.setUpdatesListener { updates: MutableList<Update> ->
                updates.forEach { update: Update -> receiver.receiveUpdate(update) }
                UpdatesListener.CONFIRMED_UPDATES_ALL
            }
        }

}
