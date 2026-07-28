package ru.tggc.telegrambotcore.service.defaults

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.pengrad.telegrambot.model.Update
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import ru.tggc.telegrambotcore.router.TelegramUpdateRouter
import ru.tggc.telegrambotcore.service.TelegramBotReceiver
import ru.tggc.telegrambotcore.service.TelegramBotSender
import java.time.Duration

@Service
open class DefaultReceiver(
    private val router: TelegramUpdateRouter,
    private val sender: TelegramBotSender
) : TelegramBotReceiver {
    private val cachedUpdates: Cache<Int, Boolean?> = Caffeine.newBuilder()
        .maximumSize(100000)
        .expireAfterWrite(Duration.ofMinutes(5))
        .build<Int, Boolean?>()

    @Async
    override fun receiveUpdate(update: Update) {
        if (isNew(update)) {
            router.route(update)?.let { response ->
                sender.send(response)
            }
        }
    }

    fun isNew(update: Update): Boolean {
        val id = update.updateId() ?: return true

        val exists = cachedUpdates.getIfPresent(id)
        if (exists != null) {
            return false
        }

        cachedUpdates.put(id, true)
        return true
    }
}