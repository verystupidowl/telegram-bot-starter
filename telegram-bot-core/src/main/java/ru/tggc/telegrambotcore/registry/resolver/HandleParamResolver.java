package ru.tggc.telegrambotcore.registry.resolver;

import org.springframework.stereotype.Component;
import ru.tggc.telegrambotcore.annotation.params.HandleParam;
import ru.tggc.telegrambotcore.util.ParamConverter;

import java.lang.reflect.Parameter;
import java.util.regex.Matcher;

@Component
public class HandleParamResolver implements ParameterResolver<Object> {

    @Override
    public boolean supports(Parameter p) {
        return p.isAnnotationPresent(HandleParam.class);
    }

    @Override
    public Object resolve(Parameter parameter, HandlerCtx ctx) {
        Matcher matcher = ctx.matcher();

        if (matcher == null || !matcher.matches()) {
            return null;
        }

        String name = parameter.getAnnotation(HandleParam.class).value();

        String raw = matcher.group(name);

        return ParamConverter.convert(
                raw,
                parameter.getType()
        );
    }
}
