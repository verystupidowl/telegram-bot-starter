package ru.tggc.telegrambotcore.registry

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.Chat
import com.pengrad.telegrambot.model.User
import jakarta.annotation.PostConstruct
import lombok.extern.slf4j.Slf4j
import ru.tggc.telegrambotcore.access.checker.GlobalAccessChecker
import ru.tggc.telegrambotcore.annotation.handle.BotHandler
import ru.tggc.telegrambotcore.dto.ChatDto
import ru.tggc.telegrambotcore.dto.Response
import ru.tggc.telegrambotcore.dto.UserDto
import ru.tggc.telegrambotcore.exception.ExceptionHandler
import ru.tggc.telegrambotcore.service.UserRateLimiterService
import ru.tggc.telegrambotcore.service.UserService
import java.lang.reflect.Method
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

@Slf4j
abstract class AbstractHandleRegistry(
    private val handlerScanner: HandlerScanner,
    private val rateLimiter: UserRateLimiterService,
    private val exceptionHandler: ExceptionHandler,
    private val globalAccessChecker: GlobalAccessChecker,
    private val userService: UserService
) : HandleRegistry {
    protected val handlerMap: MutableMap<String, RegisteredHandler> = ConcurrentHashMap()

    protected var defaultMethod: Method? = null
    protected var defaultBean: Any? = null

    @PostConstruct
    fun init() {
        val data = handlerScanner.scan(this.handleAnnotation, BotHandler::class.java)

        handlerMap.putAll(
            data?.registeredHandlers
                ?: throw IllegalStateException("Handler not registered for ${this.handleAnnotation}")
        )
        defaultMethod = data.defaultMethod
        defaultBean = data.defaultBean
    }

    protected fun invokeWithCatch(from: User, method: Method, bean: Any?, args: Array<Any?>, chat: Chat): Response? {
        val checkedRequest = globalAccessChecker.check(from, method, chat)
        if (checkedRequest != null) {
            return checkedRequest
        }
        rateLimiter.lock(from.id())
        try {
            val response = method.invoke(bean, *args) as Response?
            return response?.andThen { _: TelegramBot ->
                rateLimiter.unlock(from.id())
                CompletableFuture.completedFuture<Void>(null)
            }
        } catch (e: Exception) {
            return exceptionHandler.handleException(e, chat, from)
                .andThen { _: TelegramBot ->
                    rateLimiter.unlock(from.id())
                    CompletableFuture.completedFuture<Void>(null)
                }
        }
    }

    protected fun saveOrUpdateUser(from: User, chat: Chat) {
        val userDto = UserDto(from.id(), from.username())
        val chatDto = ChatDto(chat.id(), chat.title())
        userService.saveOrUpdate(userDto, chatDto)
    }

    protected abstract val handleAnnotation: Class<out Annotation?>?

    companion object {
        protected const val NOT_IMPLEMENTED_MESSAGE: String = "Пока не реализовано, следите за новостями!"
        protected const val ADMIN_ID: Long = 428873987
    }
}
