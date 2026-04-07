package com.isalnikov.msqwen.scheduler;

import com.isalnikov.msqwen.entity.Channel;
import com.isalnikov.msqwen.parser.NewsParserService;
import com.isalnikov.msqwen.repository.ChannelRepository;
import com.isalnikov.msqwen.repository.UserRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Планировщик для автоматического парсинга новостей из Telegram каналов.
 *
 * <p>Запускается по расписанию (cron) и парсит все активные каналы
 * всех зарегистрированных пользователей. Новые новости сохраняются в БД.
 * Используется Virtual Threads для конкурентного парсинга каналов.</p>
 *
 * <p>Расписание настраивается через application.yml:
 * scheduler.parse.cron (каждые 30 минут)</p>
 *
 * @author isalnikov
 * @version 1.0
 */
@Component
public class NewsParsingScheduler {

    /**
     * Логгер для записи событий планировщика.
     */
    private static final Logger logger = LoggerFactory.getLogger(NewsParsingScheduler.class);

    /**
     * Сервис для парсинга и сохранения новостей.
     */
    private final NewsParserService newsParserService;

    /**
     * Репозиторий для доступа к каналам.
     */
    private final ChannelRepository channelRepository;

    /**
     * Репозиторий для доступа к пользователям.
     */
    private final UserRepository userRepository;

    /**
     * Флаг включения планировщика парсинга.
     */
    @Value("${scheduler.parse.enabled:true}")
    private boolean parseEnabled;

    /**
     * Конструктор с внедрением зависимостей через конструктор.
     *
     * @param newsParserService сервис парсинга новостей
     * @param channelRepository репозиторий каналов
     * @param userRepository репозиторий пользователей
     */
    public NewsParsingScheduler(NewsParserService newsParserService,
                                ChannelRepository channelRepository,
                                UserRepository userRepository) {
        this.newsParserService = newsParserService;
        this.channelRepository = channelRepository;
        this.userRepository = userRepository;
    }

    /**
     * Автоматический парсинг всех активных каналов по расписанию.
     *
     * <p>Выполняется каждые 30 минут (настраивается через cron).
     * Проверяет флаг scheduler.parse.enabled - если false, задача пропускается.</p>
     */
    @Scheduled(cron = "${scheduler.parse.cron:0 */30 * * * *}")
    public void parseAllActiveChannels() {
        if (!parseEnabled) {
            logger.debug("Парсинг отключён через конфигурацию, пропускаем задачу");
            return;
        }

        logger.info("=== Запущен плановый парсинг всех активных каналов ===");

        try {
            // Получаем всех пользователей
            var allUsers = userRepository.findAll();
            logger.debug("Найдено пользователей для парсинга: {}", allUsers.size());

            int totalNews = 0;
            int totalChannels = 0;

            // Для каждого пользователя получаем активные каналы
            for (var user : allUsers) {
                List<Channel> activeChannels = channelRepository.findAllByUserIdAndIsActive(user.getId(), true);

                if (activeChannels.isEmpty()) {
                    logger.debug("У пользователя {} нет активных каналов", user.getId());
                    continue;
                }

                logger.info("Парсинг каналов пользователя {}: количество каналов {}",
                        user.getId(), activeChannels.size());

                int newsCount = newsParserService.parseAllChannels(user.getId(), activeChannels);
                totalNews += newsCount;
                totalChannels += activeChannels.size();
            }

            logger.info("=== Завершён плановый парсинг: обработано каналов={}, добавлено новостей={} ===",
                    totalChannels, totalNews);

        } catch (Exception e) {
            logger.error("Ошибка при плановом парсинге каналов: {}", e.getMessage(), e);
            // Не пробрасываем исключение чтобы не ломать планировщик
        }
    }
}
