package ru.tggc.telegrambotcore.exception;

public class RetryableWithSecsException extends RetryableException {

    public RetryableWithSecsException(String message) {
        super(message, 5);
    }
}
