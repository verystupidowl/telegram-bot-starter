package ru.tggc.telegrambotspringbootstarter

import com.pengrad.telegrambot.utility.BotUtils
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.tggc.telegrambotcore.service.TelegramBotReceiver

@RestController
@RequestMapping("/telegram")
class WebhookController(private val receiver: TelegramBotReceiver) {
    private val log = KotlinLogging.logger { }

    @PostMapping("/webhook")
    fun onUpdateReceived(@RequestBody updateJson: String): ResponseEntity<Void> {
        log.debug { "onUpdateReceived $updateJson" }
        val update = BotUtils.parseUpdate(updateJson)
        receiver.receiveUpdate(update)
        return ResponseEntity.ok().build()
    }
}
