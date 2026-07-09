package ru.tggc.telegrambotcore.util

class ParamConverter {
    companion object {

        fun convert(value: String?, targetType: Class<*>): Any? {
            if (value == null) {
                return null
            }

            return when {
                targetType == String::class.java -> value

                targetType == Int::class.java || targetType == Int::class.javaObjectType -> value.toInt()

                targetType == Long::class.java || targetType == Long::class.javaObjectType -> value.toLong()

                targetType == Boolean::class.java || targetType == Boolean::class.javaObjectType -> value.toBoolean()

                targetType.isEnum -> targetType.enumConstants.first { (it as Enum<*>).name == value.uppercase() }

                else -> throw IllegalArgumentException("Unsupported parameter type: ${targetType.name}")
            }
        }
    }
}
