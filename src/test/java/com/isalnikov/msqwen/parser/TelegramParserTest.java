package com.isalnikov.msqwen.parser;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit тесты для {@link TelegramParser}.
 *
 * @author isalnikov
 * @version 1.0
 */
class TelegramParserTest {

    private TelegramParser telegramParser;

    @BeforeEach
    void setUp() {
        telegramParser = new TelegramParser();
    }

    @Test
    void testCalculateEngagementScore_ZeroValues() {
        BigDecimal score = telegramParser.calculateEngagementScore(0, 0, 0);
        assertEquals(BigDecimal.ZERO.setScale(2), score);
    }

    @Test
    void testCalculateEngagementScore_NormalValues() {
        // (1000 * 0.1 + 100 * 0.3 + 200 * 0.6) / 100 = (100 + 30 + 120) / 100 = 2.5
        // Нормализовано до 1.0
        BigDecimal score = telegramParser.calculateEngagementScore(1000, 100, 200);
        assertEquals(new BigDecimal("1.00"), score);
    }

    @Test
    void testCalculateEngagementScore_LowValues() {
        // (10 * 0.1 + 2 * 0.3 + 5 * 0.6) / 100 = (1 + 0.6 + 3) / 100 = 0.046
        BigDecimal score = telegramParser.calculateEngagementScore(10, 2, 5);
        assertEquals(new BigDecimal("0.05"), score);
    }

    @Test
    void testCalculateEngagementScore_MediumValues() {
        // (100 * 0.1 + 10 * 0.3 + 20 * 0.6) / 100 = (10 + 3 + 12) / 100 = 0.25
        BigDecimal score = telegramParser.calculateEngagementScore(100, 10, 20);
        assertEquals(new BigDecimal("0.25"), score);
    }

    @Test
    void testCalculateEngagementScore_NullValues() {
        BigDecimal score = telegramParser.calculateEngagementScore(null, null, null);
        assertEquals(BigDecimal.ZERO.setScale(2), score);
    }

    @Test
    void testParseCountWithSuffix_PlainNumber() {
        assertEquals(1234, telegramParser.parseCountWithSuffix("1234"));
    }

    @Test
    void testParseCountWithSuffix_KSuffix() {
        assertEquals(5000, telegramParser.parseCountWithSuffix("5K"));
        assertEquals(5000, telegramParser.parseCountWithSuffix("5k"));
        assertEquals(5200, telegramParser.parseCountWithSuffix("5.2K"));
    }

    @Test
    void testParseCountWithSuffix_MSuffix() {
        assertEquals(2000000, telegramParser.parseCountWithSuffix("2M"));
        assertEquals(2000000, telegramParser.parseCountWithSuffix("2m"));
        assertEquals(2500000, telegramParser.parseCountWithSuffix("2.5M"));
    }

    @Test
    void testParseCountWithSuffix_EmptyString() {
        assertEquals(0, telegramParser.parseCountWithSuffix(""));
        assertEquals(0, telegramParser.parseCountWithSuffix("   "));
        assertEquals(0, telegramParser.parseCountWithSuffix(null));
    }

    @Test
    void testParseCountWithSuffix_InvalidFormat() {
        assertEquals(0, telegramParser.parseCountWithSuffix("abc"));
    }

    @Test
    void testExtractTitle_ShortContent() {
        String content = "Это короткая новость";
        String title = telegramParser.extractTitle(content);
        assertEquals("Это короткая новость", title);
    }

    @Test
    void testExtractTitle_LongContent() {
        String content = "Первая строка новости\nВторая строка\nТретья строка";
        String title = telegramParser.extractTitle(content);
        assertEquals("Первая строка новости", title);
    }

    @Test
    void testExtractTitle_VeryLongFirstLine() {
        String content = "А".repeat(150) + "\nВторая строка";
        String title = telegramParser.extractTitle(content);
        assertEquals("А".repeat(100), title);
    }

    @Test
    void testExtractTitle_NullOrEmpty() {
        assertNull(telegramParser.extractTitle(null));
        assertNull(telegramParser.extractTitle(""));
        assertNull(telegramParser.extractTitle("   "));
    }

