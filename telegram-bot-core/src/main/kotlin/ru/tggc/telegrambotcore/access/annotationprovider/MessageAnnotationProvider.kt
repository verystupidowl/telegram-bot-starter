package ru.tggc.telegrambotcore.access.annotationprovider

import org.springframework.stereotype.Component
import ru.tggc.telegrambotcore.annotation.handle.HandleMeta
import ru.tggc.telegrambotcore.annotation.handle.MessageHandle
import java.lang.reflect.Method

@Component
class MessageAnnotationProvider : AnnotationProvider {
    override fun supports(m: Method): Boolean = m.isAnnotationPresent(MessageHandle::class.java)


    override fun extractMeta(m: Method): HandleMeta? =
        HandleMeta.from(m.getAnnotation(MessageHandle::class.java))

}
