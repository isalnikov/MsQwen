package com.isalnikov.msqwen.parser;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Временный объект распарсенной новости из Telegram канала.
 *
 * <p>Используется для хранения данных полученных при парсинге
 * перед конвертацией в {@link com.isalnikov.msqwen.entity.News} entity.
 * Содержит все метаданные для анализа: просмотры, пересылки, реакции, лайки
 * и рассчитанный индекс вовлечённости.</p>
 *
 * @param messageId ID сообщения в Telegram канале
 * @param title заголовок новости (может быть null)
 * @param content текст новости
 * @param publicationDate дата публикации
 * @param viewsCount количество просмотров
 * @param forwardsCount количество пересылок
 * @param reactionsCount количество реакций
 * @param likesCount количество лайков
 * @param engagementScore индекс вовлечённости (0.0 - 1.0)
 * @param newsUrl прямая ссылка на новость
 * @author isalnikov
 * @version 1.0
 */
public record ParsedNews(
        Long messageId,
        String title,
        String content,
        LocalDateTime publicationDate,
        Integer viewsCount,
        Integer forwardsCount,
        Integer reactionsCount,
        Integer likesCount,
        BigDecimal engagementScore,
        String newsUrl
) {

    /**
     * Создаёт ParsedNews с нулевыми метриками.
     *
     * @param messageId ID сообщения
     * @param content текст новости
     * @param publicationDate дата публикации
     * @param newsUrl ссылка на новость
     * @return ParsedNews с нулевыми метриками
     */
    public static ParsedNews withDefaults(Long messageId, String content,
                                          LocalDateTime publicationDate, String newsUrl) {
        return new ParsedNews(
                messageId,
                null,
                content,
                publicationDate,
                0,
                0,
                0,
                0,
                BigDecimal.ZERO,
                newsUrl
        );
    }
}
