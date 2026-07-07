package ru.tggc.telegrambotcore.service;

import ru.tggc.telegrambotcore.dto.UpdateContext;

public interface HistoryService {

    boolean contains(UpdateContext ctx);
}
