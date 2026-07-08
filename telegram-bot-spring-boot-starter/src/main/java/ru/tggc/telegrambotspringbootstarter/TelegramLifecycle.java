package ru.tggc.telegrambotspringbootstarter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import ru.tggc.telegrambotspringbootstarter.runner.TelegramBotRunner;

@Slf4j
@RequiredArgsConstructor
public class TelegramLifecycle implements SmartLifecycle {
    private boolean running = false;

    private final TelegramBotRunner telegramBotRunner;

    @Override
    public void start() {
        telegramBotRunner.start();
        running = true;
    }

    @Override
    public void stop() {
        log.info("Stopping Telegram bot runner");
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
