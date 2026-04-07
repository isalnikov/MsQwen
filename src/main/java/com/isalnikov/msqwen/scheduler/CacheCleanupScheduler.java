package com.isalnikov.msqwen.scheduler;

import com.isalnikov.msqwen.repository.AnalysisResultRepository;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Планировщик для очистки просроченных записей кеша результатов анализа.
 *
 * <p>Запускается по расписанию (cron) и удаляет записи из таблицы analysis_results
 * у которых истёк срок действия кеша (cache_expires_at < now).
 * Это предотвращает разрастание таблицы и освобождает место.</p>
 *
 * <p>Расписание настраивается через application.yml:
 * {@code scheduler.cache.cron:0 0 4 * * *} (каждый день в 4:00)</p>
 *
 * @author isalnikov
 * @version 1.0
 */
@Component
public class CacheCleanupScheduler {

    /**
     * Логгер для записи событий планировщика.
     */
    private static final Logger logger = LoggerFactory.getLogger(CacheCleanupScheduler.class);

    /**
     * Репозиторий для доступа к результатам анализа.
     */
    private final AnalysisResultRepository analysisResultRepository;

    /**
     * Флаг включения планировщика очистки кеша.
     */
    @Value("${scheduler.cache.enabled:true}")
    private boolean cacheCleanupEnabled;

    /**
     * Конструктор с внедрением зависимостей через конструктор.
     *
     * @param analysisResultRepository репозиторий результатов анализа
     */
    public CacheCleanupScheduler(AnalysisResultRepository analysisResultRepository) {
        this.analysisResultRepository = analysisResultRepository;
    }

    /**
     * Автоматическая очистка просроченных записей кеша по расписанию.
     *
     * <p>Выполняется каждый день в 4:00 (настраивается через cron).
     * Проверяет флаг scheduler.cache.enabled - если false, задача пропускается.
     * Удаляет записи у которых cache_expires_at < current_time.</p>
     */
    @Scheduled(cron = "${scheduler.cache.cron:0 0 4 * * *}")
    @Transactional
    public void cleanupExpiredCache() {
        if (!cacheCleanupEnabled) {
            logger.debug("Очистка кеша отключена через конфигурацию, пропускаем задачу");
            return;
        }

        logger.info("=== Запущена плановая очистка просроченного кеша ===");

        try {
            LocalDateTime now = LocalDateTime.now();

            // Считаем количество записей до удаления (для логирования)
            long countBefore = analysisResultRepository.count();

            // Удаляем просроченные записи
            analysisResultRepository.deleteByCacheExpiresAtBefore(now);

            long countAfter = analysisResultRepository.count();
            long deleted = countBefore - countAfter;

            logger.info("=== Завершена очистка кеша: удалено записей={} (было={}, стало={}) ===",
                    deleted, countBefore, countAfter);

        } catch (Exception e) {
            logger.error("Ошибка при плановой очистке кеша: {}", e.getMessage(), e);
            // Не пробрасываем исключение чтобы не ломать планировщик
        }
    }
}
