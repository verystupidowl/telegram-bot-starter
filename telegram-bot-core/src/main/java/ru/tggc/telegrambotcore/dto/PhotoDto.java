package ru.tggc.telegrambotcore.dto;

import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public final class PhotoDto {
    private String url;
    private String caption;
    private long chatId;
    private InlineKeyboardMarkup markup;
}
