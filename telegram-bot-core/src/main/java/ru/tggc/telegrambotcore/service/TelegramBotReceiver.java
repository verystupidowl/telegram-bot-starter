package ru.tggc.telegrambotcore.service;

import com.pengrad.telegrambot.model.Update;

public interface TelegramBotReceiver {

    void receiveUpdate(Update update);
}
