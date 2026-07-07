package ru.tggc.telegrambotcore.handler;

import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import ru.tggc.telegrambotcore.dto.PhotoDto;
import ru.tggc.telegrambotcore.dto.Response;
import ru.tggc.telegrambotcore.dto.ResponseBuilder;

import java.util.List;

public abstract class Handler {

    public Response sendSimplePhoto(PhotoDto photo) {
        return ResponseBuilder.create()
                .photo(photo)
                .build();
    }

    public Response sendSimplePhotos(List<PhotoDto> photos) {
        return ResponseBuilder.create()
                .photos(photos)
                .build();
    }

    public Response sendSimpleMessage(long chatId, String text) {
        return sendSimpleMessage(chatId, text, null);
    }

    public Response sendSimpleMessage(long chatId, String text, InlineKeyboardMarkup markup) {
        return ResponseBuilder.to(chatId)
                .message(text, markup)
                .build();
    }

    public Response sendSimpleMessages(long chatId, List<String> texts) {
        return ResponseBuilder.to(chatId)
                .messages(texts)
                .build();
    }

    public Response editMessageCaption(long chatId, Integer messageId, String caption, InlineKeyboardMarkup markup) {
        return ResponseBuilder.to(chatId)
                .edit(messageId, caption, markup)
                .build();
    }

    public Response editPhotos(Integer messageId, List<PhotoDto> photos) {
        return ResponseBuilder.create()
                .edit(photos, messageId)
                .build();
    }

    public Response editPhoto(long chatId, Integer messageId, String photoUrl, String caption) {
        return ResponseBuilder.to(chatId)
                .editPhoto(messageId, photoUrl, caption)
                .build();
    }

    public Response editSimpleMessage(long chatId, int messageId, String text) {
        return editSimpleMessage(chatId, messageId, text, null);
    }

    public Response editSimpleMessage(long chatId, int messageId, String text, InlineKeyboardMarkup markup) {
        return ResponseBuilder.to(chatId)
                .edit(messageId, text, markup)
                .build();
    }
}
