package ru.tggc.telegrambotcore.access.annotationprovider

import org.springframework.stereotype.Component
import ru.tggc.telegrambotcore.annotation.handle.CommandHandle
import ru.tggc.telegrambotcore.annotation.handle.HandleMeta
import java.lang.reflect.Method

@Component
class CommandAnnotationProvider : AnnotationProvider {
    override fun supports(m: Method): Boolean = m.isAnnotationPresent(CommandHandle::class.java)


    override fun extractMeta(m: Method): HandleMeta? =
        HandleMeta.from(m.getAnnotation(CommandHandle::class.java))
}
