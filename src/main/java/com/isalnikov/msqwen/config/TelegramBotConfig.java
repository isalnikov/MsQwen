package com.isalnikov.msqwen.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Конфигурация Telegram бота.
 *
 * <p>Создаёт и настраивает бин TelegramLongPollingBot
 * с параметрами из application.yml.</p>
 *
 * @author isalnikov
 * @version 1.0
 */
@Configuration
public class TelegramBotConfig {

    /**
     * Токен бота из Telegram BotFather.
     */
    @Value("${telegram.bot.token}")
    private String botToken;

    /**
     * Имя пользователя бота.
     */
    @Value("${telegram.bot.username}")
    private String botUsername;

    /**
     * Создаёт бин Telegram бота.
     *
     * @param telegramBotHandler обработчик сообщений бота
     * @return настроенный бин бота
     */
    @Bean
    public TelegramLongPollingBot telegramBot(TelegramBotHandler telegramBotHandler) {
        TelegramLongPollingBot bot = new TelegramLongPollingBot() {
            @Override
            public String getBotUsername() {
                return botUsername;
            }

            @Override
            public String getBotToken() {
                return botToken;
            }

            @Override
            public void onUpdateReceived(Update update) {
                telegramBotHandler.handleUpdate(update);
            }
        };
        return bot;
    }
}
