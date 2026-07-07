package ru.tggc.telegrambotcore.exception;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.User;
import ru.tggc.telegrambotcore.dto.Response;

public interface ExceptionHandler {

    Response handleException(Exception e, Chat chat, User from);

    String buildMessageToAdmin(String s, Chat chat, User from);
}
