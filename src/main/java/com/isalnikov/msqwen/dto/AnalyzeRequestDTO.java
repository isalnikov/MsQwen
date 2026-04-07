package com.isalnikov.msqwen.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO для запроса анализа новостей.
 *
 * @param promptId идентификатор промпта
 * @author isalnikov
 * @version 1.0
 */
public record AnalyzeRequestDTO(
        @NotNull Long promptId
) {
}
