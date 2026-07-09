package ru.tggc.telegrambotcore.access.annotationprovider

import org.springframework.stereotype.Component
import ru.tggc.telegrambotcore.annotation.handle.CallbackHandle
import ru.tggc.telegrambotcore.annotation.handle.HandleMeta
import java.lang.reflect.Method

@Component
class CallbackAnnotationProvider : AnnotationProvider {
    override fun supports(m: Method): Boolean = m.isAnnotationPresent(CallbackHandle::class.java)


    override fun extractMeta(m: Method): HandleMeta? =
        HandleMeta.from(m.getAnnotation(CallbackHandle::class.java))

}
