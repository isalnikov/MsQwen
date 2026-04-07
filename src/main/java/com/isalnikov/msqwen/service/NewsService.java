package com.isalnikov.msqwen.service;

import com.isalnikov.msqwen.dto.NewsDTO;
import com.isalnikov.msqwen.entity.News;
import com.isalnikov.msqwen.exception.AccessDeniedException;
import com.isalnikov.msqwen.exception.ResourceNotFoundException;
import com.isalnikov.msqwen.repository.NewsRepository;
import com.isalnikov.msqwen.repository.PromptRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис для управления новостями.
 * 
 * <p>Обеспечивает получение сохранение очистку старых новостей и статистику.
 * Поддерживает расчёт engagement_score и фильтрацию по статусу анализа.
 * Все операции изолированы по user_id.</p>
 * 
 * @author isalnikov
 * @version 1.0
 */
@Service
@Transactional(readOnly = true)
public class NewsService {

    /**
     * Логгер для записи событий сервиса.
     */
    private static final Logger logger = LoggerFactory.getLogger(NewsService.class);

    /**
     * Репозиторий для доступа к данным новостей.
     */
    private final NewsRepository newsRepository;

    /**
     * Репозиторий для доступа к данным промптов.
     */
    private final PromptRepository promptRepository;

    /**
     * Конструктор с внедрением зависимостей через конструктор.
     *
     * @param newsRepository репозиторий новостей
     * @param promptRepository репозиторий промптов
     */
    public NewsService(NewsRepository newsRepository, PromptRepository promptRepository) {
        this.newsRepository = newsRepository;
        this.promptRepository = promptRepository;
    }

    /**
     * Возвращает новости промпта с пагинацией.
     *
     * @param promptId идентификатор промпта
     * @param userId идентификатор пользователя
     * @param pageable пагинация
     * @return страница DTO новостей
     * @throws AccessDeniedException если промпт не принадлежит пользователю
     */
    public Page<NewsDTO> getNewsByPrompt(Long promptId, Long userId, Pageable pageable) {
        logger.debug("Получение новостей промпта: promptId={}, userId={}", promptId, userId);
        
        if (!promptRepository.existsByIdAndUserId(promptId, userId)) {
            if (promptRepository.existsById(promptId)) {
                throw new AccessDeniedException("Доступ к промпту запрещён");
            }
            throw new ResourceNotFoundException("Промпт не найден");
        }
        
        return newsRepository.findAllByPromptIdAndUserId(promptId, userId, pageable)
                .map(NewsDTO::new);
    }

    /**
     * Возвращает новость по идентификатору с проверкой прав.
     *
     * @param newsId идентификатор новости
     * @param userId идентификатор пользователя
     * @return DTO новости
     * @throws ResourceNotFoundException если новость не найдена
     * @throws AccessDeniedException если новость не принадлежит пользователю
     */
    public NewsDTO getNewsByIdAndUser(Long newsId, Long userId) {
        logger.debug("Получение новости: newsId={}, userId={}", newsId, userId);
        
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> {
                    logger.warn("Новость не найдена: newsId={}", newsId);
                    return new ResourceNotFoundException("Новость не найдена");
                });
        
        if (!news.getUser().getId().equals(userId)) {
            logger.warn("Доступ запрещён: newsId={}, userId={}", newsId, userId);
            throw new AccessDeniedException("Доступ к новости запрещён");
        }
        
