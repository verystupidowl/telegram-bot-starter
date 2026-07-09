package ru.tggc.telegrambotcore.access.annotationprovider

import org.springframework.stereotype.Component
import ru.tggc.telegrambotcore.annotation.handle.HandleMeta
import ru.tggc.telegrambotcore.annotation.handle.PhotoHandle
import java.lang.reflect.Method

@Component
class PhotoAnnotationProvider : AnnotationProvider {
    override fun supports(m: Method): Boolean = m.isAnnotationPresent(PhotoHandle::class.java)


    override fun extractMeta(m: Method): HandleMeta? =
        HandleMeta.from(m.getAnnotation(PhotoHandle::class.java))

}
