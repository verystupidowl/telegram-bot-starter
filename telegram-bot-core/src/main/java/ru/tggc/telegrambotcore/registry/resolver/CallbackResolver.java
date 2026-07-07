package ru.tggc.telegrambotcore.registry.resolver;

import com.pengrad.telegrambot.model.CallbackQuery;
import org.springframework.stereotype.Component;

import java.lang.reflect.Parameter;

@Component
public class CallbackResolver implements ParameterResolver<CallbackQuery> {

    @Override
    public boolean supports(Parameter p) {
        return p.getType().equals(CallbackQuery.class);
    }

    @Override
    public CallbackQuery resolve(Parameter parameter, HandlerCtx ctx) {
        return ctx.update().callbackQuery();
    }
}
