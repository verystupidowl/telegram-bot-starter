package ru.tggc.telegrambotcore.service

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.pengrad.telegrambot.model.User
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Function

@Service
class UserRateLimiterService {
    private val log = KotlinLogging.logger {}

    private val countOfUpdates: Cache<Long?, Int?> = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofSeconds(10))
        .maximumSize(10000)
        .build()
    private val lockCache: Cache<Long?, AtomicBoolean?> = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofSeconds(10))
        .maximumSize(10000)
        .build()

    fun lock(userId: Long) {
        val locked = lockCache.get(userId, Function { `_`: Long? -> AtomicBoolean(true) })
        locked?.set(true)
    }

    fun tryLock(id: Long): Boolean {
        val locked = lockCache.get(id, Function { `_`: Long? -> AtomicBoolean(false) })
        return locked!!.compareAndSet(false, true)
    }

    fun unlock(userId: Long) {
        val locked = lockCache.getIfPresent(userId)
        locked?.set(false)
    }

    fun isLocked(userId: Long?): Boolean {
        val locked = lockCache.get(userId, Function { `_`: Long? -> AtomicBoolean(false) })
        return locked!!.get()
    }

    fun checkRateLimit(from: User): Optional<String> {
        val count = countOfUpdates.getIfPresent(from.id())

        if (count != null && count > MAX_REQUESTS) {
            log.info { "user ${from.username()} is trying to ddos" }

            val ageOpt = countOfUpdates.policy().expireAfterWrite()
                .map { ex -> ex.ageOf(from.id(), TimeUnit.SECONDS) }
                .orElse(OptionalLong.empty())

            val text = if (ageOpt.isPresent)
                "Слишком много запросов, попробуй снова через ${MAX_REQUESTS - ageOpt.getAsLong()} c"
            else
                "Слишком много запросов"

            return Optional.of<String>(text)
        }

        countOfUpdates.put(from.id(), if (count == null) 1 else count + 1)
        return Optional.empty<String>()
    }

    companion object {
        private const val MAX_REQUESTS = 10
    }
}
