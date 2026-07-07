package ru.tggc.telegrambotcore.service;

import ru.tggc.telegrambotcore.dto.ChatDto;
import ru.tggc.telegrambotcore.dto.UserDto;
import ru.tggc.telegrambotcore.dto.UserRole;

public interface UserService {

    boolean checkRoles(Long id, UserRole[] requiredRoles);

    void saveOrUpdate(UserDto dto, ChatDto chatDto);
}
