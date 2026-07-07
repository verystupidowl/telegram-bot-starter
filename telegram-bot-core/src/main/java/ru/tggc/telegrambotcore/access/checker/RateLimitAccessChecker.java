package ru.tggc.telegrambotcore.access.checker;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.tggc.telegrambotcore.dto.AccessResult;
import ru.tggc.telegrambotcore.dto.Response;
import ru.tggc.telegrambotcore.dto.ResponseBuilder;
import ru.tggc.telegrambotcore.service.UserRateLimiterService;

import java.lang.reflect.Method;

@Component
@Order(4)
@RequiredArgsConstructor
public class RateLimitAccessChecker implements AccessChecker {
    private final UserRateLimiterService userRateLimiterService;

    @Override
    public AccessResult check(User from, Method method, Chat chat) {
        return userRateLimiterService.checkRateLimit(from)
                .map(text -> {
                    Response response = ResponseBuilder.to(chat.id())
                            .message(text)
                            .build();
                    return AccessResult.deny(response);
                })
                .orElseGet(AccessResult::allow);
    }
}
