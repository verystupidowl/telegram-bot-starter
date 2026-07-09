package ru.tggc.telegrambotcore.access.annotationprovider

import org.springframework.stereotype.Component
import ru.tggc.telegrambotcore.annotation.handle.DefaultMessageHandle
import ru.tggc.telegrambotcore.annotation.handle.HandleMeta
import java.lang.reflect.Method

@Component
class DefaultMessageProvider : AnnotationProvider {
    override fun supports(m: Method): Boolean = m.isAnnotationPresent(DefaultMessageHandle::class.java)


    override fun extractMeta(m: Method): HandleMeta? = HandleMeta.fromDefault()

}
