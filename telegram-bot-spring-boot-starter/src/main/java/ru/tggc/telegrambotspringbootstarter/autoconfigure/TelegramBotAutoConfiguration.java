package ru.tggc.telegrambotspringbootstarter.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@ComponentScan("ru.tggc.telegrambotcore")
public class TelegramBotAutoConfiguration {
}
