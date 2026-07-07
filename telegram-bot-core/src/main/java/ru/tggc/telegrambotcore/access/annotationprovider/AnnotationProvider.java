package ru.tggc.telegrambotcore.access.annotationprovider;

import ru.tggc.telegrambotcore.annotation.handle.HandleMeta;

import java.lang.reflect.Method;

public interface AnnotationProvider {

    boolean supports(Method m);

    HandleMeta extractMeta(Method m);
}
