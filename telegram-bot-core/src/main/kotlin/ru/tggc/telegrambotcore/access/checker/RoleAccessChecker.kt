package ru.tggc.telegrambotcore.access.checker

import com.pengrad.telegrambot.model.Chat
import com.pengrad.telegrambot.model.User
import com.pengrad.telegrambot.request.SendMessage
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import ru.tggc.telegrambotcore.access.annotationprovider.AnnotationProviderFactory
import ru.tggc.telegrambotcore.dto.AccessResult
import ru.tggc.telegrambotcore.dto.Response
import ru.tggc.telegrambotcore.service.UserService
import java.lang.reflect.Method

@Component
@Order(1)
class RoleAccessChecker(
    private val userService: UserService,
    private val factory: AnnotationProviderFactory
) : AccessChecker {
    override fun check(from: User, method: Method, chat: Chat): AccessResult {
        val roles = factory.getAnnotationMeta(method).requiredRoles

        if (roles.isEmpty() || userService.checkRoles(from.id(), roles)) {
            return AccessResult.allow()
        }

        return AccessResult.deny(
            Response.of {
                SendMessage(chat.id(), "У вас недостаточно прав для этой команды")
            }
        )
    }
}
