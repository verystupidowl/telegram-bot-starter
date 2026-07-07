package ru.tggc.telegrambotcore.router;

import com.pengrad.telegrambot.model.Update;
import ru.tggc.telegrambotcore.dto.Response;

public interface TelegramUpdateRouter {

    Response route(Update update);
}
