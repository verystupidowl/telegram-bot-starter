package ru.tggc.telegrambotcore.registry

import com.pengrad.telegrambot.model.Update
import io.github.oshai.kotlinlogging.KotlinLogging
import lombok.extern.slf4j.Slf4j
import org.springframework.stereotype.Component
import ru.tggc.telegrambotcore.access.checker.GlobalAccessChecker
import ru.tggc.telegrambotcore.annotation.handle.MessageHandle
import ru.tggc.telegrambotcore.dto.Response
import ru.tggc.telegrambotcore.dto.UpdateContext
import ru.tggc.telegrambotcore.exception.ExceptionHandler
import ru.tggc.telegrambotcore.registry.resolver.HandlerArgumentResolver
import ru.tggc.telegrambotcore.registry.resolver.HandlerCtx
import ru.tggc.telegrambotcore.service.HistoryService
import ru.tggc.telegrambotcore.service.UserRateLimiterService
import ru.tggc.telegrambotcore.service.UserService
import java.util.*

@Component
@Slf4j
class MessageHandleRegistry(
    handlerScanner: HandlerScanner,
    rateLimiter: UserRateLimiterService,
    exceptionHandler: ExceptionHandler,
    globalAccessChecker: GlobalAccessChecker,
    private val handlerArgumentResolver: HandlerArgumentResolver,
    private val historyService: HistoryService,
    userService: UserService
) : AbstractHandleRegistry(handlerScanner, rateLimiter, exceptionHandler, globalAccessChecker, userService) {
    private val log = KotlinLogging.logger { }

    override val handleAnnotation: Class<out Annotation?>?
        get() = MessageHandle::class.java

    override fun dispatch(update: Update): Response? {
        val message = update.message()

        val text = message.text().lowercase(Locale.getDefault())
        val method = handlerMap.values
            .map { it.method }
            .firstOrNull { m ->
                val template = m!!.getAnnotation(MessageHandle::class.java)!!.value
                if (template.lowercase() == text) return@firstOrNull true
                val p = handlerMap[template]!!.pattern
                p != null && p.matcher(text).matches()
            }

        val chat = message.chat()
        val from = message.from()
        var response: Response? = Response.empty()

        saveOrUpdateUser(from, chat)

        if (method == null) {
            if (defaultMethod == null) {
                log.warn { "Unknown message: $text" }
            } else {
                val ctx = UpdateContext(chat.id(), from.id(), message.messageId())
                if (historyService.contains(ctx)) {
                    val handlerCtx = HandlerCtx(
                        update,
                        chat,
                        from,
                        0,
                        null
                    )
                    val args = handlerArgumentResolver.resolve(defaultMethod!!, handlerCtx)
                    response = invokeWithCatch(from, defaultMethod!!, defaultBean, args, chat)
                }
            }
            return response
        }
        log.info { "message ${message.text()} from ${from.username()}" }

        val template = method.getAnnotation(MessageHandle::class.java)!!.value
        val matcher = handlerMap[template]?.pattern?.matcher(template)

        val ctx = HandlerCtx(
            update,
            chat,
            from,
            0,
            matcher
        )
        val args = handlerArgumentResolver.resolve(method, ctx)
        return invokeWithCatch(from, method, handlerMap[template]!!.bean, args, chat)
    }

    override fun canHandle(update: Update): Boolean {
        return update.message() != null && update.message().text() != null && (update.message().photo() == null
                || update.message().photo().size == 0)
    }
}
