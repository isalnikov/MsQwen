package com.isalnikov.msqwen.controller;

import com.isalnikov.msqwen.dto.NewsDTO;
import com.isalnikov.msqwen.dto.ParseRequestDTO;
import com.isalnikov.msqwen.parser.NewsParserService;
import com.isalnikov.msqwen.service.ChannelService;
import com.isalnikov.msqwen.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
 * REST API контроллер для управления новостями.
 *
 * <p>Предоставляет endpoints для получения новостей запуска парсинга
 * и удаления новостей. Новости содержат метрики вовлечённости.
 * Все операции изолированы по user_id. Базовый путь: /api/v1/news</p>
 *
 * @author isalnikov
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/news")
@Tag(name = "News API", description = "Операции управления новостями")
public class NewsController {

    /**
     * Логгер для записи событий контроллера.
     */
    private static final Logger logger = LoggerFactory.getLogger(NewsController.class);

    /**
     * Сервис для управления новостями.
     */
    private final NewsService newsService;

    /**
     * Сервис для парсинга новостей.
     */
    private final NewsParserService newsParserService;

    /**
     * Сервис для управления каналами.
     */
    private final ChannelService channelService;

    /**
     * Конструктор с внедрением зависимостей через конструктор.
     *
     * @param newsService сервис новостей
     * @param newsParserService сервис парсинга новостей
     * @param channelService сервис каналов
     */
    public NewsController(NewsService newsService,
                          NewsParserService newsParserService,
                          ChannelService channelService) {
        this.newsService = newsService;
        this.newsParserService = newsParserService;
        this.channelService = channelService;
    }

    /**
     * Получить новости промпта с пагинацией.
     *
     * @param promptId идентификатор промпта
     * @param userId идентификатор пользователя
     * @param pageable пагинация
     * @return страница новостей
     */
    @GetMapping("/prompt/{promptId}")
    @Operation(summary = "Новости промпта", description = "Возвращает новости промпта с пагинацией")
    public ResponseEntity<Page<NewsDTO>> getNewsByPrompt(
            @PathVariable Long promptId,
            @Parameter(description = "ID пользователя") @RequestParam Long userId,
            @PageableDefault(size = 20) Pageable pageable) {
        logger.debug("Запрос на получение новостей: promptId={}, userId={}", promptId, userId);

        Page<NewsDTO> newsPage = newsService.getNewsByPrompt(promptId, userId, pageable);
        return ResponseEntity.ok(newsPage);
    }

    /**
     * Получить новость по ID с проверкой прав.
     *
     * @param id идентификатор новости
     * @param userId идентификатор пользователя
     * @return данные новости
     */
    @GetMapping("/{id}")
    @Operation(summary = "Получить новость", description = "Возвращает новость по ID с проверкой прав")
    public ResponseEntity<NewsDTO> getNews(
            @PathVariable Long id,
            @Parameter(description = "ID пользователя") @RequestParam Long userId) {
        logger.debug("Запрос на получение новости: id={}, userId={}", id, userId);

        NewsDTO newsDTO = newsService.getNews(id, userId);
        return ResponseEntity.ok(newsDTO);
    }

    /**
     * Запустить парсинг вручную.
     *
     * @param request DTO с идентификатором промпта
     * @param userId идентификатор пользователя
     * @return количество добавленных новостей
     * @apiNote Асинхронная операция (возвращает сразу результат)
     */
    @PostMapping("/parse")
    @Operation(summary = "Запустить парсинг", description = "Запускает парсинг каналов промпта")
    public ResponseEntity<String> parseNews(
            @Valid @RequestBody ParseRequestDTO request,
            @Parameter(description = "ID пользователя") @RequestParam Long userId) {
        logger.info("Запрос на парсинг: promptId={}, userId={}", request.promptId(), userId);

        // Получаем все каналы промпта
        var channels = channelService.getChannelsByPrompt(request.promptId(), userId);

        if (channels.isEmpty()) {
            return ResponseEntity.badRequest().body("Нет активных каналов для промпта");
        }

        // Конвертируем DTO в entity для парсинга
        var channelEntities = channels.stream()
                .map(dto -> {
                    var channel = new com.isalnikov.msqwen.entity.Channel();
                    channel.setId(dto.id());
                    channel.setTelegramHandle(dto.telegramHandle());
                    channel.setIsActive(dto.isActive());
                    return channel;
                })
                .toList();

        int newsCount = newsParserService.parseAllChannels(userId, channelEntities);

        return ResponseEntity.ok("Парсинг завершён, добавлено новостей: " + newsCount);
    }

    /**
     * Удалить новости промпта.
     *
     * @param promptId идентификатор промпта
     * @param userId идентификатор пользователя
     * @return успешный ответ
     */
    @DeleteMapping("/prompt/{promptId}")
    @Operation(summary = "Удалить новости промпта", description = "Удаляет все новости промпта")
    public ResponseEntity<Void> deleteNewsByPrompt(
            @PathVariable Long promptId,
            @Parameter(description = "ID пользователя") @RequestParam Long userId) {
        logger.info("Запрос на удаление новостей промпта: promptId={}, userId={}", promptId, userId);

        newsService.deleteNewsByPrompt(promptId, userId);
        return ResponseEntity.noContent().build();
    }
}
