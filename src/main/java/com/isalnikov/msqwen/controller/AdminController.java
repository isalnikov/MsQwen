package com.isalnikov.msqwen.controller;

import com.isalnikov.msqwen.dto.SystemStatsDTO;
import com.isalnikov.msqwen.repository.AnalysisResultRepository;
import com.isalnikov.msqwen.repository.ChannelRepository;
import com.isalnikov.msqwen.repository.NewsRepository;
import com.isalnikov.msqwen.repository.PromptRepository;
import com.isalnikov.msqwen.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API контроллер для административных операций.
 *
 * <p>Предоставляет endpoints для очистки всех таблиц и получения статистики системы.
 * Эти endpoints требуют подтверждения через header и предназначены для администрирования.
 * Базовый путь: /api/v1/admin</p>
 *
 * @author isalnikov
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin API", description = "Административные операции")
public class AdminController {

    /**
     * Логгер для записи событий контроллера.
     */
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    /**
     * Репозиторий пользователей.
     */
    private final UserRepository userRepository;

    /**
     * Репозиторий промптов.
     */
    private final PromptRepository promptRepository;

    /**
     * Репозиторий каналов.
     */
    private final ChannelRepository channelRepository;

    /**
     * Репозиторий новостей.
     */
    private final NewsRepository newsRepository;

    /**
     * Репозиторий результатов анализа.
     */
    private final AnalysisResultRepository analysisResultRepository;

    /**
     * Конструктор с внедрением зависимостей через конструктор.
     *
     * @param userRepository репозиторий пользователей
     * @param promptRepository репозиторий промптов
     * @param channelRepository репозиторий каналов
     * @param newsRepository репозиторий новостей
     * @param analysisResultRepository репозиторий результатов анализа
     */
    public AdminController(UserRepository userRepository,
                           PromptRepository promptRepository,
                           ChannelRepository channelRepository,
                           NewsRepository newsRepository,
                           AnalysisResultRepository analysisResultRepository) {
        this.userRepository = userRepository;
        this.promptRepository = promptRepository;
        this.channelRepository = channelRepository;
        this.newsRepository = newsRepository;
        this.analysisResultRepository = analysisResultRepository;
    }

    /**
     * Получить статистику системы.
     *
     * @return системная статистика
     */
    @GetMapping("/stats")
    @Operation(summary = "Статистика системы", description = "Возвращает общую статистику по всем таблицам")
    public ResponseEntity<SystemStatsDTO> getSystemStats() {
        logger.debug("Запрос статистики системы");

        long totalUsers = userRepository.count();
        long totalPrompts = promptRepository.count();
        long totalChannels = channelRepository.count();
        long totalNews = newsRepository.count();
        long totalAnalyzed = newsRepository.countByUserIdAndIsAnalyzed(null, true);

        SystemStatsDTO stats = new SystemStatsDTO(totalUsers, totalPrompts, totalChannels, totalNews, totalAnalyzed);

        return ResponseEntity.ok(stats);
    }

    /**
     * Очистить таблицу новостей (все пользователи).
     *
     * @param confirmation подтверждение ("CONFIRM")
     * @return успешный ответ
     */
    @DeleteMapping("/cleanup/news")
    @Operation(summary = "Очистить новости", description = "Удаляет все новости из базы (требует подтверждения)")
    public ResponseEntity<Void> cleanupNews(
            @RequestHeader("X-Confirmation") String confirmation) {
        validateConfirmation(confirmation);

        logger.warn("Запрос на очистку таблицы новостей");
        newsRepository.deleteAll();

        return ResponseEntity.noContent().build();
    }

    /**
     * Очистить таблицу каналов (все пользователи).
     *
     * @param confirmation подтверждение ("CONFIRM")
     * @return успешный ответ
     */
    @DeleteMapping("/cleanup/channels")
    @Operation(summary = "Очистить каналы", description = "Удаляет все каналы из базы (требует подтверждения)")
    public ResponseEntity<Void> cleanupChannels(
            @RequestHeader("X-Confirmation") String confirmation) {
        validateConfirmation(confirmation);

        logger.warn("Запрос на очистку таблицы каналов");
        channelRepository.deleteAll();

        return ResponseEntity.noContent().build();
    }

    /**
     * Очистить таблицу пользователей (все связанные данные каскадно).
     *
     * @param confirmation подтверждение ("CONFIRM")
     * @return успешный ответ
     */
    @DeleteMapping("/cleanup/users")
    @Operation(summary = "Очистить пользователей", description = "Удаляет всех пользователей каскадно")
    public ResponseEntity<Void> cleanupUsers(
            @RequestHeader("X-Confirmation") String confirmation) {
        validateConfirmation(confirmation);

        logger.warn("Запрос на очистку таблицы пользователей");
        userRepository.deleteAll();

        return ResponseEntity.noContent().build();
    }

    /**
     * Очистить все таблицы (полный сброс).
     *
     * @param confirmation подтверждение ("CONFIRM")
     * @return успешный ответ
     */
    @DeleteMapping("/cleanup/all")
    @Operation(summary = "Полная очистка", description = "Удаляет все данные из всех таблиц")
    public ResponseEntity<Void> cleanupAll(
            @RequestHeader("X-Confirmation") String confirmation) {
        validateConfirmation(confirmation);

        logger.warn("Запрос на полную очистку всех таблиц");

        // Порядок важен из-за внешних ключей
        analysisResultRepository.deleteAll();
        newsRepository.deleteAll();
        channelRepository.deleteAll();
        promptRepository.deleteAll();
        userRepository.deleteAll();

        return ResponseEntity.noContent().build();
    }

    /**
     * Проверяет подтверждение операции.
     *
     * @param confirmation значение подтверждения
     * @throws IllegalArgumentException если подтверждение некорректное
     */
    private void validateConfirmation(String confirmation) {
        if (!"CONFIRM".equals(confirmation)) {
            logger.warn("Некорректное подтверждение операции: {}", confirmation);
            throw new IllegalArgumentException("Требуется подтверждение через header X-Confirmation: CONFIRM");
        }
    }
}