    @Test
    void testParseMessage_EmptyMessage() {
        String html = "<div class=\"tgme_widget_message_wrap\"></div>";
        Document document = Jsoup.parse(html);
        Element messageWrap = document.selectFirst("div.tgme_widget_message_wrap");

        ParsedNews news = telegramParser.parseMessage(messageWrap, "test");
        assertNull(news);
    }

    @Test
    void testParseMessage_WithBasicData() {
        String html = """
                <div class="tgme_widget_message_wrap">
                    <div class="tgme_widget_message">
                        <a class="tgme_widget_message_date" href="https://t.me/test/123">
                            <time datetime="2024-01-01 12:00">1 янв. 2024 г., 12:00</time>
                        </a>
                        <div class="tgme_widget_message_text">Это тестовая новость</div>
                        <span class="tgme_widget_message_views">1.2K просмотров</span>
                    </div>
                </div>
                """;

        Document document = Jsoup.parse(html);
        Element messageWrap = document.selectFirst("div.tgme_widget_message_wrap");

        ParsedNews news = telegramParser.parseMessage(messageWrap, "test");

        assertNotNull(news);
        assertNotNull(news.messageId());
        assertEquals("Это тестовая новость", news.content());
        assertEquals(1200, news.viewsCount());
    }

    @Test
    void testParseMessage_WithAllMetrics() {
        String html = """
                <div class="tgme_widget_message_wrap">
                    <div class="tgme_widget_message">
                        <a class="tgme_widget_message_date" href="https://t.me/test/456">
                            <time datetime="2024-01-15 10:30">15 янв. 2024 г., 10:30</time>
                        </a>
                        <div class="tgme_widget_message_text">Заголовок\nПолный текст новости с метриками</div>
                        <span class="tgme_widget_message_views">5K просмотров</span>
                        <span class="tgme_widget_message_forwards">100 пересылок</span>
                        <span class="tgme_widget_message_reaction_count">50</span>
                        <span class="reaction-count">25</span>
                    </div>
                </div>
                """;

        Document document = Jsoup.parse(html);
        Element messageWrap = document.selectFirst("div.tgme_widget_message_wrap");

        ParsedNews news = telegramParser.parseMessage(messageWrap, "test");

        assertNotNull(news);
        assertNotNull(news.messageId());
        assertEquals(5000, news.viewsCount());
        assertEquals(100, news.forwardsCount());
        assertEquals(50, news.reactionsCount());
        assertEquals(25, news.likesCount());
        assertNotNull(news.engagementScore());
        assertEquals("https://t.me/test/456", news.newsUrl());
    }

    @Test
    void testParseMessages_MultipleMessages() {
        String html = """
                <div class="tgme_widget_message_wrap">
                    <div class="tgme_widget_message">
                        <a class="tgme_widget_message_date" href="https://t.me/test/1">
                            <time datetime="2024-01-01 10:00">1</time>
                        </a>
                        <div class="tgme_widget_message_text">Новость 1</div>
                    </div>
                </div>
                <div class="tgme_widget_message_wrap">
                    <div class="tgme_widget_message">
                        <a class="tgme_widget_message_date" href="https://t.me/test/2">
                            <time datetime="2024-01-01 11:00">2</time>
                        </a>
                        <div class="tgme_widget_message_text">Новость 2</div>
                    </div>
                </div>
                """;

        Document document = Jsoup.parse(html);

        var newsList = telegramParser.parseMessages(document, "test");

        assertTrue(newsList.size() >= 0);
        assertEquals("Новость 2", newsList.get(0).content()); // Обратный порядок
        assertEquals("Новость 1", newsList.get(1).content());
    }

    @Test
    void testParseMessages_SkipEmptyContent() {
        String html = """
                <div class="tgme_widget_message_wrap">
                    <div class="tgme_widget_message">
                        <a class="tgme_widget_message_date" href="https://t.me/test/1">
                            <time datetime="2024-01-01 10:00">1</time>
                        </a>
                    </div>
                </div>
                """;

        Document document = Jsoup.parse(html);

        var newsList = telegramParser.parseMessages(document, "test");

        assertTrue(newsList.isEmpty());
    }

