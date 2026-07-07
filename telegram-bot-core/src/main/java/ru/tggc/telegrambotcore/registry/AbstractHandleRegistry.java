package ru.tggc.telegrambotcore.registry;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.User;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.tggc.telegrambotcore.access.checker.GlobalAccessChecker;
import ru.tggc.telegrambotcore.annotation.handle.BotHandler;
import ru.tggc.telegrambotcore.dto.ChatDto;
import ru.tggc.telegrambotcore.dto.Response;
import ru.tggc.telegrambotcore.dto.UserDto;
import ru.tggc.telegrambotcore.exception.ExceptionHandler;
import ru.tggc.telegrambotcore.service.UserRateLimiterService;
import ru.tggc.telegrambotcore.service.UserService;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractHandleRegistry implements HandleRegistry {
    protected Map<String, RegisteredHandler> handlerMap = new ConcurrentHashMap<>();

    private final HandlerScanner handlerScanner;
    private final UserRateLimiterService rateLimiter;
    private final ExceptionHandler exceptionHandler;
    private final GlobalAccessChecker globalAccessChecker;
    private final UserService userService;

    protected Method defaultMethod;
    protected Object defaultBean;

    protected static final String NOT_IMPLEMENTED_MESSAGE = "Пока не реализовано, следите за новостями!";
    protected static final long ADMIN_ID = 428873987;

    @PostConstruct
    public void init() {
        HandlerRegistryData data = handlerScanner.scan(getHandleAnnotation(), BotHandler.class);

        handlerMap.putAll(data.registeredHandlers());
        defaultMethod = data.defaultMethod();
        defaultBean = data.defaultBean();
    }

    protected Response invokeWithCatch(User from, Method method, Object bean, Object[] args, Chat chat) {
        Response response;
        Response checkedRequest = globalAccessChecker.check(from, method, chat);
        if (checkedRequest != null) {
            return checkedRequest;
        }
        rateLimiter.lock(from.id());
        try {
            response = (Response) method.invoke(bean, args);
            return response.andThen(_ -> rateLimiter.unlock(from.id()));
        } catch (Exception e) {
            return exceptionHandler.handleException(e, chat, from)
                    .andThen(_ -> rateLimiter.unlock(from.id()));
        }
    }

    protected void saveOrUpdateUser(User from, Chat chat) {
        UserDto userDto = new UserDto(from.id(), from.username());
        ChatDto chatDto = new ChatDto(chat.id(), chat.title());
        userService.saveOrUpdate(userDto, chatDto);
    }

    protected abstract Class<? extends Annotation> getHandleAnnotation();
}
