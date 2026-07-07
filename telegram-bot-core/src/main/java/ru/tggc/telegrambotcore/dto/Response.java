package ru.tggc.telegrambotcore.dto;

import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.response.BaseResponse;
import ru.tggc.telegrambotcore.exception.RetryableWithSecsException;
import ru.tggc.telegrambotcore.util.SendUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@FunctionalInterface
public interface Response extends Consumer<TelegramBot> {

    static <Rq extends BaseRequest<Rq, Rs>, Rs extends BaseResponse> Response ofAll(List<Rq> requests) {
        return bot -> requests.forEach(request -> bot.execute(request, checkResponse()));
    }

    @SafeVarargs
    static <Rq extends BaseRequest<Rq, Rs>, Rs extends BaseResponse> Response ofAll(Rq... requests) {
        return bot -> Arrays.stream(requests).forEach(request -> bot.execute(request, checkResponse()));
    }

    static <Rq extends BaseRequest<Rq, Rs>, Rs extends BaseResponse> Response of(Rq request) {
        return bot -> bot.execute(request, checkResponse());
    }

    static Response of(Consumer<TelegramBot> consumer) {
        return consumer::accept;
    }

    static Response ofAllConsumers(List<Consumer<TelegramBot>> consumers) {
        return bot -> consumers.forEach(c -> c.accept(bot));
    }

    static <T> Response of(BiConsumer<TelegramBot, T> consumer, T request) {
        return bot -> consumer.accept(bot, request);
    }

    static Response empty() {
        return _ -> {
        };
    }

    default Response andThen(Response after) {
        return bot -> {
            this.accept(bot);
            after.accept(bot);
        };
    }

    private static <Rq extends BaseRequest<Rq, Rs>, Rs extends BaseResponse> Callback<Rq, Rs> checkResponse() {
        return new Callback<>() {
            @Override
            public void onResponse(Rq rq, Rs rs) {
                SendUtils.checkRequestAndResponse(rs);
            }

            @Override
            public void onFailure(Rq rq, IOException e) {
                throw new RetryableWithSecsException("Exception while sending request " + e.getMessage());
            }
        };
    }
}