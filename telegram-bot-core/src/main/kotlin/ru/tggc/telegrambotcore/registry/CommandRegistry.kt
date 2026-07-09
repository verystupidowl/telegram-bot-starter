package ru.tggc.telegrambotcore.registry

import com.pengrad.telegrambot.model.MessageEntity
import com.pengrad.telegrambot.model.Update
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import ru.tggc.telegrambotcore.access.checker.GlobalAccessChecker
import ru.tggc.telegrambotcore.annotation.handle.CommandHandle
import ru.tggc.telegrambotcore.dto.Response
import ru.tggc.telegrambotcore.exception.ExceptionHandler
import ru.tggc.telegrambotcore.registry.resolver.HandlerArgumentResolver
import ru.tggc.telegrambotcore.registry.resolver.HandlerCtx
import ru.tggc.telegrambotcore.service.UserRateLimiterService
import ru.tggc.telegrambotcore.service.UserService
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

@Component
class CommandRegistry(
    handlerScanner: HandlerScanner,
    rateLimiter: UserRateLimiterService,
    exceptionHandler: ExceptionHandler,
    globalAccessChecker: GlobalAccessChecker,
    private val handlerArgumentResolver: HandlerArgumentResolver,
    userService: UserService
) : AbstractHandleRegistry(handlerScanner, rateLimiter, exceptionHandler, globalAccessChecker, userService) {
    private val log = KotlinLogging.logger {}

    override fun dispatch(update: Update): Response? {
        val message = update.message()

        if (message.text() == null) {
            return null
        }
        val command: String = extractCommand(message.text().lowercase(Locale.getDefault()))
        val method = handlerMap.values
            .map { it.method }
            .firstOrNull { m ->
                val template = m?.getAnnotation(CommandHandle::class.java)?.value
                if (template?.lowercase() == command) return@firstOrNull true
                val p = handlerMap[template]!!.pattern
                p != null && p.matcher(command).matches()
            } ?: throw RuntimeException("Unknown command: $command")

        val chat = message.chat()
        val from = message.from()

        saveOrUpdateUser(from, chat)
        log.info { "message ${message.text()} from ${from.username()}" }

        val template = method.getAnnotation(CommandHandle::class.java).value
        val matcher = handlerMap[template]?.pattern?.matcher(command)

        val ctx = HandlerCtx(
            update,
            chat,
            from,
            0,
            matcher
        )
        val args = handlerArgumentResolver.resolve(method, ctx)
        return invokeWithCatch(from, method, handlerMap[template]?.bean, args, chat)
    }

    override val handleAnnotation: Class<out Annotation?>?
        get() = CommandHandle::class.java

    override fun canHandle(update: Update): Boolean =
        update.message()?.entities()?.any { it.type() == MessageEntity.Type.bot_command } ?: false


    companion object {
        private val COMMAND_PATTERN: Pattern = Pattern.compile("^/(?<command>[a-zA-Z0-9_]+)(?:@\\w+)?(?:\\s.*)?$")

        fun extractCommand(text: String): String {
            val matcher: Matcher = COMMAND_PATTERN.matcher(text)

            if (matcher.matches()) {
                return matcher.group("command")
            }

            return ""
        }
    }
}
