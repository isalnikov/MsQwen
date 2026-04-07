package com.isalnikov.msqwen.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO для запроса парсинга каналов.
 *
 * @param promptId идентификатор промпта
 * @author isalnikov
 * @version 1.0
 */
public record ParseRequestDTO(
        @NotNull Long promptId
) {
}
