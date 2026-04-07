package com.isalnikov.msqwen.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO для запроса добавления канала.
 *
 * @param name название канала
 * @param telegramHandle handle канала в Telegram
 * @param telegramUrl URL канала в Telegram
 * @param description описание канала
 * @author isalnikov
 * @version 1.0
 */
public record ChannelCreateRequestDTO(
        @NotBlank String name,
        @NotBlank String telegramHandle,
        String telegramUrl,
        String description
) {
}
