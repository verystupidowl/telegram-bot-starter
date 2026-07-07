package ru.tggc.telegrambotcore.access.annotationprovider;

import org.springframework.stereotype.Component;
import ru.tggc.telegrambotcore.annotation.handle.BotAddedHandle;
import ru.tggc.telegrambotcore.annotation.handle.HandleMeta;

import java.lang.reflect.Method;

@Component
public class BotAddedAnnotationProvider implements AnnotationProvider {

    @Override
    public boolean supports(Method m) {
        return m.isAnnotationPresent(BotAddedHandle.class);
    }

    @Override
    public HandleMeta extractMeta(Method m) {
        return HandleMeta.fromDefault();
    }
}
