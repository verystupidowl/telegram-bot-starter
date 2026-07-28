package ru.tggc.telegrambotcore.service.defaults

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.stereotype.Service
import ru.tggc.telegrambotcore.dto.DialogSession
import ru.tggc.telegrambotcore.dto.HistoryKey
import ru.tggc.telegrambotcore.dto.UpdateContext
import ru.tggc.telegrambotcore.service.HistoryService
import java.time.Duration
import java.util.*
import java.util.function.Consumer
import java.util.function.Function

@Service
open class DefaultHistoryService : HistoryService {
    init {
        UpdateContext.historyService = this
    }

    companion object {
        private val cache: Cache<UpdateContext, DialogSession?> = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(3))
            .maximumSize(10000)
            .build<UpdateContext, DialogSession?>()
    }

    override fun setHistory(ctx: UpdateContext, historyType: HistoryKey, failAction: Consumer<DialogSession>) {
        val prev = cache.asMap().putIfAbsent(ctx, DialogSession(historyType, HashMap<String, String>()))
        if (prev != null) {
            failAction.accept(prev)
        }
    }

    override fun setHistory(
        ctx: UpdateContext,
        historyType: HistoryKey,
        promptMessageId: Int?,
        failAction: Consumer<DialogSession>
    ) {
        val existingSession = cache.getIfPresent(ctx)
        if (existingSession != null) {
            failAction.accept(existingSession)
            return
        }

        val newSession = DialogSession(
            state = historyType,
            promptMessageId = promptMessageId,
            data = HashMap()
        )

        cache.put(ctx, newSession)
    }

    override fun getSession(ctx: UpdateContext): DialogSession? {
        return cache.getIfPresent(ctx)
    }

    override fun getPromptMessageId(ctx: UpdateContext): Int? {
        return cache.getIfPresent(ctx)?.promptMessageId
    }

    override fun putData(ctx: UpdateContext, key: String, value: String) {
        cache.getIfPresent(ctx)?.let { it.data[key] = value }
    }

    override fun isEmpty(ctx: UpdateContext): Boolean {
        return cache.getIfPresent(ctx)?.data?.isEmpty() ?: false
    }

    override fun getData(ctx: UpdateContext, key: String): Optional<String> {
        return Optional.ofNullable<DialogSession?>(cache.getIfPresent(ctx))
            .map(Function { s: DialogSession? -> s!!.data[key] })
    }

    override fun isInHistory(ctx: UpdateContext, historyType: HistoryKey): Boolean {
        val session = cache.getIfPresent(ctx)
        return session != null && session.state === historyType
    }

    override fun contains(ctx: UpdateContext?): Boolean {
        return cache.getIfPresent(ctx) != null
    }

    override fun removeFromHistory(ctx: UpdateContext) {
        cache.invalidate(ctx)
    }

    override fun getFromHistory(ctx: UpdateContext): HistoryKey? {
        return cache.getIfPresent(ctx)?.state
    }
}