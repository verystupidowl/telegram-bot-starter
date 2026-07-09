package ru.tggc.telegrambotcore.dto

@JvmRecord
data class AccessResult(val allowed: Boolean, val response: Response?) {
    companion object {
        @JvmStatic
        fun allow(): AccessResult {
            return AccessResult(true, null)
        }

        @JvmStatic
        fun deny(response: Response?): AccessResult {
            return AccessResult(false, response)
        }
    }
}
