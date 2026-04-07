package com.isalnikov.msqwen.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * DTO для передачи данных промпта.
 * 
 * <p>Использует record для неизменяемости.
 * Содержит текст промпта для анализа новостей.</p>
 * 
 * @param id уникальный идентификатор промпта
 * @param userId идентификатор пользователя владельца
 * @param name название промпта
 * @param description описание промпта
 * @param promptText текст промпта для анализа
 * @param isActive флаг активности
 * @param createdAt дата создания
 * @author isalnikov
 * @version 1.0
 */
public record PromptDTO(
        Long id,
        Long userId,
        @NotBlank(message = "Название промпта не может быть пустым")
        String name,
        String description,
        @NotBlank(message = "Текст промпта не может быть пустым")
        String promptText,
        Boolean isActive,
        LocalDateTime createdAt
) {
    /**
     * Компактный конструктор для создания DTO из сущности Prompt.
     *
     * @param prompt сущность промпта
     */
    public PromptDTO(com.isalnikov.msqwen.entity.Prompt prompt) {
        this(
                prompt.getId(),
                prompt.getUser() != null ? prompt.getUser().getId() : null,
                prompt.getName(),
                prompt.getDescription(),
                prompt.getPromptText(),
                prompt.getIsActive(),
                prompt.getCreatedAt()
        );
    }
}
