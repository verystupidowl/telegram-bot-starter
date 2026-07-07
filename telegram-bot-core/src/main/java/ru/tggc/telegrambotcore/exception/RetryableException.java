package ru.tggc.telegrambotcore.exception;

import lombok.Getter;

@Getter
public class RetryableException extends RuntimeException {
    private final int retryMillis;

    public RetryableException(String message, int retryMillis) {
        super(message);
        this.retryMillis = retryMillis;
    }

    public RetryableException(String message) {
        super(message);
        this.retryMillis = 2000;
    }
}
