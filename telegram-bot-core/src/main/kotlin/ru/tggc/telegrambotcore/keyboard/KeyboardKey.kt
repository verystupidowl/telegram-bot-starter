package ru.tggc.telegrambotcore.keyboard

@Suppress("UNCHECKED_CAST")
data class KeyboardKey<T> @JvmOverloads constructor(
    val keyboardId: String,
    val classType: Class<T> = Void::class.java as Class<T>
)