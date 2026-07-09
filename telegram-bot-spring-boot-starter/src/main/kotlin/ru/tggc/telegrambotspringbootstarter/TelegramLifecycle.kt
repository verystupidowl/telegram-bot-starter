package ru.tggc.telegrambotspringbootstarter

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.SmartLifecycle
import ru.tggc.telegrambotspringbootstarter.runner.TelegramBotRunner

class TelegramLifecycle(private val runner: TelegramBotRunner) : SmartLifecycle {
    private val log = KotlinLogging.logger {}
    private var running = false

    override fun start() {
        runner.start()
        running = true
    }

    override fun stop() {
        log.info { "Stopping Telegram bot runner" }
        running = false
    }

    override fun isRunning(): Boolean = running

}
