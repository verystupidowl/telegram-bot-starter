package ru.tggc.telegrambotcore.access.checker

import com.pengrad.telegrambot.model.Chat
import com.pengrad.telegrambot.model.User
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import ru.tggc.telegrambotcore.access.annotationprovider.AnnotationProviderFactory
import ru.tggc.telegrambotcore.dto.AccessResult
import ru.tggc.telegrambotcore.dto.Response
import java.lang.reflect.Method

@Component
@Order(2)
class ChatTypeAccessChecker(private val factory: AnnotationProviderFactory) : AccessChecker {
    override fun check(from: User, method: Method, chat: Chat): AccessResult {
        val annotationMeta = factory.getAnnotationMeta(method)

        val isPrivateMessage = chat.type() == Chat.Type.Private

        val canRequestBePrivate = annotationMeta.canPrivate
        val canRequestBePublic = annotationMeta.canPublic
        if ((isPrivateMessage && canRequestBePrivate) || (!isPrivateMessage && canRequestBePublic)) {
            return AccessResult.allow()
        }
        return AccessResult.deny(Response.empty())
    }
}
