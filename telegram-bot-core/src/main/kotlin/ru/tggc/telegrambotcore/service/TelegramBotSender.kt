package ru.tggc.telegrambotcore.service

import com.pengrad.telegrambot.TelegramBot
import ru.tggc.telegrambotcore.dto.Response
import java.util.function.Consumer

/**
 * Сервис отправки сообщений
 */
interface TelegramBotSender {
    /**
     * Отправить простое сообщение
     *
     * @param response ответ пользователю
     */
    fun send(response: Response)

    /**
     * Отправить сообщение после задержки
     *
     * @param task задача, которую надо выполнить после задержки
     * @param delayMillis задержка в миллисекундах
     */
    fun sendDelayed(task: Consumer<TelegramBot>, delayMillis: Long)

    /**
     * Отправить сообщение после задержки
     *
     * @param response ответ, который надо отправить после задержки
     * @param delayMillis задержка в миллисекундах
     */
    fun sendDelayed(response: Response, delayMillis: Long)

    /**
     * Отправить сообщение администратору бота
     */
    fun sendToAdmin(text: String)
}
