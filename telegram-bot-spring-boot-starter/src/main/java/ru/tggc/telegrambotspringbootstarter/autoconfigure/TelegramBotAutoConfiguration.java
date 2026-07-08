package ru.tggc.telegrambotspringbootstarter.autoconfigure;

import com.pengrad.telegrambot.TelegramBot;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import ru.tggc.telegrambotspringbootstarter.TelegramProperties;

@AutoConfiguration
@ComponentScan("ru.tggc.telegrambotcore")
@EnableConfigurationProperties(TelegramProperties.class)
public class TelegramBotAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TelegramBot telegramBot(TelegramProperties properties) {
        return new TelegramBot(properties.getToken());
    }
}
