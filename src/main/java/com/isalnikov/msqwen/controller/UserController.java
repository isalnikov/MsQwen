package com.isalnikov.msqwen.controller;

import com.isalnikov.msqwen.dto.UserCreateRequestDTO;
import com.isalnikov.msqwen.dto.UserDTO;
import com.isalnikov.msqwen.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API контроллер для управления пользователями.
 *
 * <p>Предоставляет endpoints для регистрации, получения, обновления и удаления пользователей.
 * Все операции изолированы по user_id. Базовый путь: /api/v1/users</p>
 *
 * @author isalnikov
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User API", description = "Операции управления пользователями")
public class UserController {

    /**
     * Логгер для записи событий контроллера.
     */
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    /**
     * Сервис для управления пользователями.
     */
    private final UserService userService;

    /**
     * Конструктор с внедрением зависимостей через конструктор.
     *
     * @param userService сервис пользователей
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Регистрация нового пользователя.
     *
     * @param request DTO с данными пользователя
     * @return зарегистрированный пользователь
     */
    @PostMapping("/register")
    @Operation(summary = "Регистрация пользователя", description = "Создаёт нового пользователя по telegramId")
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody UserCreateRequestDTO request) {
        logger.info("Запрос на регистрацию пользователя: telegramId={}", request.telegramId());

        UserDTO userDTO = userService.registerUser(
                request.telegramId(),
                request.username(),
                request.firstName(),
                request.lastName()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(userDTO);
    }

    /**
     * Получить текущего пользователя по telegramId.
     *
     * @param telegramId идентификатор пользователя в Telegram
     * @return данные пользователя
     */
    @GetMapping("/me")
    @Operation(summary = "Получить пользователя", description = "Возвращает данные пользователя по telegramId")
    public ResponseEntity<UserDTO> getCurrentUser(
            @Parameter(description = "Telegram ID пользователя") @RequestParam Long telegramId) {
        logger.debug("Запрос на получение пользователя: telegramId={}", telegramId);

        UserDTO userDTO = userService.getUserByTelegramId(telegramId);
        return ResponseEntity.ok(userDTO);
    }

    /**
     * Обновить данные пользователя.
     *
     * @param telegramId идентификатор пользователя в Telegram
     * @param request DTO с обновлёнными данными
     * @return обновлённые данные пользователя
     */
    @PutMapping("/me")
    @Operation(summary = "Обновить пользователя", description = "Обновляет данные пользователя")
    public ResponseEntity<UserDTO> updateUser(
            @Parameter(description = "Telegram ID пользователя") @RequestParam Long telegramId,
            @Valid @RequestBody UserCreateRequestDTO request) {
        logger.info("Запрос на обновление пользователя: telegramId={}", telegramId);

        UserDTO userDTO = userService.updateUser(telegramId, request.username(), request.firstName(), request.lastName());
        return ResponseEntity.ok(userDTO);
    }

    /**
     * Удалить пользователя и все связанные данные (каскадно).
     *
     * @param telegramId идентификатор пользователя в Telegram
     * @return успешный ответ
     */
    @DeleteMapping("/me")
    @Operation(summary = "Удалить пользователя", description = "Удаляет пользователя и все связанные данные каскадно")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "Telegram ID пользователя") @RequestParam Long telegramId) {
        logger.info("Запрос на удаление пользователя: telegramId={}", telegramId);

        userService.deleteUser(telegramId);
        return ResponseEntity.noContent().build();
    }
}
