package com.isalnikov.msqwen.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO для запроса регистрации пользователя.
 *
 * @param telegramId идентификатор пользователя в Telegram
 * @param username имя пользователя в Telegram
 * @param firstName имя пользователя
 * @param lastName фамилия пользователя
 * @author isalnikov
 * @version 1.0
 */
public record UserCreateRequestDTO(
        @NotNull Long telegramId,
        String username,
        String firstName,
        String lastName
) {
}
