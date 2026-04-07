package com.isalnikov.msqwen.scheduler;

import com.isalnikov.msqwen.entity.News;
import com.isalnikov.msqwen.repository.NewsRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Планировщик для очистки старых новостей из базы данных.
 *
 * <p>Запускается по расписанию (cron) и удаляет новости старше 30 дней
 * (настраивается через application.yml). Это предотвращает разрастание базы данных
 * и сохраняет только актуальные новости.</p>
 *
 * <p>Расписание настраивается через application.yml:
 * {@code scheduler.cleanup.cron:0 0 3 * * *} (каждый день в 3:00)</p>
 *
 * @author isalnikov
 * @version 1.0
 */
@Component
public class NewsCleanupScheduler {

    /**
     * Логгер для записи событий планировщика.
     */
    private static final Logger logger = LoggerFactory.getLogger(NewsCleanupScheduler.class);

    /**
     * Репозиторий для доступа к новостям.
     */
    private final NewsRepository newsRepository;

    /**
     * Флаг включения планировщика очистки.
     */
    @Value("${scheduler.cleanup.enabled:true}")
    private boolean cleanupEnabled;

    /**
     * Количество дней после которых новости удаляются.
     */
    @Value("${news.cleanup.days:30}")
    private int cleanupDays;

    /**
     * Конструктор с внедрением зависимостей через конструктор.
     *
     * @param newsRepository репозиторий новостей
     */
    public NewsCleanupScheduler(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    /**
     * Автоматическая очистка старых новостей по расписанию.
     *
     * <p>Выполняется каждый день в 3:00 (настраивается через cron).
     * Проверяет флаг scheduler.cleanup.enabled - если false, задача пропускается.
     * Удаляет новости старше указанной даты (по умолчанию 30 дней).</p>
     */
    @Scheduled(cron = "${scheduler.cleanup.cron:0 0 3 * * *}")
    @Transactional
    public void deleteOldNews() {
        if (!cleanupEnabled) {
            logger.debug("Очистка старых новостей отключена через конфигурацию, пропускаем задачу");
            return;
        }

        logger.info("=== Запущена плановая очистка старых новостей (старше {} дней) ===", cleanupDays);

        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(cleanupDays);

            // Находим все новости старше указанной даты
            List<News> oldNews = newsRepository.findAllByPublicationDateBefore(cutoffDate);

            if (oldNews.isEmpty()) {
                logger.info("Нет старых новостей для удаления (новостей старше {} дней нет)", cleanupDays);
                return;
            }

            logger.info("Найдено старых новостей для удаления: {}", oldNews.size());

            // Удаляем все старые новости
            newsRepository.deleteAll(oldNews);

            logger.info("=== Завершена плановая очистка: удалено новостей={} ===", oldNews.size());

        } catch (Exception e) {
            logger.error("Ошибка при плановой очистке старых новостей: {}", e.getMessage(), e);
            // Не пробрасываем исключение чтобы не ломать планировщик
        }
    }
}
