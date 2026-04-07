package com.isalnikov.msqwen.scheduler;

import com.isalnikov.msqwen.dto.AnalysisResultDTO;
import com.isalnikov.msqwen.dto.NewsDTO;
import com.isalnikov.msqwen.entity.News;
import com.isalnikov.msqwen.entity.Prompt;
import com.isalnikov.msqwen.repository.NewsRepository;
import com.isalnikov.msqwen.repository.PromptRepository;
import com.isalnikov.msqwen.service.AnalysisService;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Планировщик для автоматического анализа новостей через Qwen CLI.
 *
 * <p>Запускается по расписанию (cron) и анализирует все непроанализированные новости.
 * Новости приоритизируются по engagement score (более вовлечённые первыми).
 * Результаты анализа сохраняются в БД и кешируются.</p>
 *
 * <p>Расписание настраивается через application.yml:
 * {@code scheduler.analyze.cron:0 0 * * * *} (каждый час)</p>
 *
 * @author isalnikov
 * @version 1.0
 */
@Component
public class NewsAnalysisScheduler {

    /**
     * Логгер для записи событий планировщика.
     */
    private static final Logger logger = LoggerFactory.getLogger(NewsAnalysisScheduler.class);

    /**
     * Сервис для анализа новостей через Qwen CLI.
     */
    private final AnalysisService analysisService;

    /**
     * Репозиторий для доступа к новостям.
     */
    private final NewsRepository newsRepository;

    /**
     * Репозиторий для доступа к промптам.
     */
    private final PromptRepository promptRepository;

    /**
     * Флаг включения планировщика анализа.
     */
    @Value("${scheduler.analyze.enabled:true}")
    private boolean analyzeEnabled;

    /**
     * Максимальное количество новостей для анализа за один раз.
     */
    @Value("${scheduler.analyze.max-news:100}")
    private int maxNewsToAnalyze;

    /**
     * Конструктор с внедрением зависимостей через конструктор.
     *
     * @param analysisService сервис анализа новостей
     * @param newsRepository репозиторий новостей
     * @param promptRepository репозиторий промптов
     */
    public NewsAnalysisScheduler(AnalysisService analysisService,
                                 NewsRepository newsRepository,
                                 PromptRepository promptRepository) {
        this.analysisService = analysisService;
        this.newsRepository = newsRepository;
        this.promptRepository = promptRepository;
    }

    /**
     * Автоматический анализ непроанализированных новостей по расписанию.
     *
     * <p>Выполняется каждый час (настраивается через cron).
     * Проверяет флаг scheduler.analyze.enabled - если false, задача пропускается.
     * Новости приоритизируются по engagement score.</p>
     */
    @Scheduled(cron = "${scheduler.analyze.cron:0 0 * * * *}")
    public void analyzeNewNews() {
        if (!analyzeEnabled) {
            logger.debug("Анализ новостей отключён через конфигурацию, пропускаем задачу");
            return;
        }

        logger.info("=== Запущен плановый анализ новостей ===");

        try {
            // Получаем все непроанализированные новости
            var allNews = newsRepository.findAll().stream()
                    .filter(news -> Boolean.FALSE.equals(news.getIsAnalyzed()))
                    .sorted(Comparator.comparing(News::getEngagementScore).reversed())
                    .limit(maxNewsToAnalyze)
                    .toList();

            if (allNews.isEmpty()) {
                logger.info("Нет новостей для анализа");
                return;
            }

            logger.info("Найдено новостей для анализа: {}", allNews.size());

            // Группируем новости по промптам
            Map<Long, List<News>> newsByPrompt = allNews.stream()
                    .collect(Collectors.groupingBy(news -> news.getPrompt().getId()));

            int analyzedCount = 0;

            // Для каждого промпта анализируем новости
            for (Map.Entry<Long, List<News>> entry : newsByPrompt.entrySet()) {
                Long promptId = entry.getKey();
                List<News> promptNews = entry.getValue();

                // Находим пользователя владельца промпта
                var promptOpt = promptRepository.findById(promptId);
                if (promptOpt.isEmpty()) {
                    logger.warn("Промпт не найден: id={}", promptId);
                    continue;
                }

                Prompt prompt = promptOpt.get();
                Long userId = prompt.getUser().getId();

                // Конвертируем в DTO
                List<NewsDTO> newsDTOs = promptNews.stream()
                        .map(NewsDTO::new)
                        .toList();

                try {
                    // Анализируем новости
                    AnalysisResultDTO result = analysisService.analyzeNews(promptId, userId, newsDTOs);
                    logger.info("Анализ завершён для промпта {}: результат id={}", promptId, result.id());

                    // Помечаем новости как проанализированные
                    for (News news : promptNews) {
                        news.setIsAnalyzed(true);
                        newsRepository.save(news);
                    }

                    analyzedCount += promptNews.size();

                } catch (Exception e) {
                    logger.error("Ошибка при анализе промпта {}: {}", promptId, e.getMessage(), e);
                    // Продолжаем анализ других промптов
                }
            }

            logger.info("=== Завершён плановый анализ: проанализировано новостей={} ===", analyzedCount);

        } catch (Exception e) {
            logger.error("Ошибка при плановом анализе новостей: {}", e.getMessage(), e);
            // Не пробрасываем исключение чтобы не ломать планировщик
        }
    }
}
