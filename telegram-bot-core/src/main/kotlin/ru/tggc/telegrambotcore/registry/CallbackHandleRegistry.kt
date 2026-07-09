package ru.tggc.telegrambotcore.registry

import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.request.AnswerCallbackQuery
import com.pengrad.telegrambot.request.SendMessage
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import ru.tggc.telegrambotcore.access.checker.GlobalAccessChecker
import ru.tggc.telegrambotcore.annotation.handle.CallbackHandle
import ru.tggc.telegrambotcore.dto.Response
import ru.tggc.telegrambotcore.exception.ExceptionHandler
import ru.tggc.telegrambotcore.registry.resolver.HandlerArgumentResolver
import ru.tggc.telegrambotcore.registry.resolver.HandlerCtx
import ru.tggc.telegrambotcore.service.TelegramBotSender
import ru.tggc.telegrambotcore.service.UserRateLimiterService
import ru.tggc.telegrambotcore.service.UserService
import java.lang.reflect.Method

@Component
class CallbackHandleRegistry(
    handlerScanner: HandlerScanner,
    rateLimiter: UserRateLimiterService,
    private val exceptionHandler: ExceptionHandler,
    globalAccessChecker: GlobalAccessChecker,
    private val handlerArgumentResolver: HandlerArgumentResolver,
    private val telegramBotSender: TelegramBotSender,
    userService: UserService
) : AbstractHandleRegistry(
    handlerScanner, rateLimiter,
    exceptionHandler, globalAccessChecker, userService
) {
    private val log = KotlinLogging.logger {}

    override val handleAnnotation: Class<out Annotation?>?
        get() = CallbackHandle::class.java

    override fun dispatch(update: Update): Response? {
        val query = update.callbackQuery()
        telegramBotSender.send(Response.of(AnswerCallbackQuery(query.id())))

        val data = query.data()
        val method = handlerMap.values
            .map { it.method }
            .firstOrNull { m: Method? ->
                val template = m?.getAnnotation(CallbackHandle::class.java)?.value
                if (template == data) return@firstOrNull true
                val p = handlerMap[template]!!.pattern
                p != null && p.matcher(data).matches()
            }
        val chat = query.maybeInaccessibleMessage().chat()
        val from = query.from()
        val chatId = chat.id()
        val messageId = query.maybeInaccessibleMessage().messageId()

        saveOrUpdateUser(from, chat)

        if (method == null) {
            log.warn { "Unknown callback: $data" }
            val message = exceptionHandler.buildMessageToAdmin("Unknown callback: $data", chat, from)
            val sendMessageToUser = SendMessage(chatId, NOT_IMPLEMENTED_MESSAGE)
            val sendMessageToAdmin = SendMessage(ADMIN_ID, message)
            return Response.ofAll(sendMessageToAdmin, sendMessageToUser)
        }
        log.info { "callback ${query.data()} from ${from.username()}" }

        val template = method.getAnnotation(CallbackHandle::class.java)!!.value
        val matcher = handlerMap[template]?.pattern?.matcher(data)

        val ctx = HandlerCtx(
            update,
            chat,
            from,
            messageId,
            matcher
        )
        val args = handlerArgumentResolver.resolve(method, ctx)
        return invokeWithCatch(from, method, handlerMap[template]?.bean, args, chat)
    }

    override fun canHandle(update: Update): Boolean {
        return update.callbackQuery() != null
    }
}
