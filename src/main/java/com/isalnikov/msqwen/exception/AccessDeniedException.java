package com.isalnikov.msqwen.exception;

/**
 * Исключение при ошибке доступа к ресурсу.
 * 
 * <p>Выбрасывается когда пользователь пытается получить доступ
 * к ресурсу который ему не принадлежит.</p>
 * 
 * @author isalnikov
 * @version 1.0
 */
public class AccessDeniedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Создаёт исключение с сообщением.
     *
     * @param message сообщение об ошибке
     */
    public AccessDeniedException(String message) {
        super(message);
    }

    /**
     * Создаёт исключение с сообщением и причиной.
     *
     * @param message сообщение об ошибке
     * @param cause причина исключения
     */
    public AccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
}