    @Test
    void testExtractMessageId_ValidUrl() {
        String html = """
                <div class="tgme_widget_message">
                    <a class="tgme_widget_message_date" href="https://t.me/channel/12345"></a>
                </div>
                """;
        Document document = Jsoup.parse(html);
        Element message = document.selectFirst("div.tgme_widget_message");

        Long messageId = telegramParser.extractMessageId(message);
        assertNotNull(messageId); // messageId парсится из HTML
    }

    @Test
    void testExtractMessageId_InvalidUrl() {
        String html = """
                <div class="tgme_widget_message">
                    <a class="tgme_widget_message_date" href="https://t.me/channel/abc"></a>
                </div>
                """;
        Document document = Jsoup.parse(html);
        Element message = document.selectFirst("div.tgme_widget_message");

        Long messageId = telegramParser.extractMessageId(message);
        assertNull(messageId);
    }

    @Test
    void testExtractMessageId_NoLink() {
        String html = """
                <div class="tgme_widget_message"></div>
                """;
        Document document = Jsoup.parse(html);
        Element message = document.selectFirst("div.tgme_widget_message");

        Long messageId = telegramParser.extractMessageId(message);
        assertNull(messageId);
    }

    @Test
    void testExtractPublicationDate_ValidDatetime() {
        String html = """
                <div class="tgme_widget_message">
                    <time class="tgme_widget_message_date" datetime="2024-01-15 14:30">15 янв. 2024 г., 14:30</time>
                </div>
                """;
        Document document = Jsoup.parse(html);
        Element message = document.selectFirst("div.tgme_widget_message");

        LocalDateTime date = telegramParser.extractPublicationDate(message);
        assertNotNull(date);
        assertEquals(2024, date.getYear());
        assertEquals(1, date.getMonthValue());
        assertEquals(15, date.getDayOfMonth());
        assertEquals(14, date.getHour());
        assertEquals(30, date.getMinute());
    }

    @Test
    void testExtractPublicationDate_FallbackToNow() {
        String html = """
                <div class="tgme_widget_message">
                    <time class="tgme_widget_message_date">непонятная дата</time>
                </div>
                """;
        Document document = Jsoup.parse(html);
        Element message = document.selectFirst("div.tgme_widget_message");

        LocalDateTime date = telegramParser.extractPublicationDate(message);
        assertNotNull(date);
        // Должна вернуть текущее время
    }

    @Test
    void testExtractViewsCount_Present() {
        String html = """
                <div class="tgme_widget_message">
                    <span class="tgme_widget_message_views">3.5K просмотров</span>
                </div>
                """;
        Document document = Jsoup.parse(html);
        Element message = document.selectFirst("div.tgme_widget_message");

        Integer views = telegramParser.extractViewsCount(message);
        assertEquals(3500, views);
    }

    @Test
    void testExtractViewsCount_Missing() {
        String html = """
                <div class="tgme_widget_message"></div>
                """;
        Document document = Jsoup.parse(html);
        Element message = document.selectFirst("div.tgme_widget_message");

        Integer views = telegramParser.extractViewsCount(message);
        assertEquals(0, views);
    }

    @Test
    void testExtractForwardsCount_Present() {
        String html = """
                <div class="tgme_widget_message">
                    <span class="tgme_widget_message_forwards">50 пересылок</span>
                </div>
                """;
        Document document = Jsoup.parse(html);
        Element message = document.selectFirst("div.tgme_widget_message");

        Integer forwards = telegramParser.extractForwardsCount(message);
        assertEquals(50, forwards);
    }

    @Test
    void testExtractContent_Present() {
        String html = """
                <div class="tgme_widget_message">
                    <div class="tgme_widget_message_text">Текст новости</div>
                </div>
                """;
        Document document = Jsoup.parse(html);
        Element message = document.selectFirst("div.tgme_widget_message");

        String content = telegramParser.extractContent(message);
        assertEquals("Текст новости", content);
    }

    @Test
    void testExtractContent_Missing() {
        String html = """
                <div class="tgme_widget_message"></div>
                """;
        Document document = Jsoup.parse(html);
        Element message = document.selectFirst("div.tgme_widget_message");

        String content = telegramParser.extractContent(message);
        assertNull(content);
    }
}
