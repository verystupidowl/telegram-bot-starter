package ru.tggc.telegrambotcore.access.annotationprovider;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.tggc.telegrambotcore.annotation.handle.HandleMeta;

import java.lang.reflect.Method;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AnnotationProviderFactory {
    private final List<AnnotationProvider> annotationProviders;

    public HandleMeta getAnnotationMeta(Method method) {
        return annotationProviders.stream()
                .filter(ap -> ap.supports(method))
                .findFirst()
                .map(ap -> ap.extractMeta(method))
                .orElseThrow(() -> new RuntimeException("No annotation provider found for method " + method.getName()));
    }
}
