package ru.tggc.telegrambotspringbootstarter.autoconfigure

import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.context.annotation.Bean
import ru.tggc.telegrambotspringbootstarter.TelegramLifecycle
import ru.tggc.telegrambotspringbootstarter.runner.TelegramBotRunner

@AutoConfigureAfter(TelegramBotAutoConfiguration::class)
class LifecycleAutoConfiguration {

    @Bean
    fun telegramLifecycle(telegramBotRunner: TelegramBotRunner): TelegramLifecycle =
        TelegramLifecycle(telegramBotRunner)

}
