package ru.tggc.telegrambotcore.registry;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisteredHandler {
    Method method;
    Object bean;
    Pattern pattern;
}
