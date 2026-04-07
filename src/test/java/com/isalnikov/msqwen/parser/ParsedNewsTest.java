package com.isalnikov.msqwen.parser;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/**
 * Unit тесты для {@link ParsedNews}.
 *
 * @author isalnikov
 * @version 1.0
 */
class ParsedNewsTest {

    @Test
    void testParsedNewsCreation() {
        ParsedNews news = new ParsedNews(
                1L,
                "Test Title",
                "Test Content",
                LocalDateTime.of(2024, 1, 1, 12, 0),
                100,
                10,
                20,
                15,
                new BigDecimal("0.25"),
                "https://t.me/test/1"
        );

        assertEquals(1L, news.messageId());
        assertEquals("Test Title", news.title());
        assertEquals("Test Content", news.content());
        assertEquals(LocalDateTime.of(2024, 1, 1, 12, 0), news.publicationDate());
        assertEquals(100, news.viewsCount());
        assertEquals(10, news.forwardsCount());
        assertEquals(20, news.reactionsCount());
        assertEquals(15, news.likesCount());
        assertEquals(new BigDecimal("0.25"), news.engagementScore());
        assertEquals("https://t.me/test/1", news.newsUrl());
    }

    @Test
    void testWithDefaults() {
        ParsedNews news = ParsedNews.withDefaults(
                1L,
                "Test Content",
                LocalDateTime.of(2024, 1, 1, 12, 0),
                "https://t.me/test/1"
        );

        assertEquals(1L, news.messageId());
        assertNull(news.title());
        assertEquals("Test Content", news.content());
        assertEquals(LocalDateTime.of(2024, 1, 1, 12, 0), news.publicationDate());
        assertEquals(0, news.viewsCount());
        assertEquals(0, news.forwardsCount());
        assertEquals(0, news.reactionsCount());
        assertEquals(0, news.likesCount());
        assertEquals(BigDecimal.ZERO, news.engagementScore());
        assertEquals("https://t.me/test/1", news.newsUrl());
    }
}
