package ru.tggc.telegrambotcore.registry.resolver;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;

import java.util.regex.Matcher;

public record HandlerCtx(
        Update update,
        Chat chat,
        User from,
        int messageId,
        Matcher matcher
) {
}
