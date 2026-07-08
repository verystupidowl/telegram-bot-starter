package ru.tggc.telegrambotspringbootstarter.autoconfigure;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetWebhook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import ru.tggc.telegrambotspringbootstarter.TelegramProperties;
import ru.tggc.telegrambotspringbootstarter.runner.TelegramBotRunner;

@AutoConfiguration(after = TelegramBotAutoConfiguration.class)
@ConditionalOnProperty(
        prefix = "telegram",
        name = "mode",
        havingValue = "webhook",
        matchIfMissing = true
)
@Slf4j
public class WebhookAutoConfiguration {

    @Bean
    public TelegramBotRunner telegramBotRunner(TelegramBot bot, TelegramProperties telegramProperties) {
        return () -> {
            log.info("Starting telegram bot via webhook");
            var response = bot.execute(new SetWebhook().url(telegramProperties.getWebhook().getUrl()));
            log.info("Webhook info {}", response);
            bot.execute(new SendMessage(telegramProperties.getAdminId(), "Webhook has been set " + response));
        };
    }
}
