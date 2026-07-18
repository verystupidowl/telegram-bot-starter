package ru.tggc.telegrambotcore.messages

import org.springframework.core.io.ResourceLoader
import org.yaml.snakeyaml.Yaml

class MessageLoader(private val resourceLoader: ResourceLoader) {

    fun load(
        baseNames: List<String>
    ): Map<String, Any> {

        val result = mutableMapOf<String, Any>()

        baseNames.forEach { basename ->

            val resource =
                resourceLoader.getResource(
                    "classpath:$basename.yml"
                )


            if (resource.exists()) {

                val messages =
                    resource.inputStream.use {
                        Yaml()
                            .load<Map<String, Any>>(it)
                    }

                merge(
                    result,
                    messages
                )
            }
        }

        return result
    }


    private fun merge(
        target: MutableMap<String, Any>,
        source: Map<String, Any>
    ) {

        source.forEach { (key, value) ->

            if (
                value is Map<*, *> &&
                target[key] is Map<*, *>
            ) {

                merge(
                    target[key] as MutableMap<String, Any>,
                    value as Map<String, Any>
                )

            } else {
                target[key] = value
            }
        }
    }
}