package com.isalnikov.msqwen.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO для передачи данных новости с метриками вовлечённости.
 * 
 * <p>Использует record для неизменяемости.
 * Содержит все метрики: просмотры пересылки реакции лайки engagement_score.</p>
 * 
 * @param id уникальный идентификатор новости
 * @param channelId идентификатор канала
 * @param promptId идентификатор промпта
 * @param userId идентификатор пользователя
 * @param messageId ID сообщения в Telegram
 * @param title заголовок новости
 * @param content содержание новости
 * @param publicationDate дата публикации
 * @param viewsCount количество просмотров
 * @param forwardsCount количество пересылок
 * @param reactionsCount количество реакций
 * @param likesCount количество лайков
 * @param engagementScore индекс вовлечённости
 * @param newsUrl ссылка на новость
 * @param isAnalyzed флаг анализа
 * @param createdAt дата создания
 * @author isalnikov
 * @version 1.0
 */
public record NewsDTO(
        Long id,
        Long channelId,
        Long promptId,
        Long userId,
        Long messageId,
        String title,
        String content,
        LocalDateTime publicationDate,
        Integer viewsCount,
        Integer forwardsCount,
        Integer reactionsCount,
        Integer likesCount,
        BigDecimal engagementScore,
        String newsUrl,
        Boolean isAnalyzed,
        LocalDateTime createdAt
) {
    /**
     * Компактный конструктор для создания DTO из сущности News.
     *
     * @param news сущность новости
     */
    public NewsDTO(com.isalnikov.msqwen.entity.News news) {
        this(
                news.getId(),
                news.getChannel() != null ? news.getChannel().getId() : null,
                news.getPrompt() != null ? news.getPrompt().getId() : null,
                news.getUser() != null ? news.getUser().getId() : null,
                news.getMessageId(),
                news.getTitle(),
                news.getContent(),
                news.getPublicationDate(),
                news.getViewsCount(),
                news.getForwardsCount(),
                news.getReactionsCount(),
                news.getLikesCount(),
                news.getEngagementScore(),
                news.getNewsUrl(),
                news.getIsAnalyzed(),
                news.getCreatedAt()
        );
    }
}
