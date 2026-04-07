package com.isalnikov.msqwen.parser;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Парсер новостей из публичных Telegram каналов через t.me/s/.
 *
 * <p>Использует Jsoup для парсинга HTML страниц превью Telegram каналов.
 * Извлекает все метаданные новостей: просмотры, пересылки, реакции, лайки,
 * дату публикации и рассчитывает индекс вовлечённости (engagement_score).</p>
 *
 * <p>Формула engagement_score: (views * 0.1 + forwards * 0.3 + reactions * 0.6) / 100
 * Значение нормализуется в диапазоне 0.0 - 1.0.</p>
 *
 * <p>Пример URL канала: https://t.me/s/durov</p>
 *
 * @author isalnikov
 * @version 1.0
 */
@Component
public class TelegramParser {

    /**
     * Логгер для записи событий парсера.
     */
    private static final Logger logger = LoggerFactory.getLogger(TelegramParser.class);

    /**
     * Базовый URL для превью Telegram каналов.
     */
    private static final String BASE_URL = "https://t.me/s/";

    /**
     * Максимальное количество сообщений для парсинга за один раз.
     */
    private static final int MAX_MESSAGES = 50;

    /**
     * Паттерн для извлечения количества просмотров (может содержать "K", "M").
     */
    private static final Pattern VIEWS_PATTERN = Pattern.compile("([\\d.,]+[KkMm]?)\\s+просмотр");

