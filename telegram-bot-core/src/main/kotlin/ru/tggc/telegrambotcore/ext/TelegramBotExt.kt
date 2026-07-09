package ru.tggc.telegrambotcore.ext

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.request.BaseRequest
import com.pengrad.telegrambot.response.BaseResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.tggc.telegrambotcore.util.SendUtils

suspend fun <Rq, Rs> TelegramBot.executeAsync(request: Rq): Rs where Rq : BaseRequest<Rq, Rs>, Rs : BaseResponse =
    withContext(Dispatchers.IO) {
        val response = execute(request)

        SendUtils.checkRequestAndResponse(response)

        response
    }
