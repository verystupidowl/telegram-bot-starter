package ru.tggc.telegrambotcore.dto;

import jakarta.annotation.Nullable;

public record AccessResult(boolean allowed, @Nullable Response response) {

    public static AccessResult allow() {
        return new AccessResult(true, null);
    }

    public static AccessResult deny(Response response) {
        return new AccessResult(false, response);
    }
}
