package ru.tggc.telegrambotcore.service

import ru.tggc.telegrambotcore.dto.ChatDto
import ru.tggc.telegrambotcore.dto.UserDto
import ru.tggc.telegrambotcore.dto.UserRole

interface UserService {
    fun checkRoles(id: Long, requiredRoles: Array<UserRole>): Boolean

    fun saveOrUpdate(dto: UserDto, chatDto: ChatDto)
}
