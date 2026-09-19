package ru.tggc.telegrambotcore.dto

import com.pengrad.telegrambot.response.SendResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.future.await
import kotlinx.coroutines.future.future
import java.util.function.Consumer

internal val asyncResponseScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

/** An opt-in response whose completion includes all work, sending and cleanup. */
fun interface CompletionAwareResponse : Response {
    // Only the new response type changes sequencing; legacy Response keeps its behaviour.
    override fun andThen(after: Response): CompletionAwareResponse = CompletionAwareResponse { bot ->
        asyncResponseScope.future {
            this@CompletionAwareResponse.accept(bot).await()
            after.accept(bot).await()
        }
    }

    override fun then(action: Consumer<SendResponse>): CompletionAwareResponse = CompletionAwareResponse { bot ->
        asyncResponseScope.future {
            val result = this@CompletionAwareResponse.accept(bot).await()
            if (result is SendResponse) action.accept(result)
            result
        }
    }
}
