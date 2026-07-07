package ru.tggc.telegrambotcore.access.checker;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.tggc.telegrambotcore.access.annotationprovider.AnnotationProviderFactory;
import ru.tggc.telegrambotcore.annotation.handle.HandleMeta;
import ru.tggc.telegrambotcore.dto.AccessResult;
import ru.tggc.telegrambotcore.dto.Response;
import ru.tggc.telegrambotcore.dto.UserRole;
import ru.tggc.telegrambotcore.service.UserService;

import java.lang.reflect.Method;

@Component
@Order(1)
@RequiredArgsConstructor
public class RoleAccessChecker implements AccessChecker {
    private final UserService userService;
    private final AnnotationProviderFactory factory;

    @Override
    public AccessResult check(User from, Method method, Chat chat) {
        HandleMeta annotationMeta = factory.getAnnotationMeta(method);
        UserRole[] roles = annotationMeta.requiredRoles();

        if (roles.length == 0) {
            return AccessResult.allow();
        }

        return userService.checkRoles(from.id(), roles)
                ? AccessResult.allow()
                : AccessResult.deny(Response.of(new SendMessage(chat.id().longValue(), "У вас недостаточно прав для это команды")));
    }
}
