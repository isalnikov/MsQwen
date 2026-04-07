package com.isalnikov.msqwen.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO для регистрации и передачи данных пользователя.
 * 
 * <p>Использует record для неизменяемости и лаконичности.
 * Все поля валидируются через Jakarta Validation.</p>
 * 
 * @param telegramId уникальный Telegram ID пользователя
 * @param username имя пользователя в Telegram
 * @param firstName имя пользователя
 * @param lastName фамилия пользователя
 * @author isalnikov
 * @version 1.0
 */
public record UserDTO(
        Long id,
        @NotNull(message = "Telegram ID не может быть пустым")
        Long telegramId,
        String username,
        @NotBlank(message = "Имя не может быть пустым")
        String firstName,
        String lastName,
        Boolean isActive
) {
    /**
     * Компактный конструктор для создания DTO из сущности User.
     *
     * @param user сущность пользователя
     */
    public UserDTO(com.isalnikov.msqwen.entity.User user) {
        this(
                user.getId(),
                user.getTelegramId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getIsActive()
        );
    }
}
