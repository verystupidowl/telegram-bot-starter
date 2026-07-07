package ru.tggc.telegrambotcore.registry;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.tggc.telegrambotcore.access.checker.GlobalAccessChecker;
import ru.tggc.telegrambotcore.annotation.handle.PhotoHandle;
import ru.tggc.telegrambotcore.dto.Response;
import ru.tggc.telegrambotcore.exception.ExceptionHandler;
import ru.tggc.telegrambotcore.registry.resolver.HandlerArgumentResolver;
import ru.tggc.telegrambotcore.registry.resolver.HandlerCtx;
import ru.tggc.telegrambotcore.service.UserRateLimiterService;
import ru.tggc.telegrambotcore.service.UserService;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static ru.tggc.telegrambotcore.util.Utils.throwIfNull;

@Component
@Slf4j
public class PhotoHandleRegistry extends AbstractHandleRegistry {
    private final HandlerArgumentResolver handlerArgumentResolver;

    public PhotoHandleRegistry(HandlerScanner handlerScanner,
                               UserRateLimiterService rateLimiter,
                               ExceptionHandler exceptionHandler,
                               GlobalAccessChecker globalAccessChecker,
                               UserService userService,
                               HandlerArgumentResolver handlerArgumentResolver) {
        super(handlerScanner, rateLimiter, exceptionHandler, globalAccessChecker, userService);
        this.handlerArgumentResolver = handlerArgumentResolver;
    }

    @Override
    protected Class<? extends Annotation> getHandleAnnotation() {
        return PhotoHandle.class;
    }

    @Override
    public Response dispatch(Update update) {
        Message message = update.message();

        if (message.photo() == null || message.photo().length == 0) {
            log.warn("PhotoHandleRegistry.dispatch called, but no photo in message");
            return null;
        }

        Method method = handlerMap.values().stream()
                .map(RegisteredHandler::getMethod)
                .filter(m -> {
                    String template = m.getAnnotation(PhotoHandle.class).value();
                    return template.equals("update_photo");
                })
                .findFirst()
                .orElse(null);

        Chat chat = message.chat();
        User from = message.from();

        saveOrUpdateUser(from, chat);

        throwIfNull(method, IllegalStateException::new);

        String template = method.getAnnotation(PhotoHandle.class).value();

        HandlerCtx ctx = new HandlerCtx(
                update,
                chat,
                from,
                0,
                null
        );
        Object[] args = handlerArgumentResolver.resolve(method, ctx);
        return invokeWithCatch(from, method, handlerMap.get(template).getBean(), args, chat);
    }

    @Override
    public boolean canHandle(Update update) {
        return update.message() != null
                && update.message().photo() != null
                && update.message().photo().length > 0;
    }
}