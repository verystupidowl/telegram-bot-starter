package ru.tggc.telegrambotcore.formatter

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.core.io.ResourceLoader
import org.yaml.snakeyaml.Yaml

@Suppress("UNCHECKED_CAST")
class MessageLoader(private val resourceLoader: ResourceLoader) {
    private val log = KotlinLogging.logger {}

    fun load(baseNames: List<String>): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        baseNames.forEach { basename ->
            val resource = resourceLoader.getResource("classpath:$basename.yml")
            log.info { "Loading $resource" }
            resource.inputStream.use { input ->
                val loaded = Yaml().load<Map<String, Any>>(input)
                merge(result, loaded)
            }
        }

        return result
    }


    private fun merge(target: MutableMap<String, Any>, source: Map<String, Any>) {
        source.forEach { (key, value) ->
            if (value is Map<*, *> && target[key] is Map<*, *>) {
                merge(target[key] as MutableMap<String, Any>, value as Map<String, Any>)
            } else {
                target[key] = value
            }
        }
    }
}