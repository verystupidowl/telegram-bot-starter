package ru.tggc.telegrambotcore.registry.resolver;

import org.springframework.stereotype.Component;
import ru.tggc.telegrambotcore.annotation.params.UserId;

import java.lang.reflect.Parameter;

@Component
public class UserIdResolver implements ParameterResolver<Long> {

    @Override
    public boolean supports(Parameter p) {
        return p.isAnnotationPresent(UserId.class);
    }

    @Override
    public Long resolve(Parameter parameter, HandlerCtx ctx) {
        return ctx.from().id();
    }
}
