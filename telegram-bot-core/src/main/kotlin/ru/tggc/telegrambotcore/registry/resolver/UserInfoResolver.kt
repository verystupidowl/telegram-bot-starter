package ru.tggc.telegrambotcore.registry.resolver

import org.springframework.stereotype.Component
import ru.tggc.telegrambotcore.annotation.params.UserInfo
import ru.tggc.telegrambotcore.dto.UserDto
import java.lang.reflect.Parameter

@Component
class UserInfoResolver : ParameterResolver<UserDto?> {
    override fun supports(p: Parameter?): Boolean? =
        p?.isAnnotationPresent(UserInfo::class.java) == true && p.getType() == UserDto::class.java

    override fun resolve(parameter: Parameter?, ctx: HandlerCtx): UserDto =
        UserDto(ctx.from!!.id(), ctx.from.username())

}
