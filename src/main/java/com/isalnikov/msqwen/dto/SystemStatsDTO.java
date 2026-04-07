package com.isalnikov.msqwen.dto;

/**
 * DTO для системной статистики (админ).
 *
 * @param totalUsers общее количество пользователей
 * @param totalPrompts общее количество промптов
 * @param totalChannels общее количество каналов
 * @param totalNews общее количество новостей
 * @param totalAnalyzed количество проанализированных новостей
 * @author isalnikov
 * @version 1.0
 */
public record SystemStatsDTO(
        Long totalUsers,
        Long totalPrompts,
        Long totalChannels,
        Long totalNews,
        Long totalAnalyzed
) {
}
