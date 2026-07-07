package ru.tggc.telegrambotcore.registry;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.MessageEntity;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.tggc.telegrambotcore.access.checker.GlobalAccessChecker;
import ru.tggc.telegrambotcore.annotation.handle.CommandHandle;
import ru.tggc.telegrambotcore.dto.Response;
import ru.tggc.telegrambotcore.exception.ExceptionHandler;
import ru.tggc.telegrambotcore.registry.resolver.HandlerArgumentResolver;
import ru.tggc.telegrambotcore.registry.resolver.HandlerCtx;
import ru.tggc.telegrambotcore.service.UserRateLimiterService;
import ru.tggc.telegrambotcore.service.UserService;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class CommandRegistry extends AbstractHandleRegistry {
    private static final Pattern COMMAND_PATTERN = Pattern.compile("^/(?<command>[a-zA-Z0-9_]+)(?:@\\w+)?(?:\\s.*)?$");

    private final HandlerArgumentResolver handlerArgumentResolver;

    public CommandRegistry(HandlerScanner handlerScanner,
                           UserRateLimiterService rateLimiter,
                           ExceptionHandler exceptionHandler,
                           GlobalAccessChecker globalAccessChecker,
                           HandlerArgumentResolver handlerArgumentResolver,
                           UserService userService) {
        super(handlerScanner, rateLimiter, exceptionHandler, globalAccessChecker, userService);
        this.handlerArgumentResolver = handlerArgumentResolver;
    }

    @Override
    public Response dispatch(Update update) {
        Message message = update.message();

        if (message.text() == null) {
            return null;
        }
        String command = extractCommand(message.text().toLowerCase());
        Method method = handlerMap.values().stream()
                .map(RegisteredHandler::getMethod)
                .filter(m -> {
                    String template = m.getAnnotation(CommandHandle.class).value();
                    if (template.toLowerCase(Locale.ROOT).equals(command)) return true;
                    Pattern p = handlerMap.get(template).getPattern();
                    return p != null && p.matcher(command).matches();
                })
                .findFirst()
                .orElse(null);

        Chat chat = message.chat();
        User from = message.from();

        saveOrUpdateUser(from, chat);

        if (method == null) {
            log.warn("Unknown message: {}", command);
            throw new IllegalArgumentException("Unknown message: " + command);
        }
        log.info("message {} from {}", message.text(), from.username());

        String template = method.getAnnotation(CommandHandle.class).value();
        Matcher matcher = Optional.ofNullable(handlerMap.get(template))
                .map(RegisteredHandler::getPattern)
                .map(p -> p.matcher(command))
                .orElse(null);

        HandlerCtx ctx = new HandlerCtx(
                update,
                chat,
                from,
                0,
                matcher
        );
        Object[] args = handlerArgumentResolver.resolve(method, ctx);
        return invokeWithCatch(from, method, handlerMap.get(template).getBean(), args, chat);
    }

    @Override
    protected Class<? extends Annotation> getHandleAnnotation() {
        return CommandHandle.class;
    }

    @Override
    public boolean canHandle(Update update) {
        return update.message() != null
                && update.message().entities() != null
                && Arrays.stream(update.message().entities())
                .anyMatch(en -> en.type() == MessageEntity.Type.bot_command);
    }

    public static String extractCommand(String text) {
        Matcher matcher = COMMAND_PATTERN.matcher(text);

        if (matcher.matches()) {
            return matcher.group("command");
        }

        return "";
    }
}
