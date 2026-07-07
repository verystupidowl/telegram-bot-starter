package ru.tggc.telegrambotcore.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ParamConverter {

    @SuppressWarnings("unchecked")
    public Object convert(String value, Class<?> targetType) {
        if (value == null) {
            return null;
        }
        if (targetType.equals(String.class)) {
            return value;
        } else if (targetType.equals(int.class) || targetType.equals(Integer.class)) {
            return Integer.parseInt(value);
        } else if (targetType.equals(long.class) || targetType.equals(Long.class)) {
            return Long.parseLong(value);
        } else if (targetType.equals(boolean.class) || targetType.equals(Boolean.class)) {
            return Boolean.parseBoolean(value);
        } else if (Enum.class.isAssignableFrom(targetType)) {
            return Enum.valueOf((Class<Enum>) targetType, value.toUpperCase());
        }

        throw new IllegalArgumentException("Unsupported parameter type: " + targetType.getName());
    }
}
