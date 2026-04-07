package com.isalnikov.msqwen.exception;

/**
 * Исключение при отсутствии ресурса.
 * 
 * <p>Выбрасывается когда запрашиваемый ресурс не найден в базе данных.</p>
 * 
 * @author isalnikov
 * @version 1.0
 */
public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Создаёт исключение с сообщением.
     *
     * @param message сообщение об ошибке
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Создаёт исключение с сообщением и причиной.
     *
     * @param message сообщение об ошибке
     * @param cause причина исключения
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