        return new NewsDTO(news);
    }

    /**
     * Помечает новости как проанализированные.
     *
     * @param newsIds список идентификаторов новостей
     * @param userId идентификатор пользователя
     */
    @Transactional
    public void markNewsAsAnalyzed(List<Long> newsIds, Long userId) {
        logger.info("Пометка новостей как проанализированных: count={}", newsIds.size());
        
        for (Long newsId : newsIds) {
            newsRepository.findById(newsId).ifPresent(news -> {
                if (news.getUser().getId().equals(userId)) {
                    news.setIsAnalyzed(true);
                    news.setUpdatedAt(LocalDateTime.now());
                    newsRepository.save(news);
                }
            });
        }
        
        logger.info("Новости помечены как проанализированные: count={}", newsIds.size());
    }

    /**
     * Удаляет новости старше указанной даты.
     * Используется для регулярной очистки базы данных.
     *
     * @param days количество дней для хранения
     * @return количество удалённых новостей
     */
    @Transactional
    public long deleteOldNews(int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        logger.info("Удаление новостей старше {} дней (cutoffDate={})", days, cutoffDate);
        
        List<News> oldNews = newsRepository.findAllByPublicationDateBefore(cutoffDate);
        int count = oldNews.size();
        
        if (count > 0) {
            newsRepository.deleteAll(oldNews);
            logger.info("Удалено {} старых новостей", count);
        }
        
        return count;
    }

    /**
     * Удаляет все новости промпта.
     *
     * @param promptId идентификатор промпта
     * @param userId идентификатор пользователя
     * @throws AccessDeniedException если промпт не принадлежит пользователю
     */
    @Transactional
    public void deleteAllNewsByPrompt(Long promptId, Long userId) {
        logger.info("Удаление всех новостей промпта: promptId={}, userId={}", promptId, userId);
        
        if (!promptRepository.existsByIdAndUserId(promptId, userId)) {
            throw new AccessDeniedException("Доступ к промпту запрещён");
        }
        
        newsRepository.deleteAllByPromptIdAndUserId(promptId, userId);
        logger.info("Все новости промпта удалены: promptId={}", promptId);
    }

    /**
     * Возвращает статистику новостей пользователя.
     *
     * @param userId идентификатор пользователя
     * @return массив [всего проанализировано неПроанализировано]
     */
    public long[] getNewsStatistics(Long userId) {
        logger.debug("Получение статистики новостей: userId={}", userId);
        
        long total = newsRepository.countByUserId(userId);
        long analyzed = newsRepository.countByUserIdAndIsAnalyzed(userId, true);
        long notAnalyzed = newsRepository.countByUserIdAndIsAnalyzed(userId, false);
        
        return new long[]{total, analyzed, notAnalyzed};
    }

    /**
     * Возвращает не проанализированные новости промпта.
     *
     * @param promptId идентификатор промпта
     * @param userId идентификатор пользователя
     * @return список DTO не проанализированных новостей
     */
    public List<NewsDTO> getUnanalyzedNews(Long promptId, Long userId) {
        logger.debug("Получение не проанализированных новостей: promptId={}, userId={}", promptId, userId);
        
        if (!promptRepository.existsByIdAndUserId(promptId, userId)) {
            throw new AccessDeniedException("Доступ к промпту запрещён");
        }
        
        return newsRepository.findAllByPromptIdAndUserIdAndIsAnalyzed(promptId, userId, false)
                .stream()
                .map(NewsDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Рассчитывает engagement_score для новости.
     * Формула: (views * 0.1 + forwards * 0.3 + reactions * 0.6) / 100
     *
     * @param viewsCount количество просмотров
     * @param forwardsCount количество пересылок
     * @param reactionsCount количество реакций
     * @return рассчитанный engagement_score
     */
    public BigDecimal calculateEngagementScore(int viewsCount, int forwardsCount, int reactionsCount) {
        BigDecimal score = BigDecimal.valueOf(viewsCount)
                .multiply(BigDecimal.valueOf(0.1))
                .add(BigDecimal.valueOf(forwardsCount).multiply(BigDecimal.valueOf(0.3)))
                .add(BigDecimal.valueOf(reactionsCount).multiply(BigDecimal.valueOf(0.6)))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        
        // Нормализация 0.0 - 1.0
        return score.min(BigDecimal.ONE).max(BigDecimal.ZERO);
    }
}
