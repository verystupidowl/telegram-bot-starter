package ru.tggc.telegrambotcore.util;

import jakarta.annotation.Nullable;
import lombok.experimental.UtilityClass;
import org.apache.commons.text.StringSubstitutor;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@UtilityClass
public class Utils {

    public static <T, R> R getOrNull(T t, Function<T, R> function) {
        return Optional.ofNullable(t).map(function).orElse(null);
    }

    public static <T, R> R getOrElse(T t, Function<T, R> function, R orElse) {
        return Optional.ofNullable(t).map(function).orElse(orElse);
    }

    public static <T, R> R getOrElseGet(T t, Function<T, R> function, Supplier<R> orElse) {
        return Optional.ofNullable(t).map(function).orElseGet(orElse);
    }

    public static <T> void ifPresent(T t, Consumer<T> consumer) {
        Optional.ofNullable(t).ifPresent(consumer);
    }

    public static <T, R extends RuntimeException> void throwIfNull(@Nullable T t, Supplier<R> e) {
        if (t == null) {
            throw e.get();
        }
    }

    public static <R extends RuntimeException> void throwIf(boolean condition, Supplier<R> e) {
        if (condition) {
            throw e.get();
        }
    }

    public static String getText(String text, Map<String, String> params) {
        StringSubstitutor sub = new StringSubstitutor(params);
        return sub.replace(text);
    }

    public static String renderStaminaBar(double percent) {
        int totalBlocks = 5;
        int filledBlocks = (int) Math.round(percent / (100.0 / totalBlocks));

        return "["
                + "█".repeat(filledBlocks)
                + "░".repeat(totalBlocks - filledBlocks)
                + "] "
                + (int) percent + "%";
    }

    public String checkNumber(String number) {
        long longNumber = Long.parseLong(number);
        return String.valueOf(longNumber);
    }

    public static String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        if (hours > 0) return hours + "ч " + minutes + "м";
        if (minutes > 0) return minutes + "м " + seconds + "с";
        return seconds + "с";
    }
}
