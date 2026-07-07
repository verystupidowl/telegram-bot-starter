package ru.tggc.telegrambotcore.registry.resolver;

import org.springframework.stereotype.Component;
import ru.tggc.telegrambotcore.annotation.params.Ctx;
import ru.tggc.telegrambotcore.dto.UpdateContext;

import java.lang.reflect.Parameter;

@Component
public class CtxResolver implements ParameterResolver<UpdateContext> {

    @Override
    public boolean supports(Parameter p) {
        return p.isAnnotationPresent(Ctx.class);
    }

    @Override
    public UpdateContext resolve(Parameter parameter, HandlerCtx ctx) {
        return new UpdateContext(
                ctx.chat().id(),
                ctx.from().id(),
                ctx.messageId()
        );
    }
}
