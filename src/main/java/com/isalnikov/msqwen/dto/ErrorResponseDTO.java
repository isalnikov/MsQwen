package com.isalnikov.msqwen.dto;

import java.time.LocalDateTime;

/**
 * DTO для представления ошибок в REST API.
 *
 * <p>Используется в {@link com.isalnikov.msqwen.exception.GlobalExceptionHandler}
 * для формирования стандартного ответа об ошибке.</p>
 *
 * @param status HTTP статус ошибки
 * @param error краткое описание ошибки
 * @param message подробное сообщение об ошибке
 * @param timestamp время возникновения ошибки
 * @author isalnikov
 * @version 1.0
 */
public record ErrorResponseDTO(
        Integer status,
        String error,
        String message,
        LocalDateTime timestamp
) {
}
