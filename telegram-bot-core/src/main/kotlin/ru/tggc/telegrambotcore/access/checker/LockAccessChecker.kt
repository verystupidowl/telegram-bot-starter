package ru.tggc.telegrambotcore.access.checker

import com.pengrad.telegrambot.model.Chat
import com.pengrad.telegrambot.model.User
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import ru.tggc.telegrambotcore.dto.AccessResult
import ru.tggc.telegrambotcore.dto.Response
import ru.tggc.telegrambotcore.service.UserRateLimiterService
import java.lang.reflect.Method

@Component
@Order(3)
class LockAccessChecker(private val rateLimiterService: UserRateLimiterService) : AccessChecker {
    override fun check(from: User, method: Method, chat: Chat): AccessResult {
        return if (rateLimiterService.isLocked(from.id())) AccessResult.deny(Response.empty()) else AccessResult.allow()
    }
}
