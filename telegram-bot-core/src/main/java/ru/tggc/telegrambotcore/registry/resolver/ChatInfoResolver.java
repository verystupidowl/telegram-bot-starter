package ru.tggc.telegrambotcore.registry.resolver;

import org.springframework.stereotype.Component;
import ru.tggc.telegrambotcore.annotation.params.ChatInfo;
import ru.tggc.telegrambotcore.dto.ChatDto;

import java.lang.reflect.Parameter;

@Component
public class ChatInfoResolver implements ParameterResolver<ChatDto> {

    @Override
    public boolean supports(Parameter p) {
        return p.isAnnotationPresent(ChatInfo.class) && p.getType().equals(ChatDto.class);
    }

    @Override
    public ChatDto resolve(Parameter parameter, HandlerCtx ctx) {
        return new ChatDto(ctx.chat().id(), ctx.chat().title());
    }
}
