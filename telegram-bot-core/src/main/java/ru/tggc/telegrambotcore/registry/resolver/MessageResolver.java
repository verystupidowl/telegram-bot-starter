package ru.tggc.telegrambotcore.registry.resolver;

import com.pengrad.telegrambot.model.Message;
import org.springframework.stereotype.Component;

import java.lang.reflect.Parameter;

@Component
public class MessageResolver implements ParameterResolver<Message> {

    @Override
    public boolean supports(Parameter p) {
        return p.getType().equals(Message.class);
    }

    @Override
    public Message resolve(Parameter parameter, HandlerCtx ctx) {
        return ctx.update().message();
    }
}
