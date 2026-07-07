package ru.tggc.telegrambotcore.access.checker;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.tggc.telegrambotcore.access.annotationprovider.AnnotationProviderFactory;
import ru.tggc.telegrambotcore.annotation.handle.HandleMeta;
import ru.tggc.telegrambotcore.dto.AccessResult;
import ru.tggc.telegrambotcore.dto.Response;

import java.lang.reflect.Method;

@Component
@Order(2)
@RequiredArgsConstructor
public class ChatTypeAccessChecker implements AccessChecker {
    private final AnnotationProviderFactory factory;

    @Override
    public AccessResult check(User from, Method method, Chat chat) {
        HandleMeta annotationMeta = factory.getAnnotationMeta(method);

        boolean isPrivateMessage = chat.type() == Chat.Type.Private;

        boolean canRequestBePrivate = annotationMeta.canPrivate();
        boolean canRequestBePublic = annotationMeta.canPublic();
        if ((isPrivateMessage && canRequestBePrivate) || (!isPrivateMessage && canRequestBePublic)) {
            return AccessResult.allow();
        }
        return AccessResult.deny(Response.empty());
    }
}
