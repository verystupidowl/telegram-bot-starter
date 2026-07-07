package ru.tggc.telegrambotcore.access.annotationprovider;

import org.springframework.stereotype.Component;
import ru.tggc.telegrambotcore.annotation.handle.HandleMeta;
import ru.tggc.telegrambotcore.annotation.handle.MessageHandle;

import java.lang.reflect.Method;

@Component
public class MessageAnnotationProvider implements AnnotationProvider {

    @Override
    public boolean supports(Method m) {
        return m.isAnnotationPresent(MessageHandle.class);
    }

    @Override
    public HandleMeta extractMeta(Method m) {
        return HandleMeta.from(m.getAnnotation(MessageHandle.class));
    }
}
