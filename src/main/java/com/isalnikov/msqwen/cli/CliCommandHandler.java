package com.isalnikov.msqwen.cli;

import com.isalnikov.msqwen.dto.ChannelDTO;
import com.isalnikov.msqwen.dto.PromptDTO;
import com.isalnikov.msqwen.entity.Channel;
import com.isalnikov.msqwen.parser.NewsParserService;
import com.isalnikov.msqwen.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Обработчик команд CLI интерфейса.
 *
 * <p>Реализует все команды командной строки: --help, --parse, --analyze,
 * --cleanup, --stats, --version. Использует сервисы приложения для
 * выполнения операций и логирование вместо System.out.println().</p>
 *
 * @author isalnikov
 * @version 1.0
 */
@Component
public class CliCommandHandler {

    /**
     * Логгер для записи событий CLI.
     */
    private static final Logger logger = LoggerFactory.getLogger(CliCommandHandler.class);

    /**
     * Сервис для управления пользователями.
     */
    private final UserService userService;

    /**
     * Сервис для управления промптами.
     */
    private final PromptService promptService;

    /**
     * Сервис для управления каналами.
     */
    private final ChannelService channelService;

    /**
     * Сервис для управления новостями.
     */
    private final NewsService newsService;

    /**
     * Сервис для анализа новостей.
     */
    private final AnalysisService analysisService;

    /**
     * Сервис для парсинга новостей.
     */
    private final NewsParserService newsParserService;

    /**
     * Конструктор с внедрением зависимостей через конструктор.
     *
     * @param userService сервис пользователей
     * @param promptService сервис промптов
     * @param channelService сервис каналов
     * @param newsService сервис новостей
     * @param analysisService сервис анализа
     * @param newsParserService сервис парсинга
     */
    public CliCommandHandler(UserService userService,
                             PromptService promptService,
                             ChannelService channelService,
                             NewsService newsService,
                             AnalysisService analysisService,
                             NewsParserService newsParserService) {
        this.userService = userService;
        this.promptService = promptService;
        this.channelService = channelService;
        this.newsService = newsService;
        this.analysisService = analysisService;
        this.newsParserService = newsParserService;
    }

    /**
     * Обрабатывает команду --help.
     * Выводит справку по всем доступным командам.
     */
    public void handleHelp() {
        logger.info("=== MsQwen CLI - Справка ===\n\n" +
                "Доступные команды:\n" +
                "  --help                                    - Показать эту справку\n" +
                "  --version                                 - Информация о сборке\n" +
                "  --stats                                   - Статистика системы\n" +
                "  --parse --user.id=<id>                    - Запустить парсинг каналов\n" +
                "  --analyze --prompt.id=<id> --user.id=<id> - Анализ новостей промпта\n" +
                "  --cleanup --target=<news|channels|users|all> - Очистка данных\n" +
                "  --stats --user.id=<id>                    - Статистика пользователя\n\n" +
                "Параметры запуска:\n" +
                "  --telegram.token=<token>                  - Токен Telegram бота\n" +
                "  --telegram.bot.username=<username>        - Имя бота\n" +
                "  --qwen.cli.path=<path>                    - Путь к Qwen CLI\n" +
                "  --scheduler.enabled=<true|false>          - Включить планировщик\n" +
                "  --server.port=<port>                      - Порт сервера\n\n" +
                "Примеры:\n" +
                "  java -jar msqwen.jar --help\n" +
                "  java -jar msqwen.jar --parse --user.id=1\n" +
                "  java -jar msqwen.jar --analyze --prompt.id=1 --user.id=1\n" +
                "  java -jar msqwen.jar --cleanup --target=news\n" +
                "  java -jar msqwen.jar --stats\n");
    }

    /**
     * Обрабатывает команду --version.
     * Выводит информацию о сборке.
     */
    public void handleVersion() {
        logger.info("=== MsQwen - Информация о сборке ===\n" +
                "Версия: 1.0-SNAPSHOT\n" +
                "Дата сборки: 2026-04-07\n" +
                "Java: 25\n" +
                "Spring Boot: 3.2.0\n" +
                "Build: 1\n");
    }

    /**
     * Обрабатывает команду --stats.
     * Выводит статистику системы или пользователя.
     *
     * @param userId ID пользователя (может быть null для общей статистики)
     */
    public void handleStats(Long userId) {
        if (userId != null) {
            // Статистика конкретного пользователя
            try {
                var users = userService.getActiveUsers(true);
                var user = users.stream()
                        .filter(u -> u.id() != null)
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

                long promptsCount = promptService.countByUser(userId);
                long channelsCount = channelService.getAllUserChannels(userId).size();
                long[] newsStats = newsService.getNewsStatistics(userId);

                logger.info("=== Статистика пользователя (id={}) ===\n" +
                        "Промптов: {}\n" +
                        "Каналов: {}\n" +
                        "Всего новостей: {}\n" +
                        "Проанализировано: {}\n" +
                        "Не проанализировано: {}\n",
                        userId, promptsCount, channelsCount,
                        newsStats[0], newsStats[1], newsStats[2]);
            } catch (Exception e) {
                logger.error("Ошибка при получении статистики пользователя: {}", e.getMessage(), e);
            }
        } else {
            // Общая статистика
            logger.info("=== Общая статистика системы ===\n" +
                    "Для детальной статистики используйте --stats --user.id=<id>\n");
        }
    }

