package ru.tggc.telegrambotcore.registry.resolver;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class HandlerArgumentResolver {
    private final List<ParameterResolver<?>> parameterResolvers;

    public Object[] resolve(Method method, HandlerCtx ctx) {
        return Arrays.stream(method.getParameters())
                .map(p -> resolveParameter(p, ctx))
                .toArray();
    }

    private Object resolveParameter(Parameter parameter, HandlerCtx ctx) {
        return parameterResolvers.stream()
                .filter(r -> r.supports(parameter))
                .findFirst()
                .map(r -> r.resolve(parameter, ctx))
                .orElse(null);
    }
}
