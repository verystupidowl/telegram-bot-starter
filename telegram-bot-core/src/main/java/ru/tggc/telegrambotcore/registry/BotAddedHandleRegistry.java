package ru.tggc.telegrambotcore.registry;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.tggc.telegrambotcore.access.checker.GlobalAccessChecker;
import ru.tggc.telegrambotcore.annotation.handle.BotAddedHandle;
import ru.tggc.telegrambotcore.dto.Response;
import ru.tggc.telegrambotcore.exception.ExceptionHandler;
import ru.tggc.telegrambotcore.registry.resolver.HandlerArgumentResolver;
import ru.tggc.telegrambotcore.registry.resolver.HandlerCtx;
import ru.tggc.telegrambotcore.service.UserRateLimiterService;
import ru.tggc.telegrambotcore.service.UserService;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

@Component
@Slf4j
public class BotAddedHandleRegistry extends AbstractHandleRegistry {
    @Value("${telegram.bot-id}")
    private long botId;

    private final HandlerArgumentResolver handlerArgumentResolver;

    public BotAddedHandleRegistry(HandlerScanner handlerScanner,
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
        return BotAddedHandle.class;
    }

    @Override
    public Response dispatch(Update update) {
        Message message = update.message();

        Method method = handlerMap.values().stream()
                .map(RegisteredHandler::getMethod)
                .filter(m -> {
                    String template = m.getAnnotation(BotAddedHandle.class).value();
                    return template.equals("bot_added");
                })
                .findFirst()
                .orElse(null);
        Chat chat = message.chat();
        User from = message.from();
        int messageId = message.messageId();

        if (method == null) {
            throw new RuntimeException("method is null");
        }

        saveOrUpdateUser(from, chat);

        HandlerCtx ctx = new HandlerCtx(
                update,
                chat,
                from,
                messageId,
                null
        );
        Object[] args = handlerArgumentResolver.resolve(method, ctx);
        return invokeWithCatch(from, method, handlerMap.get("bot_added").getBean(), args, chat);
    }

    @Override
    public boolean canHandle(Update update) {
        return Stream.ofNullable(update.message())
                .map(Message::newChatMembers)
                .filter(Objects::nonNull)
                .flatMap(Arrays::stream)
                .anyMatch(member -> member.id() == botId);
    }
}
