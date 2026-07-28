package ru.tggc.telegrambotcore.access.annotationprovider

import org.springframework.stereotype.Component
import ru.tggc.telegrambotcore.annotation.handle.HandleMeta
import ru.tggc.telegrambotcore.annotation.handle.TextHandle
import java.lang.reflect.Method

@Component
class TextMessageProvider : AnnotationProvider {
    override fun supports(m: Method): Boolean = m.isAnnotationPresent(TextHandle::class.java)


    override fun extractMeta(m: Method): HandleMeta =
        HandleMeta.from(m.getAnnotation(TextHandle::class.java))

}
