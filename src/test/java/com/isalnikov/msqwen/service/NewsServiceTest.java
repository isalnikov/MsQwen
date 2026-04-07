package com.isalnikov.msqwen.service;

import com.isalnikov.msqwen.dto.NewsDTO;
import com.isalnikov.msqwen.entity.News;
import com.isalnikov.msqwen.entity.Prompt;
import com.isalnikov.msqwen.entity.User;
import com.isalnikov.msqwen.exception.AccessDeniedException;
import com.isalnikov.msqwen.repository.NewsRepository;
import com.isalnikov.msqwen.repository.PromptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit тесты для {@link NewsService}.
 *
 * @author isalnikov
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class NewsServiceTest {

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private PromptRepository promptRepository;

    @InjectMocks
    private NewsService newsService;

    private User testUser;
    private Prompt testPrompt;
    private News testNews;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);

        testPrompt = new Prompt();
        testPrompt.setId(1L);
        testPrompt.setUser(testUser);

        testNews = new News();
        testNews.setId(1L);
        testNews.setPrompt(testPrompt);
        testNews.setUser(testUser);
        testNews.setTitle("Test News");
        testNews.setViewsCount(100);
        testNews.setForwardsCount(10);
        testNews.setReactionsCount(20);
        testNews.setLikesCount(15);
        testNews.setEngagementScore(new BigDecimal("0.25"));
        testNews.setIsAnalyzed(false);
    }

    @Test
    void getNewsByPrompt_shouldReturnNews() {
        when(promptRepository.existsByIdAndUserId(1L, 1L)).thenReturn(true);
        when(newsRepository.findAllByPromptIdAndUserId(eq(1L), eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(testNews)));

        Page<NewsDTO> result = newsService.getNewsByPrompt(1L, 1L, Pageable.ofSize(10));

        assertEquals(1, result.getTotalElements());
        assertEquals("Test News", result.getContent().get(0).title());
    }

    @Test
    void getNewsByPrompt_shouldThrowWhenAccessDenied() {
        when(promptRepository.existsByIdAndUserId(1L, 1L)).thenReturn(false);
        when(promptRepository.existsById(1L)).thenReturn(true);

        assertThrows(AccessDeniedException.class,
                () -> newsService.getNewsByPrompt(1L, 1L, Pageable.ofSize(10)));
    }

    @Test
    void getNewsByIdAndUser_shouldReturnNews() {
        when(newsRepository.findById(1L)).thenReturn(Optional.of(testNews));

        NewsDTO result = newsService.getNewsByIdAndUser(1L, 1L);

        assertNotNull(result);
        assertEquals("Test News", result.title());
    }

    @Test
    void getNewsByIdAndUser_shouldThrowWhenNotFound() {
        when(newsRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> newsService.getNewsByIdAndUser(1L, 1L));
    }

    @Test
    void getNewsByIdAndUser_shouldThrowWhenAccessDenied() {
        User otherUser = new User();
        otherUser.setId(2L);
        News otherNews = new News();
        otherNews.setId(1L);
        otherNews.setUser(otherUser);

        when(newsRepository.findById(1L)).thenReturn(Optional.of(otherNews));

        assertThrows(AccessDeniedException.class, () -> newsService.getNewsByIdAndUser(1L, 1L));
    }

    @Test
    void markNewsAsAnalyzed_shouldUpdateStatus() {
        when(newsRepository.findById(1L)).thenReturn(Optional.of(testNews));
        when(newsRepository.save(any(News.class))).thenAnswer(invocation -> invocation.getArgument(0));

        newsService.markNewsAsAnalyzed(List.of(1L), 1L);

        assertTrue(testNews.getIsAnalyzed());
        verify(newsRepository).save(any(News.class));
    }

    @Test
    void deleteOldNews_shouldDeleteOldNews() {
        LocalDateTime oldDate = LocalDateTime.now().minusDays(31);
        News oldNews = new News();
        oldNews.setId(2L);
        oldNews.setPublicationDate(oldDate);

        when(newsRepository.findAllByPublicationDateBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(oldNews));

        long deleted = newsService.deleteOldNews(30);

        assertEquals(1, deleted);
        verify(newsRepository).deleteAll(anyList());
    }

    @Test
    void deleteOldNews_shouldReturnZeroWhenNoOldNews() {
        when(newsRepository.findAllByPublicationDateBefore(any(LocalDateTime.class)))
                .thenReturn(List.of());

        long deleted = newsService.deleteOldNews(30);

        assertEquals(0, deleted);
        verify(newsRepository, never()).deleteAll(anyList());
    }

    @Test
    void deleteAllNewsByPrompt_shouldDeleteNews() {
        when(promptRepository.existsByIdAndUserId(1L, 1L)).thenReturn(true);

        newsService.deleteAllNewsByPrompt(1L, 1L);

        verify(newsRepository).deleteAllByPromptIdAndUserId(1L, 1L);
    }

    @Test
    void deleteAllNewsByPrompt_shouldThrowWhenAccessDenied() {
        when(promptRepository.existsByIdAndUserId(1L, 1L)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> newsService.deleteAllNewsByPrompt(1L, 1L));
    }

    @Test
    void getNewsStatistics_shouldReturnStats() {
        when(newsRepository.countByUserId(1L)).thenReturn(100L);
        when(newsRepository.countByUserIdAndIsAnalyzed(1L, true)).thenReturn(60L);
        when(newsRepository.countByUserIdAndIsAnalyzed(1L, false)).thenReturn(40L);

        long[] stats = newsService.getNewsStatistics(1L);

        assertArrayEquals(new long[]{100L, 60L, 40L}, stats);
    }

    @Test
    void getUnanalyzedNews_shouldReturnNews() {
        when(promptRepository.existsByIdAndUserId(1L, 1L)).thenReturn(true);
        when(newsRepository.findAllByPromptIdAndUserIdAndIsAnalyzed(1L, 1L, false))
                .thenReturn(List.of(testNews));

        List<NewsDTO> result = newsService.getUnanalyzedNews(1L, 1L);

        assertEquals(1, result.size());
        assertFalse(result.get(0).isAnalyzed());
    }

    @Test
    void calculateEngagementScore_shouldCalculateCorrectly() {
        // (100 * 0.1 + 10 * 0.3 + 20 * 0.6) / 100 = 0.25
        BigDecimal score = newsService.calculateEngagementScore(100, 10, 20);

        assertEquals(new BigDecimal("0.25"), score);
    }

    @Test
    void calculateEngagementScore_shouldNormalizeToOne() {
        // Большие числа должны нормализоваться до 1.0
        BigDecimal score = newsService.calculateEngagementScore(10000, 1000, 2000);

        assertEquals(new BigDecimal("1.00"), score);
    }

    @Test
    void getNewsCount_shouldReturnCount() {
        when(newsRepository.countByUserId(1L)).thenReturn(50L);

        long count = newsService.getNewsCount(1L);

        assertEquals(50L, count);
    }

    @Test
    void getAnalyzedNewsCount_shouldReturnCount() {
        when(newsRepository.countByUserIdAndIsAnalyzed(1L, true)).thenReturn(30L);

        long count = newsService.getAnalyzedNewsCount(1L);

        assertEquals(30L, count);
    }

    @Test
    void getNewsByPromptWithoutPagination_shouldReturnNews() {
        when(promptRepository.existsByIdAndUserId(1L, 1L)).thenReturn(true);
        when(newsRepository.findAllByPromptIdAndUserIdAndIsAnalyzedOrderByPublicationDateDesc(1L, 1L, false))
                .thenReturn(List.of(testNews));

        List<NewsDTO> result = newsService.getNewsByPrompt(1L, 1L);

        assertEquals(1, result.size());
    }
}
