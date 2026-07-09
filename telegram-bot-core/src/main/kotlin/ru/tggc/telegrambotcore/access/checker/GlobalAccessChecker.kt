package ru.tggc.telegrambotcore.access.checker

import com.pengrad.telegrambot.model.Chat
import com.pengrad.telegrambot.model.User
import lombok.extern.slf4j.Slf4j
import org.springframework.stereotype.Component
import ru.tggc.telegrambotcore.dto.Response
import java.lang.reflect.Method

@Slf4j
@Component
class GlobalAccessChecker(private val accessCheckers: MutableList<AccessChecker>) {
    fun check(from: User, method: Method, chat: Chat): Response? {
        accessCheckers.forEach { accessChecker ->
            val accessResult = accessChecker.check(from, method, chat)
            if (!accessResult.allowed) {
                return accessResult.response
            }
        }
        return null
    }
}
