package ru.tggc.telegrambotcore.registry

import com.pengrad.telegrambot.model.Update
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import ru.tggc.telegrambotcore.access.checker.GlobalAccessChecker
import ru.tggc.telegrambotcore.annotation.handle.TextHandle
import ru.tggc.telegrambotcore.dto.Response
import ru.tggc.telegrambotcore.dto.UpdateContext
import ru.tggc.telegrambotcore.exception.ExceptionHandler
import ru.tggc.telegrambotcore.registry.resolver.HandlerArgumentResolver
import ru.tggc.telegrambotcore.registry.resolver.HandlerCtx
import ru.tggc.telegrambotcore.service.HistoryService
import ru.tggc.telegrambotcore.service.UserRateLimiterService
import ru.tggc.telegrambotcore.service.UserService

@Order(10)
@Component
class TextHandleRegistry(
    handlerScanner: HandlerScanner,
    rateLimiter: UserRateLimiterService,
    exceptionHandler: ExceptionHandler,
    globalAccessChecker: GlobalAccessChecker,
    userService: UserService,
    private val historyService: HistoryService,
    private val handlerArgumentResolver: HandlerArgumentResolver
) : AbstractHandleRegistry(
    handlerScanner,
    rateLimiter,
    exceptionHandler,
    globalAccessChecker,
    userService
) {
    override val handleAnnotation: Class<out Annotation?>
        get() = TextHandle::class.java

    private val log = KotlinLogging.logger { }

    override fun dispatch(update: Update): Response? {
        val message = update.message()
        val chat = message.chat()
        val from = message.from()
        val messageId = message.messageId()

        saveOrUpdateUser(from, chat)

        val ctx = UpdateContext(chat.id(), from.id(), messageId)

        val currentState = historyService.getFromHistory(ctx)
        if (currentState == null) {
            log.warn { "History state disappeared during dispatch for user ${from.id()}" }
            return Response.empty()
        }

        val stateName = currentState.name()
        val methodHolder = handlerMap[stateName]

        if (methodHolder == null) {
            log.warn { "No handler found for history state: $stateName" }
            historyService.removeFromHistory(ctx)
            return Response.empty()
        }

        log.debug { "Routing history input to $stateName from ${from.username()}" }

        val handlerCtx = HandlerCtx(update, chat, from, messageId, null)
        val args = handlerArgumentResolver.resolve(methodHolder.method!!, handlerCtx)

        var response = invokeWithCatch(from, methodHolder.method!!, methodHolder.bean, args, chat)
        if (methodHolder.method!!.getAnnotation(TextHandle::class.java).deleteAfterHandle) {
            response = response?.andThen(ctx.cleanPromptAndInput())
        }
        return response
    }

    override fun canHandle(update: Update): Boolean {
        val message = update.message()
        if (message?.text() == null) return false

        val ctx = UpdateContext(message.chat().id(), message.from().id(), message.messageId())
        return historyService.contains(ctx)
    }
}