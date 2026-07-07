package ru.tggc.telegrambotcore.registry.resolver;

import com.pengrad.telegrambot.model.Update;
import org.springframework.stereotype.Component;

import java.lang.reflect.Parameter;

@Component
public class UpdateResolver implements ParameterResolver<Update> {

    @Override
    public boolean supports(Parameter p) {
        return p.getType().equals(Update.class);
    }

    @Override
    public Update resolve(Parameter parameter, HandlerCtx ctx) {
        return ctx.update();
    }
}
