package com.isalnikov.msqwen.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * DTO для передачи данных канала Telegram.
 * 
 * <p>Использует record для неизменяемости.
 * Содержит информацию о канале для парсинга новостей.</p>
 * 
 * @param id уникальный идентификатор канала
 * @param promptId идентификатор промпта
 * @param userId идентификатор пользователя
 * @param name название канала
 * @param telegramHandle handle канала (@channel_name)
 * @param telegramUrl URL канала
 * @param description описание канала
 * @param isActive флаг активности
 * @param createdAt дата создания
 * @param lastParsedAt дата последнего парсинга
 * @author isalnikov
 * @version 1.0
 */
public record ChannelDTO(
        Long id,
        Long promptId,
        Long userId,
        @NotBlank(message = "Название канала не может быть пустым")
        String name,
        @NotBlank(message = "Telegram handle не может быть пустым")
        String telegramHandle,
        String telegramUrl,
        String description,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime lastParsedAt
) {
    /**
     * Компактный конструктор для создания DTO из сущности Channel.
     *
     * @param channel сущность канала
     */
    public ChannelDTO(com.isalnikov.msqwen.entity.Channel channel) {
        this(
                channel.getId(),
                channel.getPrompt() != null ? channel.getPrompt().getId() : null,
                channel.getUser() != null ? channel.getUser().getId() : null,
                channel.getName(),
                channel.getTelegramHandle(),
                channel.getTelegramUrl(),
                channel.getDescription(),
                channel.getIsActive(),
                channel.getCreatedAt(),
                channel.getLastParsedAt()
        );
    }
}
