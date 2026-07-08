package ru.tggc.telegrambotspringbootstarter.autoconfigure;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import ru.tggc.telegrambotcore.service.TelegramBotReceiver;
import ru.tggc.telegrambotspringbootstarter.runner.TelegramBotRunner;

@Slf4j
@AutoConfiguration(after = TelegramBotAutoConfiguration.class)
@ConditionalOnProperty(
        prefix = "telegram",
        name = "mode",
        havingValue = "longpolling",
        matchIfMissing = true
)
public class LongPollingAutoConfiguration {

    @Bean
    public TelegramBotRunner telegramBotRunner(TelegramBot telegramBot, TelegramBotReceiver receiver) {
        return () -> {
            log.info("Starting telegram bot via longPolling");
            telegramBot.setUpdatesListener(updates -> {
                updates.forEach(receiver::receiveUpdate);
                return UpdatesListener.CONFIRMED_UPDATES_ALL;
            });
        };
    }
}
