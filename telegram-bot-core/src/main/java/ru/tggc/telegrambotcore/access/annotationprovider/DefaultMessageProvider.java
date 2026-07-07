package ru.tggc.telegrambotcore.access.annotationprovider;

import org.springframework.stereotype.Component;
import ru.tggc.telegrambotcore.annotation.handle.DefaultMessageHandle;
import ru.tggc.telegrambotcore.annotation.handle.HandleMeta;

import java.lang.reflect.Method;

@Component
public class DefaultMessageProvider implements AnnotationProvider {

    @Override
    public boolean supports(Method m) {
        return m.isAnnotationPresent(DefaultMessageHandle.class);
    }

    @Override
    public HandleMeta extractMeta(Method m) {
        return HandleMeta.fromDefault();
    }
}
