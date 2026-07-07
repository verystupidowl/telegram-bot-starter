package ru.tggc.telegrambotcore.annotation.handle;


import ru.tggc.telegrambotcore.dto.UserRole;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CallbackHandle {

    String value();

    UserRole[] requiredRoles() default {};

    boolean canPrivate() default false;

    boolean canPublic() default true;
}
