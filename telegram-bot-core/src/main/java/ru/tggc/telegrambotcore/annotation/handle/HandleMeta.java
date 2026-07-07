package ru.tggc.telegrambotcore.annotation.handle;

import ru.tggc.telegrambotcore.dto.UserRole;

public record HandleMeta(UserRole[] requiredRoles, boolean canPublic, boolean canPrivate) {

    public static HandleMeta from(CallbackHandle a) {
        return new HandleMeta(a.requiredRoles(), a.canPublic(), a.canPrivate());
    }

    public static HandleMeta from(MessageHandle a) {
        return new HandleMeta(a.requiredRoles(), a.canPublic(), a.canPrivate());
    }

    public static HandleMeta from(PhotoHandle a) {
        return new HandleMeta(a.requiredRoles(), a.canPublic(), a.canPrivate());
    }

    public static HandleMeta from(CommandHandle a) {
        return new HandleMeta(a.requiredRoles(), a.canPublic(), a.canPrivate());
    }

    public static HandleMeta fromDefault() {
        return new HandleMeta(new UserRole[0], true, true);
    }
}
