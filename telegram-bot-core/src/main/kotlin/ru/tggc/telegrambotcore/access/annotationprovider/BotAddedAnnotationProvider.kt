package ru.tggc.telegrambotcore.access.annotationprovider

import org.springframework.stereotype.Component
import ru.tggc.telegrambotcore.annotation.handle.BotAddedHandle
import ru.tggc.telegrambotcore.annotation.handle.HandleMeta
import java.lang.reflect.Method

@Component
class BotAddedAnnotationProvider : AnnotationProvider {
    override fun supports(m: Method): Boolean = m.isAnnotationPresent(BotAddedHandle::class.java)

    override fun extractMeta(m: Method): HandleMeta? = HandleMeta.fromDefault()
}
