package com.isalnikov.msqwen.service;

import com.isalnikov.msqwen.dto.AnalysisResultDTO;
import com.isalnikov.msqwen.dto.NewsDTO;
import com.isalnikov.msqwen.entity.AnalysisResult;
import com.isalnikov.msqwen.entity.Prompt;
import com.isalnikov.msqwen.exception.AccessDeniedException;
import com.isalnikov.msqwen.exception.ResourceNotFoundException;
import com.isalnikov.msqwen.repository.AnalysisResultRepository;
import com.isalnikov.msqwen.repository.PromptRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис для анализа новостей через Qwen CLI.
 * 
 * <p>Обеспечивает анализ новостей кеширование результатов и историю анализов.
 * Передаёт все метаданные новостей в Qwen для анализа.
 * Все операции изолированы по user_id.</p>
 * 
 * @author isalnikov
 * @version 1.0
 */
@Service
@Transactional(readOnly = true)
public class AnalysisService {

    /**
     * Логгер для записи событий сервиса.
     */
    private static final Logger logger = LoggerFactory.getLogger(AnalysisService.class);

    /**
     * Репозиторий для доступа к результатам анализа.
     */
    private final AnalysisResultRepository analysisResultRepository;

    /**
     * Репозиторий для доступа к данным промптов.
     */
    private final PromptRepository promptRepository;

    /**
     * Сервис для вызова Qwen CLI.
     */
    private final QwenService qwenService;

    /**
     * Время жизни кеша в секундах (из конфигурации).
     */
    @Value("${cache.analysis.ttl:3600}")
    private int cacheTtlSeconds;

    /**
     * Конструктор с внедрением зависимостей через конструктор.
     *
     * @param analysisResultRepository репозиторий результатов анализа
     * @param promptRepository репозиторий промптов
     * @param qwenService сервис для вызова Qwen CLI
     */
    public AnalysisService(AnalysisResultRepository analysisResultRepository,
                           PromptRepository promptRepository,
                           QwenService qwenService) {
        this.analysisResultRepository = analysisResultRepository;
        this.promptRepository = promptRepository;
        this.qwenService = qwenService;
    }

    /**
     * Анализирует новости через Qwen CLI.
     * Если результат уже кеширован - возвращает его.
     *
     * @param promptId идентификатор промпта
     * @param userId идентификатор пользователя
     * @param news список новостей для анализа
     * @return DTO результата анализа
     * @throws AccessDeniedException если промпт не принадлежит пользователю
     */
    @Transactional
    public AnalysisResultDTO analyzeNews(Long promptId, Long userId, List<NewsDTO> news) {
        logger.info("Анализ новостей: promptId={}, userId={}, newsCount={}", promptId, userId, news.size());
        
        // Проверяем права доступа
        Prompt prompt = promptRepository.findByIdAndUserId(promptId, userId)
                .orElseThrow(() -> {
                    if (promptRepository.existsById(promptId)) {
                        return new AccessDeniedException("Доступ к промпту запрещён");
                    }
                    return new ResourceNotFoundException("Промпт не найден");
                });
        
        // Генерируем cache key
        String cacheKey = generateCacheKey(news);
        
        // Проверяем кеш
        var cachedResult = analysisResultRepository.findByCacheKey(cacheKey);
        if (cachedResult.isPresent() 
                && cachedResult.get().getCacheExpiresAt() != null
                && cachedResult.get().getCacheExpiresAt().isAfter(LocalDateTime.now())) {
            logger.info("Найден кешированный результат анализа");
            return new AnalysisResultDTO(cachedResult.get());
        }
        
        // Формируем промпт с метаданными
        String promptWithContext = buildPromptWithContext(prompt.getPromptText(), news);
        
        // Отправляем в Qwen CLI
        logger.info("Отправка новостей в Qwen CLI");
        String analysisText = qwenService.sendToQwen(promptWithContext);
        
        // Сохраняем результат
        AnalysisResult result = new AnalysisResult();
        result.setPrompt(prompt);
        result.setUser(prompt.getUser());
        result.setAnalysisText(analysisText);
        result.setCacheKey(cacheKey);
        result.setCacheExpiresAt(LocalDateTime.now().plusSeconds(cacheTtlSeconds));
        result.setCreatedAt(LocalDateTime.now());
        
        // Сохраняем IDs новостей
        String newsIds = news.stream()
                .map(n -> n.id().toString())
                .collect(Collectors.joining(","));
        result.setNewsIds(newsIds);
        
        AnalysisResult savedResult = analysisResultRepository.save(result);
        logger.info("Результат анализа сохранён: id={}", savedResult.getId());
        
        return new AnalysisResultDTO(savedResult);
    }

