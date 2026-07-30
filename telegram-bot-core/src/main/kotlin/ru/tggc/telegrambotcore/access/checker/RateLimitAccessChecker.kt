package ru.tggc.telegrambotcore.access.checker

import com.pengrad.telegrambot.model.Chat
import com.pengrad.telegrambot.model.User
import com.pengrad.telegrambot.request.DeleteMessage
import com.pengrad.telegrambot.request.SendMessage
import lombok.RequiredArgsConstructor
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import ru.tggc.telegrambotcore.dto.AccessResult
import ru.tggc.telegrambotcore.dto.Response
import ru.tggc.telegrambotcore.ext.executeAsync
import ru.tggc.telegrambotcore.service.TelegramBotSender
import ru.tggc.telegrambotcore.service.UserRateLimiterService
import java.lang.reflect.Method

@Component
@Order(4)
@RequiredArgsConstructor
class RateLimitAccessChecker(
    private val userRateLimiterService: UserRateLimiterService,
    private val telegramBotSender: TelegramBotSender
) : AccessChecker {
    override fun check(from: User, method: Method, chat: Chat): AccessResult =
        userRateLimiterService.checkRateLimit(from)
            .map { result ->
                val response = Response.create { bot ->
                    val execute = bot.executeAsync(SendMessage(chat.id(), result.text))
                    telegramBotSender.sendDelayed(Response.create {
                        bot.executeAsync(DeleteMessage(chat.id(), execute.message().messageId()))

                    }, result.retryAfter?.times(1000) ?: 10000L)
                    return@create execute
                }

                AccessResult.deny(response)
            }
            .orElseGet { AccessResult.allow() }
}
