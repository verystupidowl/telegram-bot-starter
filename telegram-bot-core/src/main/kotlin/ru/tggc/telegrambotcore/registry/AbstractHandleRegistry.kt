package ru.tggc.telegrambotcore.registry

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.Chat
import com.pengrad.telegrambot.model.User
import com.pengrad.telegrambot.response.BaseResponse
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.future.await
import kotlinx.coroutines.future.future
import lombok.extern.slf4j.Slf4j
import ru.tggc.telegrambotcore.access.checker.GlobalAccessChecker
import ru.tggc.telegrambotcore.annotation.handle.BotHandler
import ru.tggc.telegrambotcore.dto.ChatDto
import ru.tggc.telegrambotcore.dto.CompletionAwareResponse
import ru.tggc.telegrambotcore.dto.asyncResponseScope
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

    @PostConstruct
    fun init() {
        val data = handlerScanner.scan(this.handleAnnotation, BotHandler::class.java)

        handlerMap.putAll(
            data?.registeredHandlers
                ?: throw IllegalStateException("Handler not registered for ${this.handleAnnotation}")
        )
    }

    protected fun invokeWithCatch(from: User, method: Method, bean: Any?, args: Array<Any?>, chat: Chat): Response? {
        val checkedRequest = globalAccessChecker.check(from, method, chat)
        if (checkedRequest != null) {
            return checkedRequest
        }
        rateLimiter.lock(from.id())
        try {
            val response = method.invoke(bean, *args) as Response?
            if (response is CompletionAwareResponse) {
                // The async lease is acquired atomically at execution, before the lazy service call.
                rateLimiter.unlock(from.id())
                return trackAsyncResponse(response, chat, from)
            }
            return response?.andThen { _: TelegramBot ->
                rateLimiter.unlock(from.id())
                CompletableFuture.completedFuture<BaseResponse>(null)
            }
        } catch (e: Exception) {
            return exceptionHandler.handleException(e, chat, from)
                .andThen { _: TelegramBot ->
                    rateLimiter.unlock(from.id())
                    CompletableFuture.completedFuture<BaseResponse>(null)
                }
        }
    }

    private fun trackAsyncResponse(response: CompletionAwareResponse, chat: Chat, from: User): Response =
        CompletionAwareResponse { bot ->
            val lease = rateLimiter.tryAcquireAsync(from.id())
                ?: return@CompletionAwareResponse CompletableFuture.completedFuture(null)
            val completion = asyncResponseScope.future {
                try {
                    response.accept(bot).await()
                } catch (e: Exception) {
                    // Cancellation is terminal, not a reason to send another message.
                    currentCoroutineContext().ensureActive()
                    exceptionHandler.handleException(e, chat, from).accept(bot).await()
                } finally {
                    lease.close()
                }
            }
            // Also release if cancelled before the coroutine gets a chance to start.
            completion.whenComplete { _, _ -> lease.close() }
            completion
        }

    protected fun saveOrUpdateUser(from: User, chat: Chat) {
        val userDto = UserDto(from.id(), from.username(), from.firstName())
        val chatDto = ChatDto(chat.id(), chat.title())
        userService.saveOrUpdate(userDto, chatDto)
    }

    protected abstract val handleAnnotation: Class<out Annotation?>?

    companion object {
        protected const val NOT_IMPLEMENTED_MESSAGE: String = "Пока не реализовано, следите за новостями!"
        protected const val ADMIN_ID: Long = 428873987
    }
}
