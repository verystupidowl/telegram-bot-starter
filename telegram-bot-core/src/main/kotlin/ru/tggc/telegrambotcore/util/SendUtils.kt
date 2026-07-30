package ru.tggc.telegrambotcore.util

import com.pengrad.telegrambot.model.ResponseParameters
import com.pengrad.telegrambot.response.BaseResponse
import ru.tggc.telegrambotcore.exception.RetryableException
import ru.tggc.telegrambotcore.exception.RetryableWithSecsException
import java.util.*

class SendUtils {
    companion object {
        fun <Rs : BaseResponse?> checkRequestAndResponse(rs: Rs?) {
            if (!rs!!.isOk) {
                val description = rs.description()
                val retryAfter = Utils.getOrNull<ResponseParameters?, Int?>(
                    rs.parameters()
                ) { obj: ResponseParameters? -> obj!!.retryAfter() }

                if (retryAfter != null) {
                    throw RetryableException(description, retryAfter)
                }

                if (description != null) {
                    val d = description.lowercase(Locale.getDefault())

                    if (d.contains("timeout")
                        || d.contains("temporarily unavailable")
                        || d.contains("internal server error")
                        || d.contains("bad gateway")) {
                        throw RetryableWithSecsException(description)
                    }
                }

                throw RetryableWithSecsException(description + rs)
            }
        }
    }
}
