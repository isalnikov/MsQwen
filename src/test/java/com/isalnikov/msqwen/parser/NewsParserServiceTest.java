package com.isalnikov.msqwen.parser;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.isalnikov.msqwen.entity.Channel;
import com.isalnikov.msqwen.entity.News;
import com.isalnikov.msqwen.entity.Prompt;
import com.isalnikov.msqwen.entity.User;
import com.isalnikov.msqwen.repository.NewsRepository;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit тесты для {@link NewsParserService}.
 *
 * @author isalnikov
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class NewsParserServiceTest {

    @Mock
    private TelegramParser telegramParser;

    @Mock
    private NewsRepository newsRepository;

    @InjectMocks
    private NewsParserService newsParserService;

    private Channel testChannel;
    private User testUser;
    private Prompt testPrompt;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setTelegramId(12345L);
        testUser.setUsername("testuser");

        testPrompt = new Prompt();
        testPrompt.setId(1L);
        testPrompt.setUser(testUser);
        testPrompt.setPromptText("Test prompt");

        testChannel = new Channel();
        testChannel.setId(1L);
        testChannel.setPrompt(testPrompt);
        testChannel.setUser(testUser);
        testChannel.setTelegramHandle("@testchannel");
        testChannel.setIsActive(true);
    }

    @Test
    void testParseAndSaveChannelNews_Success() throws IOException {
        // Подготовка
        List<ParsedNews> parsedNewsList = List.of(
                new ParsedNews(1L, "News 1", "Content 1", LocalDateTime.now(), 100, 10, 20, 15,
                        new BigDecimal("0.25"), "https://t.me/test/1"),
                new ParsedNews(2L, "News 2", "Content 2", LocalDateTime.now(), 200, 20, 30, 25,
                        new BigDecimal("0.50"), "https://t.me/test/2")
        );

        when(telegramParser.parseChannel("testchannel")).thenReturn(parsedNewsList);
        when(newsRepository.existsByChannelIdAndMessageId(1L, 1L)).thenReturn(false);
        when(newsRepository.existsByChannelIdAndMessageId(1L, 2L)).thenReturn(false);
        when(newsRepository.save(any(News.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Выполнение
        int savedCount = newsParserService.parseAndSaveChannelNews(testChannel);

        // Проверка
        assertEquals(2, savedCount);
        verify(newsRepository, times(2)).save(any(News.class));
        assertNotNull(testChannel.getLastParsedAt());
    }

    @Test
    void testParseAndSaveChannelNews_SkipDuplicates() throws IOException {
        // Подготовка
        List<ParsedNews> parsedNewsList = List.of(
                new ParsedNews(1L, "News 1", "Content 1", LocalDateTime.now(), 100, 10, 20, 15,
                        new BigDecimal("0.25"), "https://t.me/test/1")
        );

        when(telegramParser.parseChannel("testchannel")).thenReturn(parsedNewsList);
        when(newsRepository.existsByChannelIdAndMessageId(1L, 1L)).thenReturn(true); // Уже существует

        // Выполнение
        int savedCount = newsParserService.parseAndSaveChannelNews(testChannel);

        // Проверка
        assertEquals(0, savedCount);
        verify(newsRepository, never()).save(any(News.class));
    }

    @Test
    void testParseAndSaveChannelNews_ParserException() throws IOException {
        // Подготовка
        when(telegramParser.parseChannel("testchannel")).thenThrow(new RuntimeException("Ошибка парсинга"));

        // Выполнение
        int savedCount = newsParserService.parseAndSaveChannelNews(testChannel);

        // Проверка
        assertEquals(0, savedCount);
        verify(newsRepository, never()).save(any(News.class));
    }

    @Test
    void testParseAndSaveChannelNews_EmptyNews() throws IOException {
        // Подготовка
        when(telegramParser.parseChannel("testchannel")).thenReturn(List.of());

        // Выполнение
        int savedCount = newsParserService.parseAndSaveChannelNews(testChannel);

        // Проверка
        assertEquals(0, savedCount);
        verify(newsRepository, never()).save(any(News.class));
    }

    @Test
    void testParseAllChannels_OnlyActive() throws IOException {
        // Подготовка
        Channel activeChannel = new Channel();
        activeChannel.setId(1L);
        activeChannel.setPrompt(testPrompt);
        activeChannel.setUser(testUser);
        activeChannel.setTelegramHandle("@channel1");
        activeChannel.setIsActive(true);

        Channel inactiveChannel = new Channel();
        inactiveChannel.setId(2L);
        inactiveChannel.setPrompt(testPrompt);
        inactiveChannel.setUser(testUser);
        inactiveChannel.setTelegramHandle("@channel2");
        inactiveChannel.setIsActive(false);

        List<Channel> channels = List.of(activeChannel, inactiveChannel);

        List<ParsedNews> parsedNews = List.of(
                new ParsedNews(1L, "News", "Content", LocalDateTime.now(), 100, 10, 20, 15,
                        new BigDecimal("0.25"), "https://t.me/test/1")
        );

        when(telegramParser.parseChannel("channel1")).thenReturn(parsedNews);
        when(newsRepository.existsByChannelIdAndMessageId(anyLong(), anyLong())).thenReturn(false);
        when(newsRepository.save(any(News.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Выполнение
        int totalNews = newsParserService.parseAllChannels(1L, channels);

        // Проверка
        assertEquals(1, totalNews);
        verify(telegramParser, times(1)).parseChannel("channel1");
        verify(telegramParser, never()).parseChannel("channel2");
    }

    @Test
    void testConvertToEntity() {
        // Подготовка
        ParsedNews parsedNews = new ParsedNews(
                1L, "Title", "Content", LocalDateTime.now(),
                100, 10, 20, 15,
                new BigDecimal("0.25"), "https://t.me/test/1"
        );

        // Выполнение
        News news = newsParserService.convertToEntity(parsedNews, testChannel);

        // Проверка
        assertEquals(testChannel, news.getChannel());
        assertEquals(testPrompt, news.getPrompt());
        assertEquals(testUser, news.getUser());
        assertEquals(1L, news.getMessageId());
        assertEquals("Title", news.getTitle());
        assertEquals("Content", news.getContent());
        assertEquals(100, news.getViewsCount());
        assertEquals(10, news.getForwardsCount());
        assertEquals(20, news.getReactionsCount());
        assertEquals(15, news.getLikesCount());
        assertEquals(new BigDecimal("0.25"), news.getEngagementScore());
        assertEquals("https://t.me/test/1", news.getNewsUrl());
        assertFalse(news.getIsAnalyzed());
        assertNotNull(news.getCreatedAt());
        assertNotNull(news.getUpdatedAt());
    }
}
