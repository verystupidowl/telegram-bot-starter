package ru.tggc.telegrambotspringbootstarter;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "telegram")
@Data
public class TelegramProperties {
    /**
     * Message capture mode
     */
    private Mode mode = Mode.LONG_POLLING;
    /**
     * Webhook properties
     */
    private Webhook webhook = new Webhook();

    /**
     * Telegram bot token
     */
    private String token;
    /**
     * Admin's telegram id
     */
    private String adminId;
    /**
     * Bot's Telegram ID
     */
    private String botId;

    public enum Mode {
        WEBHOOK,
        LONG_POLLING
    }

    @Data
    public static class Webhook {
        /**
         * Webhook URL
         */
        private String url;
        /**
         * Webhook path
         */
        public String path;
    }
}
