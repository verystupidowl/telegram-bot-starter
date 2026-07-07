package ru.tggc.telegrambotcore.util;

import com.pengrad.telegrambot.model.ResponseParameters;
import com.pengrad.telegrambot.response.BaseResponse;
import lombok.experimental.UtilityClass;
import ru.tggc.telegrambotcore.exception.RetryableException;
import ru.tggc.telegrambotcore.exception.RetryableWithSecsException;

import static ru.tggc.telegrambotcore.util.Utils.getOrNull;

@UtilityClass
public class SendUtils {

    public <Rs extends BaseResponse> void checkRequestAndResponse(Rs rs) {
        if (!rs.isOk()) {
            String description = rs.description();
            Integer retryAfter = getOrNull(rs.parameters(), ResponseParameters::retryAfter);

            if (retryAfter != null) {
                throw new RetryableException(description, retryAfter);
            }

            if (description != null) {
                String d = description.toLowerCase();

                if (d.contains("timeout")
                        || d.contains("temporarily unavailable")
                        || d.contains("internal server error")
                        || d.contains("bad gateway")) {

                    throw new RetryableWithSecsException(description);
                }
            }

            throw new RetryableWithSecsException(description + rs);
        }
    }
}
