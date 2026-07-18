package ru.tggc.telegrambotspringbootstarter

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "telegram")
data class TelegramProperties(
    /**
     * Message capture mode
     */
    var mode: Mode = Mode.LONG_POLLING,

    /**
     * Webhook properties
     */
    var webhook: Webhook = Webhook(),

    /**
     * Telegram bot token
     */
    var token: String? = null,

    /**
     * Admin's telegram id
     */
    var adminId: String? = null,

    /**
     * Bot's Telegram ID
     */
    var botId: String? = null,

    /**
     * List of messages base names
     */
    var baseNames: List<String> = listOf(
        "telegram/messages/messages"
    )
) {

    enum class Mode {
        WEBHOOK,
        LONG_POLLING
    }

    data class Webhook(
        /**
         * Webhook URL
         */
        var url: String? = null,

        /**
         * Webhook path
         */
        var path: String? = null
    )
}
