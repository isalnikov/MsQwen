package com.isalnikov.msqwen.controller;

import com.isalnikov.msqwen.dto.PromptCreateRequestDTO;
import com.isalnikov.msqwen.dto.PromptDTO;
import com.isalnikov.msqwen.service.PromptService;
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
 * REST API контроллер для управления промптами.
 *
 * <p>Предоставляет endpoints для создания получения обновления и удаления промптов.
 * При удалении промпта каскадно удаляются каналы новости и результаты анализа.
 * Все операции изолированы по user_id. Базовый путь: /api/v1/prompts</p>
 *
 * @author isalnikov
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/prompts")
@Tag(name = "Prompt API", description = "Операции управления промптами")
public class PromptController {

    /**
     * Логгер для записи событий контроллера.
     */
    private static final Logger logger = LoggerFactory.getLogger(PromptController.class);

    /**
     * Сервис для управления промптами.
     */
    private final PromptService promptService;

    /**
     * Конструктор с внедрением зависимостей через конструктор.
     *
     * @param promptService сервис промптов
     */
    public PromptController(PromptService promptService) {
        this.promptService = promptService;
    }

    /**
     * Создать новый промпт для пользователя.
     *
     * @param userId идентификатор пользователя
     * @param request DTO с данными промпта
     * @return созданный промпт
     */
    @PostMapping
    @Operation(summary = "Создать промпт", description = "Создаёт новый промпт для указанного пользователя")
    public ResponseEntity<PromptDTO> createPrompt(
            @Parameter(description = "ID пользователя") @RequestParam Long userId,
            @Valid @RequestBody PromptCreateRequestDTO request) {
        logger.info("Запрос на создание промпта: userId={}, name={}", userId, request.name());

        PromptDTO promptDTO = promptService.createPrompt(userId, request.name(), request.description(), request.promptText());
        return ResponseEntity.status(HttpStatus.CREATED).body(promptDTO);
    }

    /**
     * Получить список промптов пользователя.
     *
     * @param userId идентификатор пользователя
     * @return список промптов
     */
    @GetMapping
    @Operation(summary = "Список промптов", description = "Возвращает все промпты пользователя")
    public ResponseEntity<List<PromptDTO>> getUserPrompts(
            @Parameter(description = "ID пользователя") @RequestParam Long userId) {
        logger.debug("Запрос на получение промптов: userId={}", userId);

        List<PromptDTO> prompts = promptService.getUserPrompts(userId);
        return ResponseEntity.ok(prompts);
    }

    /**
     * Получить промпт по ID с проверкой прав.
     *
     * @param id идентификатор промпта
     * @param userId идентификатор пользователя
     * @return данные промпта
     */
    @GetMapping("/{id}")
    @Operation(summary = "Получить промпт", description = "Возвращает промпт по ID с проверкой прав")
    public ResponseEntity<PromptDTO> getPrompt(
            @PathVariable Long id,
            @Parameter(description = "ID пользователя") @RequestParam Long userId) {
        logger.debug("Запрос на получение промпта: id={}, userId={}", id, userId);

        PromptDTO promptDTO = promptService.getPrompt(id, userId);
        return ResponseEntity.ok(promptDTO);
    }

    /**
     * Обновить промпт с проверкой прав.
     *
     * @param id идентификатор промпта
     * @param userId идентификатор пользователя
     * @param request DTO с обновлёнными данными
     * @return обновлённый промпт
     */
    @PutMapping("/{id}")
    @Operation(summary = "Обновить промпт", description = "Обновляет промпт с проверкой прав")
    public ResponseEntity<PromptDTO> updatePrompt(
            @PathVariable Long id,
            @Parameter(description = "ID пользователя") @RequestParam Long userId,
            @Valid @RequestBody PromptCreateRequestDTO request) {
        logger.info("Запрос на обновление промпта: id={}, userId={}", id, userId);

        PromptDTO promptDTO = promptService.updatePrompt(id, userId, request.name(), request.description(), request.promptText());
        return ResponseEntity.ok(promptDTO);
    }

    /**
     * Удалить промпт каскадно (каналы, новости, результаты анализа).
     *
     * @param id идентификатор промпта
     * @param userId идентификатор пользователя
     * @return успешный ответ
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить промпт", description = "Удаляет промпт каскадно со всеми связанными данными")
    public ResponseEntity<Void> deletePrompt(
            @PathVariable Long id,
            @Parameter(description = "ID пользователя") @RequestParam Long userId) {
        logger.info("Запрос на удаление промпта: id={}, userId={}", id, userId);

        promptService.deletePrompt(id, userId);
        return ResponseEntity.noContent().build();
    }
}
