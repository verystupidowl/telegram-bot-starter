package ru.tggc.telegrambotcore.registry.resolver;

import org.springframework.stereotype.Component;
import ru.tggc.telegrambotcore.annotation.params.Username;

import java.lang.reflect.Parameter;

@Component
public class UsernameResolver implements ParameterResolver<String> {

    @Override
    public boolean supports(Parameter p) {
        return p.isAnnotationPresent(Username.class);
    }

    @Override
    public String resolve(Parameter parameter, HandlerCtx ctx) {
        return ctx.from().username();
    }
}
