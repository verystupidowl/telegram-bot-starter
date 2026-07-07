package ru.tggc.telegrambotcore.access.checker;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.User;
import ru.tggc.telegrambotcore.dto.AccessResult;

import java.lang.reflect.Method;

public interface AccessChecker {

    AccessResult check(User from, Method method, Chat chat);
}
