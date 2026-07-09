package ru.tggc.telegrambotcore.registry

import com.pengrad.telegrambot.model.Update
import io.github.oshai.kotlinlogging.KotlinLogging
import lombok.extern.slf4j.Slf4j
import org.springframework.stereotype.Component
import ru.tggc.telegrambotcore.access.checker.GlobalAccessChecker
import ru.tggc.telegrambotcore.annotation.handle.PhotoHandle
import ru.tggc.telegrambotcore.dto.Response
import ru.tggc.telegrambotcore.exception.ExceptionHandler
import ru.tggc.telegrambotcore.registry.resolver.HandlerArgumentResolver
import ru.tggc.telegrambotcore.registry.resolver.HandlerCtx
import ru.tggc.telegrambotcore.service.UserRateLimiterService
import ru.tggc.telegrambotcore.service.UserService

@Component
@Slf4j
class PhotoHandleRegistry(
    handlerScanner: HandlerScanner,
    rateLimiter: UserRateLimiterService,
    exceptionHandler: ExceptionHandler,
    globalAccessChecker: GlobalAccessChecker,
    userService: UserService,
    private val handlerArgumentResolver: HandlerArgumentResolver
) : AbstractHandleRegistry(handlerScanner, rateLimiter, exceptionHandler, globalAccessChecker, userService) {
    private val log = KotlinLogging.logger {}

    override val handleAnnotation: Class<out Annotation?>?
        get() = PhotoHandle::class.java

    override fun dispatch(update: Update): Response? {
        val message = update.message()

        if (message.photo() == null || message.photo().size == 0) {
            log.warn { "PhotoHandleRegistry.dispatch called, but no photo in message" }
            return null
        }

        val method = handlerMap.values
            .map { it.method }
            .firstOrNull { m ->
                val template = m?.getAnnotation(PhotoHandle::class.java)?.value
                    ?: throw IllegalArgumentException("Template must not be null")
                template == "update_photo"
            }

        val chat = message.chat()
        val from = message.from()

        saveOrUpdateUser(from, chat)

        val template = method?.getAnnotation(PhotoHandle::class.java)?.value
            ?: throw IllegalArgumentException("PhotoHandle did not return a photo")

        val ctx = HandlerCtx(
            update,
            chat,
            from,
            0,
            null
        )
        val args = handlerArgumentResolver.resolve(method, ctx)
        return invokeWithCatch(from, method, handlerMap[template]!!.bean, args, chat)
    }

    override fun canHandle(update: Update): Boolean {
        return update.message() != null && update.message().photo() != null && update.message().photo().size > 0
    }
}