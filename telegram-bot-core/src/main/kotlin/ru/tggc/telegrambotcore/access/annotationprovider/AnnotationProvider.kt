package ru.tggc.telegrambotcore.access.annotationprovider

import ru.tggc.telegrambotcore.annotation.handle.HandleMeta
import java.lang.reflect.Method

interface AnnotationProvider {
    fun supports(m: Method): Boolean

    fun extractMeta(m: Method): HandleMeta?
}
