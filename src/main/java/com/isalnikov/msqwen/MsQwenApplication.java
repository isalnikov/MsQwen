package com.isalnikov.msqwen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Главный класс приложения MsQwen "Госпожа Qwen".
 * 
 * <p>Приложение для парсинга новостей из публичных Telegram-каналов
 * через https://t.me/s/ и их анализа через Qwen Code CLI.</p>
 * 
 * <p>Функциональность:</p>
 * <ul>
 *   <li>Парсинг новостей из Telegram каналов через Jsoup</li>
 *   <li>Сохранение новостей в H2 базу данных</li>
 *   <li>REST API + Swagger документация</li>
 *   <li>Автоматическая обработка новостей по расписанию</li>
 *   <li>Кеширование результатов анализа</li>
 *   <li>Консольный режим для запуска из командной строки</li>
 *   <li>Telegram бот с интерактивными командами</li>
 * </ul>
 * 
 * @author isalnikov
 * @version 1.0-SNAPSHOT
 */
@SpringBootApplication
@EnableScheduling
@EnableCaching
public class MsQwenApplication {

    /**
     * Логгер для записи событий жизненного цикла приложения.
     */
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MsQwenApplication.class);

    /**
     * Точка входа в приложение.
     * 
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        logger.info("Запуск приложения MsQwen...");
        SpringApplication.run(MsQwenApplication.class, args);
        logger.info("Приложение MsQwen успешно запущено!");
    }
}
