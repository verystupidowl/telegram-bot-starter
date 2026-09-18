package ru.tggc.telegrambotcore.service.defaults

import ru.tggc.telegrambotcore.dto.ChatDto
import ru.tggc.telegrambotcore.dto.UserDto
import ru.tggc.telegrambotcore.dto.UserRole
import ru.tggc.telegrambotcore.service.UserService

/** Stateless default: stores nothing and never grants a required role. */
class NoOpUserService : UserService {
    override fun saveOrUpdate(dto: UserDto, chatDto: ChatDto) = Unit

    override fun checkRoles(id: Long, requiredRoles: Array<UserRole>): Boolean = requiredRoles.isEmpty()
}
