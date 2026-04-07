package com.isalnikov.msqwen.parser;

import com.isalnikov.msqwen.entity.Channel;
import com.isalnikov.msqwen.entity.News;
import com.isalnikov.msqwen.entity.Prompt;
import com.isalnikov.msqwen.entity.User;
import com.isalnikov.msqwen.repository.NewsRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис для парсинга и сохранения новостей из Telegram каналов.
 *
 * <p>Объединяет работу {@link TelegramParser} с базой данных:
 * парсит каналы, конвертирует {@link ParsedNews} в {@link News} entity,
 * проверяет на дубликаты и сохраняет в БД.</p>
 *
 * @author isalnikov
 * @version 1.0
 */
@Service
public class NewsParserService {

    /**
     * Логгер для записи событий сервиса.
     */
    private static final Logger logger = LoggerFactory.getLogger(NewsParserService.class);

    /**
     * Парсер для извлечения данных из Telegram каналов.
     */
    private final TelegramParser telegramParser;

    /**
     * Репозиторий для доступа к новостям.
     */
    private final NewsRepository newsRepository;

    /**
     * Конструктор с внедрением зависимостей через конструктор.
     *
     * @param telegramParser парсер Telegram каналов
     * @param newsRepository репозиторий новостей
     */
    public NewsParserService(TelegramParser telegramParser, NewsRepository newsRepository) {
        this.telegramParser = telegramParser;
        this.newsRepository = newsRepository;
    }

    /**
     * Парсит канал и сохраняет новые новости в базу данных.
     *
     * @param channel канал для парсинга
     * @return количество добавленных новостей
     */
    @Transactional
    public int parseAndSaveChannelNews(Channel channel) {
        logger.info("Начало парсинга канала: {} (id={})", channel.getTelegramHandle(), channel.getId());

        // Извлекаем handle канала из URL или directly из поля
        String handle = extractHandle(channel.getTelegramHandle());

        List<ParsedNews> parsedNewsList;
        try {
            parsedNewsList = telegramParser.parseChannel(handle);
        } catch (Exception e) {
            logger.error("Ошибка при парсинге канала {}: {}", channel.getTelegramHandle(), e.getMessage(), e);
            return 0;
        }

        if (parsedNewsList.isEmpty()) {
            logger.info("Нет новостей для сохранения из канала: {}", channel.getTelegramHandle());
            return 0;
        }

        int savedCount = 0;
        for (ParsedNews parsedNews : parsedNewsList) {
            // Проверяем на дубликаты
            boolean exists = newsRepository.existsByChannelIdAndMessageId(
                    channel.getId(), parsedNews.messageId());

            if (exists) {
                logger.debug("Новость уже существует, пропускаем: messageId={}", parsedNews.messageId());
                continue;
            }

            // Конвертируем и сохраняем
            News news = convertToEntity(parsedNews, channel);
            newsRepository.save(news);
            savedCount++;
        }

        // Обновляем дату последнего парсинга
        channel.setLastParsedAt(LocalDateTime.now());

        logger.info("Завершён парсинг канала: {}, сохранено новостей: {}",
                channel.getTelegramHandle(), savedCount);

        return savedCount;
    }

    /**
     * Парсит все активные каналы пользователя.
     *
     * @param userId идентификатор пользователя
     * @param channels список каналов для парсинга
     * @return общее количество добавленных новостей
     */
    @Transactional
    public int parseAllChannels(Long userId, List<Channel> channels) {
        logger.info("Начало парсинга всех каналов пользователя: {}, каналов: {}", userId, channels.size());

        int totalNews = 0;
        for (Channel channel : channels) {
            if (Boolean.TRUE.equals(channel.getIsActive())) {
                int newsCount = parseAndSaveChannelNews(channel);
                totalNews += newsCount;
            } else {
                logger.debug("Пропуск неактивного канала: {}", channel.getTelegramHandle());
            }
        }

        logger.info("Завершён парсинг всех каналов, всего новостей добавлено: {}", totalNews);
        return totalNews;
    }

    /**
     * Конвертирует ParsedNews в News entity.
     *
     * @param parsedNews распарсенная новость
     * @param channel канал владелец
     * @return News entity готовый для сохранения
     */
    News convertToEntity(ParsedNews parsedNews, Channel channel) {
        News news = new News();
        news.setChannel(channel);
        news.setPrompt(channel.getPrompt());
        news.setUser(channel.getUser());
        news.setMessageId(parsedNews.messageId());
        news.setTitle(parsedNews.title());
        news.setContent(parsedNews.content());
        news.setPublicationDate(parsedNews.publicationDate());
        news.setViewsCount(parsedNews.viewsCount());
        news.setForwardsCount(parsedNews.forwardsCount());
        news.setReactionsCount(parsedNews.reactionsCount());
        news.setLikesCount(parsedNews.likesCount());
        news.setEngagementScore(parsedNews.engagementScore());
        news.setNewsUrl(parsedNews.newsUrl());
        news.setIsAnalyzed(false);
        news.setCreatedAt(LocalDateTime.now());
        news.setUpdatedAt(LocalDateTime.now());
        return news;
    }

    /**
     * Извлекает handle канала из строки (удаляет @ если есть).
     *
     * @param handle handle канала
     * @return очищенный handle
     */
    private String extractHandle(String handle) {
        if (handle == null) {
            return "";
        }
        return handle.replace("@", "").trim();
    }
}
