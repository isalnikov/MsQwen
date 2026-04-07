package com.isalnikov.msqwen.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO для запроса создания/обновления промпта.
 *
 * @param name название промпта
 * @param description описание промпта
 * @param promptText текст промпта для анализа
 * @author isalnikov
 * @version 1.0
 */
public record PromptCreateRequestDTO(
        @NotBlank String name,
        String description,
        @NotBlank String promptText
) {
}