    /**
     * Форматтеры для парсинга дат из Telegram.
     */
    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("d MMM yyyy г., HH:mm", new Locale("ru")),
            DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", new Locale("ru")),
            DateTimeFormatter.ofPattern("d MMM г., HH:mm", new Locale("ru")),
            DateTimeFormatter.ofPattern("d MMM, HH:mm", new Locale("ru"))
    );

    /**
     * Парсит все доступные новости из Telegram канала.
     *
     * @param channelHandle handle канала (например "durov" без @)
     * @return список распарсенных новостей
     * @throws IOException если ошибка при загрузке страницы
     */
    public List<ParsedNews> parseChannel(String channelHandle) throws IOException {
        String channelUrl = BASE_URL + channelHandle;
        logger.info("Начало парсинга канала: {}", channelUrl);

        Document document = Jsoup.connect(channelUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                        + "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .timeout(30000)
                .get();

        List<ParsedNews> newsList = parseMessages(document, channelHandle);

        logger.info("Завершён парсинг канала: {}, найдено новостей: {}", channelUrl, newsList.size());
        return newsList;
    }

    /**
     * Парсит сообщения из HTML документа Telegram канала.
     *
     * @param document HTML документ
     * @param channelHandle handle канала
     * @return список распарсенных новостей
     */
    List<ParsedNews> parseMessages(Document document, String channelHandle) {
        Elements messages = document.select("div.tgme_widget_message_wrap");

        logger.debug("Найдено сообщений для парсинга: {}", messages.size());

        List<ParsedNews> newsList = new ArrayList<>();
        int count = 0;

        // Обрабатываем сообщения в обратном порядке (новые сверху)
        for (int i = messages.size() - 1; i >= 0 && count < MAX_MESSAGES; i--) {
            Element messageWrap = messages.get(i);
            try {
                ParsedNews news = parseMessage(messageWrap, channelHandle);
                if (news != null) {
                    newsList.add(news);
                    count++;
                }
            } catch (Exception e) {
                logger.warn("Ошибка при парсинге сообщения, пропускаем: {}", e.getMessage());
            }
        }

        logger.debug("Успешно распарсено новостей: {}", newsList.size());
        return newsList;
    }

    /**
     * Парсит одно сообщение из HTML элемента.
     *
     * @param messageWrap элемент сообщения
     * @param channelHandle handle канала
     * @return распарсенная новость или null если не удалось
     */
    ParsedNews parseMessage(Element messageWrap, String channelHandle) {
        Element message = messageWrap.selectFirst("div.tgme_widget_message");
        if (message == null) {
            logger.debug("Не найден элемент сообщения");
            return null;
        }

        // Извлекаем ID сообщения
        Long messageId = extractMessageId(message);
        if (messageId == null) {
            logger.debug("Не удалось извлечь ID сообщения");
            return null;
        }

        // Извлекаем текст сообщения
        String content = extractContent(message);
        if (content == null || content.isBlank()) {
            logger.debug("Пустое содержание сообщения");
            return null;
        }

        // Извлекаем заголовок (первая строка если есть)
        String title = extractTitle(content);

        // Извлекаем дату публикации
        LocalDateTime publicationDate = extractPublicationDate(message);

        // Извлекаем метрики
        Integer viewsCount = extractViewsCount(message);
        Integer forwardsCount = extractForwardsCount(message);
        Integer reactionsCount = extractReactionsCount(message);
        Integer likesCount = extractLikesCount(message);

        // Рассчитываем engagement score
        BigDecimal engagementScore = calculateEngagementScore(viewsCount, forwardsCount, reactionsCount);

        // Формируем URL новости
        String newsUrl = String.format("https://t.me/%s/%d", channelHandle, messageId);

        return new ParsedNews(
                messageId,
                title,
                content,
                publicationDate,
                viewsCount,
                forwardsCount,
                reactionsCount,
                likesCount,
                engagementScore,
                newsUrl
        );
    }

    /**
     * Извлекает ID сообщения из HTML элемента.
     *
     * @param message элемент сообщения
     * @return ID сообщения или null
     */
    Long extractMessageId(Element message) {
        Element link = message.selectFirst("a.tgme_widget_message_date");
        if (link == null) {
            return null;
        }

        String href = link.attr("href");
        // URL формата: https://t.me/channel/12345
        Pattern pattern = Pattern.compile(".+/\\d+/?(\\d+)$");
        Matcher matcher = pattern.matcher(href);
        if (matcher.find()) {
            try {
                return Long.parseLong(matcher.group(1));
            } catch (NumberFormatException e) {
                logger.warn("Не удалось распознать ID сообщения из URL: {}", href);
            }
        }
        return null;
    }

    /**
     * Извлекает заголовок из содержания (первая строка или первые 100 символов).
     *
     * @param content полное содержание
     * @return заголовок или null
     */
    String extractTitle(String content) {
        if (content == null || content.isBlank()) {
            return null;
        }

        // Берём первую строку как заголовок
        String firstLine = content.lines().findFirst().orElse(null);
        if (firstLine != null && firstLine.length() <= 100) {
            return firstLine.trim();
        }

        // Или первые 100 символов
        return content.substring(0, Math.min(100, content.length())).trim();
    }

    /**
     * Извлекает текст сообщения из HTML элемента.
     *
     * @param message элемент сообщения
     * @return текст сообщения
     */
    String extractContent(Element message) {
        Element textElement = message.selectFirst("div.tgme_widget_message_text");
        if (textElement == null) {
            return null;
        }
        return textElement.text().trim();
    }

    /**
     * Извлекает дату публикации сообщения.
     *
     * @param message элемент сообщения
     * @return дата публикации или null
     */
    LocalDateTime extractPublicationDate(Element message) {
        Element timeElement = message.selectFirst("time.tgme_widget_message_date");
        if (timeElement == null) {
            return LocalDateTime.now();
        }

        // Пытаемся получить datetime атрибут
        String datetime = timeElement.attr("datetime");
        if (datetime != null && !datetime.isBlank()) {
            try {
                return LocalDateTime.parse(datetime.replace(" ", "T"));
            } catch (DateTimeParseException e) {
                logger.debug("Не удалось распознать дату из datetime атрибута: {}", datetime);
            }
        }

        // Пытаемся распознать из текста
        String dateText = timeElement.text();
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDateTime.parse(dateText, formatter);
            } catch (DateTimeParseException e) {
                // Пробуем следующий формат
            }
        }

        logger.debug("Не удалось распознать дату из текста: {}", dateText);
        return LocalDateTime.now();
    }

    /**
     * Извлекает количество просмотров из сообщения.
     *
     * @param message элемент сообщения
     * @return количество просмотров
     */
    Integer extractViewsCount(Element message) {
        Element viewsElement = message.selectFirst("span.tgme_widget_message_views");
        if (viewsElement == null) {
            return 0;
        }

        String viewsText = viewsElement.text();
        return parseCountWithSuffix(viewsText);
    }

    /**
     * Извлекает количество пересылок из сообщения.
     *
     * @param message элемент сообщения
     * @return количество пересылок
     */
    Integer extractForwardsCount(Element message) {
        Element forwardsElement = message.selectFirst("span.tgme_widget_message_forwards");
        if (forwardsElement == null) {
            return 0;
        }

        String forwardsText = forwardsElement.text();
        return parseCountWithSuffix(forwardsText);
    }

    /**
     * Извлекает количество реакций из сообщения.
     *
     * @param message элемент сообщения
     * @return количество реакций
     */
    Integer extractReactionsCount(Element message) {
        Elements reactionElements = message.select("span.tgme_widget_message_reaction_count");
        if (reactionElements.isEmpty()) {
            return 0;
        }

        int totalReactions = 0;
        for (Element element : reactionElements) {
            totalReactions += parseCountWithSuffix(element.text());
        }
        return totalReactions;
    }

    /**
     * Извлекает количество лайков из сообщения.
     *
     * @param message элемент сообщения
     * @return количество лайков
     */
    Integer extractLikesCount(Element message) {
        // В Telegram реакции могут быть в виде лайков
        Element likesElement = message.selectFirst("span.reaction-count");
        if (likesElement == null) {
            return 0;
        }

        return parseCountWithSuffix(likesElement.text());
    }

    /**
     * Парсит число с возможным суффиксом K (тысячи) или M (миллионы).
     *
     * @param text текст для парсинга
     * @return распарсенное число
     */
    int parseCountWithSuffix(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }

        // Извлекаем число и суффикс
        Pattern pattern = Pattern.compile("([\\d.,]+)\\s*([KkMm])?");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            try {
                double value = Double.parseDouble(matcher.group(1).replace(",", "."));
                String suffix = matcher.group(2);

                if (suffix != null) {
                    if (suffix.equalsIgnoreCase("K")) {
                        value *= 1000;
                    } else if (suffix.equalsIgnoreCase("M")) {
                        value *= 1000000;
                    }
                }

                return (int) Math.round(value);
            } catch (NumberFormatException e) {
                logger.warn("Не удалось распознать число из текста: {}", text);
            }
        }

        return 0;
    }

    /**
     * Рассчитывает индекс вовлечённости новости.
     *
     * <p>Формула: (views * 0.1 + forwards * 0.3 + reactions * 0.6) / 100
     * Результат нормализуется в диапазоне 0.0 - 1.0.</p>
     *
     * @param viewsCount количество просмотров
     * @param forwardsCount количество пересылок
     * @param reactionsCount количество реакций
     * @return индекс вовлечённости (0.0 - 1.0)
     */
    BigDecimal calculateEngagementScore(Integer viewsCount, Integer forwardsCount,
                                        Integer reactionsCount) {
        int views = viewsCount != null ? viewsCount : 0;
        int forwards = forwardsCount != null ? forwardsCount : 0;
        int reactions = reactionsCount != null ? reactionsCount : 0;

        double score = (views * 0.1 + forwards * 0.3 + reactions * 0.6) / 100.0;

        // Нормализация (0.0 - 1.0)
        double normalized = Math.min(1.0, score);

        return BigDecimal.valueOf(normalized).setScale(2, RoundingMode.HALF_UP);
    }
}
