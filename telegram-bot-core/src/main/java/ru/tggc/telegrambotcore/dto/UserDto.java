package ru.tggc.telegrambotcore.dto;

import java.util.Objects;

public record UserDto(
        Long userId,
        String username
) {
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UserDto userDto = (UserDto) o;
        return Objects.equals(userId, userDto.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userId);
    }
}
