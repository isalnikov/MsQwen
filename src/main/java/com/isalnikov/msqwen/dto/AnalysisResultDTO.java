package com.isalnikov.msqwen.dto;

import java.time.LocalDateTime;

/**
 * DTO для передачи данных результата анализа.
 * 
 * <p>Использует record для неизменяемости.
 * Содержит текст анализа и информацию о кешировании.</p>
 * 
 * @param id уникальный идентификатор результата
 * @param promptId идентификатор промпта
 * @param userId идентификатор пользователя
 * @param analysisText текст результата анализа
 * @param newsIds ID новостей участвовавших в анализе
 * @param cacheKey ключ кеша
 * @param cacheExpiresAt дата истечения кеша
 * @param createdAt дата создания
 * @author isalnikov
 * @version 1.0
 */
public record AnalysisResultDTO(
        Long id,
        Long promptId,
        Long userId,
        String analysisText,
        String newsIds,
        String cacheKey,
        LocalDateTime cacheExpiresAt,
        LocalDateTime createdAt
) {
    /**
     * Компактный конструктор для создания DTO из сущности AnalysisResult.
     *
     * @param result сущность результата анализа
     */
    public AnalysisResultDTO(com.isalnikov.msqwen.entity.AnalysisResult result) {
        this(
                result.getId(),
                result.getPrompt() != null ? result.getPrompt().getId() : null,
                result.getUser() != null ? result.getUser().getId() : null,
                result.getAnalysisText(),
                result.getNewsIds(),
                result.getCacheKey(),
                result.getCacheExpiresAt(),
                result.getCreatedAt()
        );
    }
}
