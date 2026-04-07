package com.isalnikov.msqwen.config;

import com.isalnikov.msqwen.bot.TelegramBotCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Обработчик обновлений Telegram бота.
 *
 * <p>Принимает все обновления от Telegram и делегирует обработку
 * соответствующему обработчику команд. Это точка входа всех сообщений
 * от бота.</p>
 *
 * @author isalnikov
 * @version 1.0
 */
@Component
public class TelegramBotHandler {

    /**
     * Логгер для записи событий обработчика.
     */
    private static final Logger logger = LoggerFactory.getLogger(TelegramBotHandler.class);

    /**
     * Обработчик команд бота.
     */
    private final TelegramBotCommandHandler commandHandler;

    /**
     * Конструктор с внедрением зависимостей через конструктор.
     *
     * @param commandHandler обработчик команд
     */
    public TelegramBotHandler(TelegramBotCommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    /**
     * Обрабатывает обновление от Telegram.
     *
     * @param update обновление от Telegram
     */
    public void handleUpdate(Update update) {
        if (update == null) {
            logger.warn("Получено пустое обновление");
            return;
        }

        // Проверяем что это сообщение
        if (update.hasMessage()) {
            var message = update.getMessage();
            logger.debug("Получено сообщение от пользователя: id={}", message.getFrom().getId());

            // Обрабатываем команду или текст
            commandHandler.handleMessage(message);
        } else if (update.hasCallbackQuery()) {
            var callback = update.getCallbackQuery();
            logger.debug("Получен callback запрос от пользователя: id={}", callback.getFrom().getId());

            // Обрабатываем callback от inline клавиатуры
            commandHandler.handleCallback(callback);
        } else {
            logger.debug("Неподдерживаемый тип обновления: updateId={}", update.getUpdateId());
        }
    }
}