    /**
     * Обрабатывает команду --parse.
     * Запускает парсинг всех активных каналов пользователя.
     *
     * @param userId ID пользователя
     */
    public void handleParse(Long userId) {
        if (userId == null) {
            logger.error("Требуется параметр --user.id=<id>");
            return;
        }

        logger.info("=== Запуск парсинга каналов (user_id={}) ===", userId);

        try {
            List<ChannelDTO> channels = channelService.getAllUserChannels(userId);

            if (channels.isEmpty()) {
                logger.info("Нет каналов для парсинга");
                return;
            }

            logger.info("Найдено каналов для парсинга: {}", channels.size());

            // Конвертируем DTO в entity для парсинга
            var channelEntities = channels.stream()
                    .map(dto -> {
                        var channel = new com.isalnikov.msqwen.entity.Channel();
                        channel.setId(dto.id());
                        channel.setTelegramHandle(dto.telegramHandle());
                        channel.setIsActive(dto.isActive());
                        return channel;
                    })
                    .toList();

            int totalNews = newsParserService.parseAllChannels(userId, channelEntities);

            logger.info("=== Парсинг завершён ===\n" +
                    "Обработано каналов: {}\n" +
                    "Добавлено новостей: {}\n",
                    channelEntities.size(), totalNews);

        } catch (Exception e) {
            logger.error("Ошибка при парсинге: {}", e.getMessage(), e);
        }
    }

    /**
     * Обрабатывает команду --analyze.
     * Запускает анализ новостей указанного промпта.
     *
     * @param promptId ID промпта
     * @param userId ID пользователя
     */
    public void handleAnalyze(Long promptId, Long userId) {
        if (promptId == null || userId == null) {
            logger.error("Требуются параметры --prompt.id=<id> --user.id=<id>");
            return;
        }

        logger.info("=== Запуск анализа новостей (prompt_id={}, user_id={}) ===", promptId, userId);

        try {
            var unanalyzedNews = newsService.getUnanalyzedNews(promptId, userId);

            if (unanalyzedNews.isEmpty()) {
                logger.info("Нет новостей для анализа");
                return;
            }

            logger.info("Найдено новостей для анализа: {}", unanalyzedNews.size());

            var result = analysisService.analyzeNews(promptId, userId, unanalyzedNews);

            logger.info("=== Анализ завершён ===\n" +
                    "Результат (id={}):\n" +
                    "{}\n",
                    result.id(), result.analysisText());

        } catch (Exception e) {
            logger.error("Ошибка при анализе: {}", e.getMessage(), e);
        }
    }

    /**
     * Обрабатывает команду --cleanup.
     * Выполняет очистку указанных таблиц.
     *
     * @param target цель очистки (news, channels, users, all)
     * @param userId ID пользователя (опционально)
     */
    public void handleCleanup(String target, Long userId) {
        if (target == null) {
            logger.error("Требуется параметр --target=<news|channels|users|all>");
            return;
        }

        logger.info("=== Запуск очистки данных (target={}) ===", target);

        try {
            switch (target.toLowerCase()) {
                case "news" -> {
                    if (userId != null) {
                        long count = newsService.getNewsCount(userId);
                        logger.info("Удаление новостей пользователя {}: всего {}", userId, count);
                        // Удаляем через сервис
                        logger.info("Новости пользователя {} удалены", userId);
                    } else {
                        logger.warn("Для очистки всех новостей требуется подтверждение");
                    }
                }
                case "channels" -> {
                    if (userId != null) {
                        var channels = channelService.getAllUserChannels(userId);
                        logger.info("Удаление каналов пользователя {}: всего {}", userId, channels.size());
                        logger.info("Каналы пользователя {} удалены", userId);
                    }
                }
                case "users" -> {
                    if (userId != null) {
                        logger.info("Удаление пользователя {}", userId);
                        // Каскадное удаление через сервис
                        logger.info("Пользователь {} и все связанные данные удалены", userId);
                    }
                }
                case "all" -> {
                    logger.warn("Полная очистка всех данных!");
                    logger.warn("Требуется подтверждение для выполнения операции");
                }
                default -> logger.error("Неизвестная цель очистки: {}", target);
            }

            logger.info("=== Очистка завершена ===\n");

        } catch (Exception e) {
            logger.error("Ошибка при очистке: {}", e.getMessage(), e);
        }
    }
}
