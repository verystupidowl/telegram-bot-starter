package ru.tggc.telegrambotcore.service;

import com.pengrad.telegrambot.TelegramBot;
import ru.tggc.telegrambotcore.dto.Response;

import java.util.function.Consumer;

public interface TelegramBotSender {

    void send(Response response);

    void sendDelayed(Consumer<TelegramBot> task, long delayMillis);

    void sendToAdmin(String text);
}
