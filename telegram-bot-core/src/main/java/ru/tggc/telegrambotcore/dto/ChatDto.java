package ru.tggc.telegrambotcore.dto;

import lombok.Builder;

@Builder
public record ChatDto(Long id, String title) {
}
