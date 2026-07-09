package ru.tggc.telegrambotcore.access.checker

import com.pengrad.telegrambot.model.Chat
import com.pengrad.telegrambot.model.User
import lombok.RequiredArgsConstructor
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import ru.tggc.telegrambotcore.dto.AccessResult
import ru.tggc.telegrambotcore.dto.ResponseBuilder
import ru.tggc.telegrambotcore.service.UserRateLimiterService
import java.lang.reflect.Method

@Component
@Order(4)
@RequiredArgsConstructor
class RateLimitAccessChecker(private val userRateLimiterService: UserRateLimiterService) : AccessChecker {
    override fun check(from: User, method: Method, chat: Chat): AccessResult =
        userRateLimiterService.checkRateLimit(from)
            .map {
                val response = ResponseBuilder.to(chat.id())
                    .message(it)
                    .build()
                AccessResult.deny(response)
            }
            .orElseGet { AccessResult.allow() }
}
