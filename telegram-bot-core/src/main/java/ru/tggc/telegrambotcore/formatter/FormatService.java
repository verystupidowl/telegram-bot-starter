package ru.tggc.telegrambotcore.formatter;

public interface FormatService {

    String getMessage(MsgKey key, Object... args);
}
