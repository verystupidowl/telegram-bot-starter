package ru.tggc.telegrambotcore.registry.resolver;

import org.springframework.stereotype.Component;
import ru.tggc.telegrambotcore.annotation.params.MessageId;

import java.lang.reflect.Parameter;

@Component
public class MessageIdResolver implements ParameterResolver<Integer> {

    @Override
    public boolean supports(Parameter p) {
        return p.isAnnotationPresent(MessageId.class);
    }

    @Override
    public Integer resolve(Parameter parameter, HandlerCtx ctx) {
        return ctx.messageId();
    }
}
