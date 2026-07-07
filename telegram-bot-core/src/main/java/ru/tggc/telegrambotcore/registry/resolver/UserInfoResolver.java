package ru.tggc.telegrambotcore.registry.resolver;

import org.springframework.stereotype.Component;
import ru.tggc.telegrambotcore.annotation.params.UserInfo;
import ru.tggc.telegrambotcore.dto.UserDto;

import java.lang.reflect.Parameter;

@Component
public class UserInfoResolver implements ParameterResolver<UserDto> {

    @Override
    public boolean supports(Parameter p) {
        return p.isAnnotationPresent(UserInfo.class) && p.getType().equals(UserDto.class);
    }

    @Override
    public UserDto resolve(Parameter parameter, HandlerCtx ctx) {
        return new UserDto(ctx.from().id(), ctx.from().username());
    }
}
