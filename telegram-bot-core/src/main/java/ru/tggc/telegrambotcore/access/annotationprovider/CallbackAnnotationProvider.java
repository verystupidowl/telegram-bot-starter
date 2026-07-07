package ru.tggc.telegrambotcore.access.annotationprovider;

import org.springframework.stereotype.Component;
import ru.tggc.telegrambotcore.annotation.handle.CallbackHandle;
import ru.tggc.telegrambotcore.annotation.handle.HandleMeta;

import java.lang.reflect.Method;

@Component
public class CallbackAnnotationProvider implements AnnotationProvider {

    @Override
    public boolean supports(Method m) {
        return m.isAnnotationPresent(CallbackHandle.class);
    }

    @Override
    public HandleMeta extractMeta(Method m) {
        return HandleMeta.from(m.getAnnotation(CallbackHandle.class));
    }
}
