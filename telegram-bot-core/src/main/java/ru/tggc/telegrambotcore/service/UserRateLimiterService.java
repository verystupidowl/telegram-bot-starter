package ru.tggc.telegrambotcore.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.pengrad.telegrambot.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class UserRateLimiterService {
    private final Cache<Long, Integer> countOfUpdates = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofSeconds(10))
            .maximumSize(10_000)
            .build();
    private final Cache<Long, AtomicBoolean> lockCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofSeconds(10))
            .maximumSize(10_000)
            .build();
    private static final int MAX_REQUESTS = 10;

    public void lock(long userId) {
        AtomicBoolean locked = lockCache.get(userId, _ -> new AtomicBoolean(true));
        locked.set(true);
    }

    public boolean tryLock(long id) {
        AtomicBoolean locked = lockCache.get(id, _ -> new AtomicBoolean(false));
        return locked.compareAndSet(false, true);
    }

    public void unlock(long userId) {
        AtomicBoolean locked = lockCache.getIfPresent(userId);
        if (locked != null) locked.set(false);
    }

    public boolean isLocked(Long userId) {
        AtomicBoolean locked = lockCache.get(userId, _ -> new AtomicBoolean(false));
        return locked.get();
    }

    public Optional<String> checkRateLimit(User from) {
        Integer count = countOfUpdates.getIfPresent(from.id());

        if (count != null && count > MAX_REQUESTS) {
            log.info("user {} is trying to ddos", from.username());

            OptionalLong ageOpt = countOfUpdates.policy().expireAfterWrite()
                    .map(ex -> ex.ageOf(from.id(), TimeUnit.SECONDS))
                    .orElse(OptionalLong.empty());

            String text = ageOpt.isPresent()
                    ? "Слишком много запросов, попробуй снова через " + (MAX_REQUESTS - ageOpt.getAsLong()) + "c"
                    : "Слишком много запросов";

            return Optional.of(text);
        }

        countOfUpdates.put(from.id(), count == null ? 1 : count + 1);
        return Optional.empty();
    }
}