    /**
     * Возвращает кешированный результат анализа.
     *
     * @param cacheKey ключ кеша
     * @return DTO результата или null если не найден
     */
    public AnalysisResultDTO getCachedAnalysis(String cacheKey) {
        logger.debug("Поиск кешированного анализа: cacheKey={}", cacheKey);
        
        return analysisResultRepository.findByCacheKey(cacheKey)
                .filter(result -> result.getCacheExpiresAt() == null 
                        || result.getCacheExpiresAt().isAfter(LocalDateTime.now()))
                .map(AnalysisResultDTO::new)
                .orElse(null);
    }

    /**
     * Возвращает историю анализов промпта.
     *
     * @param promptId идентификатор промпта
     * @param userId идентификатор пользователя
     * @return список DTO результатов
     */
    public List<AnalysisResultDTO> getAnalysisHistory(Long promptId, Long userId) {
        logger.debug("Получение истории анализов: promptId={}, userId={}", promptId, userId);
        
        if (!promptRepository.existsByIdAndUserId(promptId, userId)) {
            throw new AccessDeniedException("Доступ к промпту запрещён");
        }
        
        return analysisResultRepository.findAllByPromptIdAndUserIdOrderByCreatedAtDesc(promptId, userId)
                .stream()
                .map(AnalysisResultDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Возвращает историю анализов с пагинацией.
     *
     * @param userId идентификатор пользователя
     * @param pageable пагинация
     * @return страница результатов
     */
    public Page<AnalysisResultDTO> getAnalysisHistory(Long userId, Pageable pageable) {
        return analysisResultRepository.findAllByUserId(userId, pageable)
                .map(AnalysisResultDTO::new);
    }

    /**
     * Формирует промпт с метаданными новостей.
     *
     * @param promptTemplate шаблон промпта
     * @param news список новостей
     * @return промпт с контекстом
     */
    public String buildPromptWithContext(String promptTemplate, List<NewsDTO> news) {
        logger.debug("Формирование промпта с контекстом: newsCount={}", news.size());
        
        var contextBuilder = new StringBuilder();
        contextBuilder.append(promptTemplate).append("\n\n");
        contextBuilder.append("=== НОВОСТИ ДЛЯ АНАЛИЗА ===\n\n");
        
        int index = 1;
        for (NewsDTO n : news) {
            contextBuilder.append("Новость #").append(index).append("\n");
            contextBuilder.append("Заголовок: ").append(n.title() != null ? n.title() : "Без заголовка").append("\n");
            contextBuilder.append("Содержание: ").append(n.content() != null ? n.content() : "Нет содержания").append("\n");
            contextBuilder.append("Дата публикации: ").append(n.publicationDate()).append("\n");
            contextBuilder.append("Просмотры: ").append(n.viewsCount()).append("\n");
            contextBuilder.append("Пересылки: ").append(n.forwardsCount()).append("\n");
            contextBuilder.append("Реакции: ").append(n.reactionsCount()).append("\n");
            contextBuilder.append("Лайки: ").append(n.likesCount()).append("\n");
            contextBuilder.append("Engagement Score: ").append(n.engagementScore()).append("\n");
            contextBuilder.append("Ссылка: ").append(n.newsUrl() != null ? n.newsUrl() : "Нет ссылки").append("\n");
            contextBuilder.append("---\n\n");
            index++;
        }
        
        return contextBuilder.toString();
    }

    /**
     * Генерирует cache key на основе хеша новостей.
     *
     * @param news список новостей
     * @return SHA-256 хеш
     */
    public String generateCacheKey(List<NewsDTO> news) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String newsString = news.stream()
                    .map(n -> n.id() + ":" + n.title() + ":" + n.publicationDate())
                    .collect(Collectors.joining("|"));
            byte[] hash = digest.digest(newsString.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // Fallback на hashCode если SHA-256 недоступен
            return String.valueOf(news.hashCode());
        }
    }

    /**
     * Удаляет результаты анализа промпта.
     *
     * @param promptId идентификатор промпта
     * @param userId идентификатор пользователя
     * @throws AccessDeniedException если промпт не принадлежит пользователю
     */
    @Transactional
    public void deleteAnalysisByPrompt(Long promptId, Long userId) {
        logger.info("Удаление результатов анализа: promptId={}, userId={}", promptId, userId);
        
        if (!promptRepository.existsByIdAndUserId(promptId, userId)) {
            throw new AccessDeniedException("Доступ к промпту запрещён");
        }
        
        analysisResultRepository.deleteAllByPromptIdAndUserId(promptId, userId);
        logger.info("Результаты анализа удалены: promptId={}", promptId);
    }
}
