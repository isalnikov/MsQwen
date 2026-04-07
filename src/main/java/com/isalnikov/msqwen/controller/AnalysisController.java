package com.isalnikov.msqwen.controller;

import com.isalnikov.msqwen.dto.AnalysisResultDTO;
import com.isalnikov.msqwen.dto.AnalyzeRequestDTO;
import com.isalnikov.msqwen.dto.NewsDTO;
import com.isalnikov.msqwen.entity.News;
import com.isalnikov.msqwen.service.AnalysisService;
import com.isalnikov.msqwen.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API контроллер для управления результатами анализа новостей.
 *
 * <p>Предоставляет endpoints для запуска анализа получения результатов
 * и истории анализов. Результаты кешируются при пустом кеше выполняется
 * свежий анализ через Qwen CLI.
 * Все операции изолированы по user_id. Базовый путь: /api/v1/analysis</p>
 *
 * @author isalnikov
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/analysis")
@Tag(name = "Analysis API", description = "Операции анализа новостей")
public class AnalysisController {

    /**
     * Логгер для записи событий контроллера.
     */
    private static final Logger logger = LoggerFactory.getLogger(AnalysisController.class);

    /**
     * Сервис для анализа новостей.
     */
    private final AnalysisService analysisService;

    /**
     * Сервис для управления новостями.
     */
    private final NewsService newsService;

    /**
     * Конструктор с внедрением зависимостей через конструктор.
     *
     * @param analysisService сервис анализа
     * @param newsService сервис новостей
     */
    public AnalysisController(AnalysisService analysisService, NewsService newsService) {
        this.analysisService = analysisService;
        this.newsService = newsService;
    }

    /**
     * Запустить анализ новостей промпта.
     *
     * @param request DTO с идентификатором промпта
     * @param userId идентификатор пользователя
     * @return результат анализа
     */
    @PostMapping
    @Operation(summary = "Анализ новостей", description = "Запускает анализ новостей через Qwen CLI")
    public ResponseEntity<AnalysisResultDTO> analyzeNews(
            @Valid @RequestBody AnalyzeRequestDTO request,
            @Parameter(description = "ID пользователя") @RequestParam Long userId) {
        logger.info("Запрос на анализ: promptId={}, userId={}", request.promptId(), userId);

        // Получаем непроанализированные новости
        List<NewsDTO> newsToAnalyze = newsService.getUnanalyzedNews(request.promptId(), userId);

        if (newsToAnalyze.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        AnalysisResultDTO result = analysisService.analyzeNews(request.promptId(), userId, newsToAnalyze);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * Получить историю анализов промпта.
     *
     * @param promptId идентификатор промпта
     * @param userId идентификатор пользователя
     * @param pageable пагинация
     * @return страница результатов анализа
     */
    @GetMapping("/prompt/{promptId}")
    @Operation(summary = "История анализов", description = "Возвращает историю анализов промпта")
    public ResponseEntity<Page<AnalysisResultDTO>> getAnalysisHistory(
            @PathVariable Long promptId,
            @Parameter(description = "ID пользователя") @RequestParam Long userId,
            @PageableDefault(size = 20) Pageable pageable) {
        logger.debug("Запрос на получение истории анализов: promptId={}, userId={}", promptId, userId);

        Page<AnalysisResultDTO> history = analysisService.getAnalysisHistory(userId, pageable);
        return ResponseEntity.ok(history);
    }

    /**
     * Получить результат анализа по ID.
     *
     * @param id идентификатор результата
     * @param userId идентификатор пользователя
     * @return результат анализа
     */
    @GetMapping("/{id}")
    @Operation(summary = "Результат анализа", description = "Возвращает результат анализа по ID")
    public ResponseEntity<AnalysisResultDTO> getAnalysis(
            @PathVariable Long id,
            @Parameter(description = "ID пользователя") @RequestParam Long userId) {
        logger.debug("Запрос на получение результата анализа: id={}, userId={}", id, userId);

        var history = analysisService.getAnalysisHistory(userId, Pageable.ofSize(100));
        var result = history.getContent().stream()
                .filter(a -> a.id().equals(id))
                .findFirst();

        return result.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Удалить результаты анализа промпта.
     *
     * @param promptId идентификатор промпта
     * @param userId идентификатор пользователя
     * @return успешный ответ
     */
    @DeleteMapping("/prompt/{promptId}")
    @Operation(summary = "Удалить результаты анализа", description = "Удаляет все результаты анализа промпта")
    public ResponseEntity<Void> deleteAnalysisByPrompt(
            @PathVariable Long promptId,
            @Parameter(description = "ID пользователя") @RequestParam Long userId) {
        logger.info("Запрос на удаление результатов анализа: promptId={}, userId={}", promptId, userId);

        analysisService.deleteAnalysisByPrompt(promptId, userId);
        return ResponseEntity.noContent().build();
    }
}
