package ru.tggc.telegrambotcore.router;

import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.tggc.telegrambotcore.dto.Response;
import ru.tggc.telegrambotcore.registry.HandleRegistry;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramUpdateRouterImpl implements TelegramUpdateRouter {
    private final List<HandleRegistry> handlers;

    @Override
    public Response route(Update update) {
        return handlers.stream()
                .filter(handler -> handler.canHandle(update))
                .findFirst()
                .map(registry -> registry.dispatch(update))
                .orElse(Response.empty());
    }
}
