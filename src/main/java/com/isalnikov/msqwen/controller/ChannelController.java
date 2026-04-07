package com.isalnikov.msqwen.controller;

import com.isalnikov.msqwen.dto.ChannelCreateRequestDTO;
import com.isalnikov.msqwen.dto.ChannelDTO;
import com.isalnikov.msqwen.service.ChannelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API контроллер для управления каналами Telegram.
 *
 * <p>Предоставляет endpoints для создания получения обновления и удаления каналов.
 * При удалении канала каскадно удаляются все новости.
 * Все операции изолированы по user_id. Базовый путь: /api/v1/channels</p>
 *
 * @author isalnikov
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/channels")
@Tag(name = "Channel API", description = "Операции управления каналами")
public class ChannelController {

    /**
     * Логгер для записи событий контроллера.
     */
    private static final Logger logger = LoggerFactory.getLogger(ChannelController.class);

    /**
     * Сервис для управления каналами.
     */
    private final ChannelService channelService;

    /**
     * Конструктор с внедрением зависимостей через конструктор.
     *
     * @param channelService сервис каналов
     */
    public ChannelController(ChannelService channelService) {
        this.channelService = channelService;
    }

    /**
     * Добавить канал к промпту.
     *
     * @param promptId идентификатор промпта
     * @param userId идентификатор пользователя
     * @param request DTO с данными канала
     * @return созданный канал
     */
    @PostMapping
    @Operation(summary = "Добавить канал", description = "Добавляет канал к указанному промпту")
    public ResponseEntity<ChannelDTO> createChannel(
            @Parameter(description = "ID промпта") @RequestParam Long promptId,
            @Parameter(description = "ID пользователя") @RequestParam Long userId,
            @Valid @RequestBody ChannelCreateRequestDTO request) {
        logger.info("Запрос на создание канала: promptId={}, userId={}, handle={}",
                promptId, userId, request.telegramHandle());

        ChannelDTO channelDTO = channelService.createChannel(promptId, userId,
                request.name(), request.telegramHandle(), request.telegramUrl(), request.description());

        return ResponseEntity.status(HttpStatus.CREATED).body(channelDTO);
    }

    /**
     * Список каналов промпта.
     *
     * @param promptId идентификатор промпта
     * @param userId идентификатор пользователя
     * @return список каналов
     */
    @GetMapping("/prompt/{promptId}")
    @Operation(summary = "Список каналов", description = "Возвращает все каналы промпта")
    public ResponseEntity<List<ChannelDTO>> getChannelsByPrompt(
            @PathVariable Long promptId,
            @Parameter(description = "ID пользователя") @RequestParam Long userId) {
        logger.debug("Запрос на получение каналов: promptId={}, userId={}", promptId, userId);

        List<ChannelDTO> channels = channelService.getChannelsByPrompt(promptId, userId);
        return ResponseEntity.ok(channels);
    }

    /**
     * Получить канал по ID с проверкой прав.
     *
     * @param id идентификатор канала
     * @param userId идентификатор пользователя
     * @return данные канала
     */
    @GetMapping("/{id}")
    @Operation(summary = "Получить канал", description = "Возвращает канал по ID с проверкой прав")
    public ResponseEntity<ChannelDTO> getChannel(
            @PathVariable Long id,
            @Parameter(description = "ID пользователя") @RequestParam Long userId) {
        logger.debug("Запрос на получение канала: id={}, userId={}", id, userId);

        ChannelDTO channelDTO = channelService.getChannel(id, userId);
        return ResponseEntity.ok(channelDTO);
    }

    /**
     * Обновить канал с проверкой прав.
     *
     * @param id идентификатор канала
     * @param userId идентификатор пользователя
     * @param request DTO с обновлёнными данными
     * @return обновлённый канал
     */
    @PutMapping("/{id}")
    @Operation(summary = "Обновить канал", description = "Обновляет канал с проверкой прав")
    public ResponseEntity<ChannelDTO> updateChannel(
            @PathVariable Long id,
            @Parameter(description = "ID пользователя") @RequestParam Long userId,
            @Valid @RequestBody ChannelCreateRequestDTO request) {
        logger.info("Запрос на обновление канала: id={}, userId={}", id, userId);

        ChannelDTO channelDTO = channelService.updateChannel(id, userId,
                request.name(), request.telegramHandle(), request.telegramUrl(), request.description());

        return ResponseEntity.ok(channelDTO);
    }

    /**
     * Удалить канал каскадно (все новости канала).
     *
     * @param id идентификатор канала
     * @param userId идентификатор пользователя
     * @return успешный ответ
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить канал", description = "Удаляет канал каскадно со всеми новостями")
    public ResponseEntity<Void> deleteChannel(
            @PathVariable Long id,
            @Parameter(description = "ID пользователя") @RequestParam Long userId) {
        logger.info("Запрос на удаление канала: id={}, userId={}", id, userId);

        channelService.deleteChannel(id, userId);
        return ResponseEntity.noContent().build();
    }
}
