package ru.tggc.telegrambotcore.registry;

import com.pengrad.telegrambot.model.Update;
import ru.tggc.telegrambotcore.dto.Response;

public interface HandleRegistry {

    Response dispatch(Update update);

    boolean canHandle(Update update);
}
