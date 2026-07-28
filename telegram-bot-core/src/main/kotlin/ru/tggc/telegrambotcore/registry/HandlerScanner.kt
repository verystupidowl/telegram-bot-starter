package ru.tggc.telegrambotcore.registry

import io.github.oshai.kotlinlogging.KotlinLogging
import lombok.SneakyThrows
import org.springframework.beans.factory.ListableBeanFactory
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern

@Component
class HandlerScanner(private val beanFactory: ListableBeanFactory) {
    private val log = KotlinLogging.logger {}

    @SneakyThrows
    fun scan(annotationClass: Class<out Annotation>?, handlerClass: Class<out Annotation?>): HandlerRegistryData? {
        if (annotationClass == null) {
            return null
        }
        val handlers: MutableMap<String, RegisteredHandler> = ConcurrentHashMap()

        val handlerBeans: MutableMap<String, Any> = beanFactory.getBeansWithAnnotation(handlerClass)

        for (bean in handlerBeans.values) {
            for (method in bean.javaClass.declaredMethods) {
                if (method.isAnnotationPresent(annotationClass)) {
                    val handler = RegisteredHandler()
                    val ann: Annotation = method.getAnnotation(annotationClass)

                    val key = ann.annotationClass.java
                        .getMethod("value")
                        .invoke(ann) as String

                    handler.method = method
                    handler.bean = bean

                    if (key.contains($$"${")) {
                        val regex = key.replace("\\$\\{(\\w+)}".toRegex(), "(?<$1>\\\\S+)")
                        handler.pattern = Pattern.compile(regex)
                    }

                    log.info { "Registered handler '$key' -> ${bean.javaClass.simpleName}.${method.name}" }

                    handlers[key] = handler
                }
            }
        }

        return HandlerRegistryData(handlers)
    }
}
