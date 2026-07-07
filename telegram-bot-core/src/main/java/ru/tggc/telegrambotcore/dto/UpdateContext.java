package ru.tggc.telegrambotcore.dto;

import lombok.Builder;

import java.util.Objects;

@Builder
public record UpdateContext(
        long chatId,
        long userId,
        int messageId
) {

    public UpdateContext(long chatId, long userId) {
        this(chatId, userId, 0);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatId, userId);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UpdateContext that = (UpdateContext) o;
        return chatId == that.chatId && userId == that.userId;
    }
}
