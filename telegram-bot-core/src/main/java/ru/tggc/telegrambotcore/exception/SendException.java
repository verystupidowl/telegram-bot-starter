package ru.tggc.telegrambotcore.exception;

import com.pengrad.telegrambot.response.BaseResponse;
import lombok.Getter;

@Getter
public class SendException extends RuntimeException {
    private BaseResponse rs;

    public SendException(BaseResponse rs) {
        super("Send Failed " + rs.description());
        this.rs = rs;
    }

    public SendException(String message) {
        super(message);
    }
}
