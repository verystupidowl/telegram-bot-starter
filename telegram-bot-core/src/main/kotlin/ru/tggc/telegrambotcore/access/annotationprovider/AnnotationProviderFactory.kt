package ru.tggc.telegrambotcore.access.annotationprovider

import org.springframework.stereotype.Component
import ru.tggc.telegrambotcore.annotation.handle.HandleMeta
import java.lang.reflect.Method

@Component
class AnnotationProviderFactory(private val annotationProviders: MutableList<AnnotationProvider>) {
    fun getAnnotationMeta(method: Method): HandleMeta =
        annotationProviders.firstOrNull { it.supports(method) }
            ?.let { ap: AnnotationProvider -> ap.extractMeta(method) }
            ?: throw RuntimeException("AnnotationProvider not found for method: $method")
}
