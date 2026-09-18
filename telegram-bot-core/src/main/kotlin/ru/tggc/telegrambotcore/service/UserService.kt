package ru.tggc.telegrambotcore.service

import ru.tggc.telegrambotcore.dto.ChatDto
import ru.tggc.telegrambotcore.dto.UserDto
import ru.tggc.telegrambotcore.dto.UserRole

/**
 * Сервис для работы с БД пользователей
 */
interface UserService {
    /**
     * Метод для проверки ролей пользователя
     *
     * @param id id пользователя в телеграм
     */
    fun checkRoles(id: Long, requiredRoles: Array<UserRole>): Boolean

    /**
     * Метод сохранения или обновления пользователя в БД
     */
    fun saveOrUpdate(dto: UserDto, chatDto: ChatDto)
}
