package ru.tggc.telegrambotcore.annotation.handle

import ru.tggc.telegrambotcore.dto.Access
import ru.tggc.telegrambotcore.dto.UserRole

data class HandleMeta(
    val requiredRoles: Array<UserRole> = emptyArray<UserRole>(),
    val canPublic: Boolean = true,
    val canPrivate: Boolean = true,
    val access: Access = Access.OWNER_ONLY,
) {
    companion object {
        fun from(a: CallbackHandle): HandleMeta {
            return HandleMeta(a.requiredRoles, a.canPublic, a.canPrivate, a.access)
        }

        fun from(a: MessageHandle): HandleMeta {
            return HandleMeta(a.requiredRoles, a.canPublic, a.canPrivate, a.access)
        }

        fun from(a: PhotoHandle): HandleMeta {
            return HandleMeta(a.requiredRoles, a.canPublic, a.canPrivate, a.access)
        }

        fun from(a: CommandHandle): HandleMeta {
            return HandleMeta(a.requiredRoles, a.canPublic, a.canPrivate, a.access)
        }

        fun from(a: TextHandle): HandleMeta {
            return HandleMeta(access = a.access)
        }

        fun from(): HandleMeta {
            return HandleMeta(access = Access.ANYONE)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HandleMeta

        if (canPublic != other.canPublic) return false
        if (canPrivate != other.canPrivate) return false
        if (!requiredRoles.contentEquals(other.requiredRoles)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = canPublic.hashCode()
        result = 31 * result + canPrivate.hashCode()
        result = 31 * result + (requiredRoles.contentHashCode())
        return result
    }
}
