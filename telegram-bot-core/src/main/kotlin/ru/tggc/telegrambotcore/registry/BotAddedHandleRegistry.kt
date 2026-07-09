package ru.tggc.telegrambotcore.registry

import com.pengrad.telegrambot.model.Update
import lombok.extern.slf4j.Slf4j
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import ru.tggc.telegrambotcore.access.checker.GlobalAccessChecker
import ru.tggc.telegrambotcore.annotation.handle.BotAddedHandle
import ru.tggc.telegrambotcore.dto.Response
import ru.tggc.telegrambotcore.exception.ExceptionHandler
import ru.tggc.telegrambotcore.registry.resolver.HandlerArgumentResolver
import ru.tggc.telegrambotcore.registry.resolver.HandlerCtx
import ru.tggc.telegrambotcore.service.UserRateLimiterService
import ru.tggc.telegrambotcore.service.UserService

@Component
@Slf4j
class BotAddedHandleRegistry(
    handlerScanner: HandlerScanner,
    rateLimiter: UserRateLimiterService,
    exceptionHandler: ExceptionHandler,
    globalAccessChecker: GlobalAccessChecker,
    userService: UserService,
    private val handlerArgumentResolver: HandlerArgumentResolver
) : AbstractHandleRegistry(handlerScanner, rateLimiter, exceptionHandler, globalAccessChecker, userService) {
    @Value($$"${telegram.bot-id}")
    private var botId: Long = 0

    override val handleAnnotation: Class<out Annotation?>?
        get() = BotAddedHandle::class.java

    override fun dispatch(update: Update): Response? {
        val message = update.message()

        val method = handlerMap.values
            .map { it.method }
            .firstOrNull { m ->
                val template = m?.getAnnotation(BotAddedHandle::class.java)?.value
                    ?: throw RuntimeException("Template for $m not found")
                template == "bot_added"
            }
            ?: throw RuntimeException("Cannot find handler for $update")
        val chat = message.chat()
        val from = message.from()
        val messageId = message.messageId()

        saveOrUpdateUser(from, chat)

        val ctx = HandlerCtx(
            update,
            chat,
            from,
            messageId,
            null
        )
        val args = handlerArgumentResolver.resolve(method, ctx)
        return invokeWithCatch(from, method, handlerMap["bot_added"]?.bean, args, chat)
    }

    override fun canHandle(update: Update): Boolean =
        update.message()
            ?.newChatMembers()
            ?.any { member -> member.id() == botId } ?: false

}
