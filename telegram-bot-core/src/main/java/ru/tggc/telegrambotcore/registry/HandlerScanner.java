package ru.tggc.telegrambotcore.registry;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.stereotype.Component;
import ru.tggc.telegrambotcore.annotation.handle.DefaultMessageHandle;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Component
@Slf4j
@RequiredArgsConstructor
public class HandlerScanner {
    private final ListableBeanFactory beanFactory;

    @SneakyThrows
    public HandlerRegistryData scan(Class<? extends Annotation> annotationClass, Class<? extends Annotation> handlerClass) {
        if (annotationClass == null) {
            return null;
        }
        Map<String, RegisteredHandler> handlers = new ConcurrentHashMap<>();

        Method defaultMethod = null;
        Object defaultBean = null;

        Map<String, Object> handlerBeans = beanFactory.getBeansWithAnnotation(handlerClass);

        for (Object bean : handlerBeans.values()) {
            for (Method method : bean.getClass().getDeclaredMethods()) {
                if (method.isAnnotationPresent(annotationClass)) {
                    RegisteredHandler handler = new RegisteredHandler();
                    Annotation ann = method.getAnnotation(annotationClass);

                    String key = (String) ann.annotationType()
                            .getMethod("value")
                            .invoke(ann);

                    handler.setMethod(method);
                    handler.setBean(bean);

                    if (key.contains("${")) {
                        String regex = key.replaceAll("\\$\\{(\\w+)}", "(?<$1>\\\\S+)");
                        handler.setPattern(Pattern.compile(regex));
                    }

                    log.info("Registered handler '{}' -> {}.{}",
                            key,
                            bean.getClass().getSimpleName(),
                            method.getName()
                    );

                    handlers.put(key, handler);
                }

                if (method.isAnnotationPresent(DefaultMessageHandle.class)) {
                    if (defaultMethod != null) {
                        throw new IllegalStateException("Должен быть только один @DefaultMessageHandle");
                    }

                    defaultMethod = method;
                    defaultBean = bean;
                }
            }
        }

        return new HandlerRegistryData(
                handlers,
                defaultMethod,
                defaultBean
        );
    }
}
