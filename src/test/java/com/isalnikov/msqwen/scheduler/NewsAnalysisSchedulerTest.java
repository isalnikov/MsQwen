package com.isalnikov.msqwen.scheduler;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.isalnikov.msqwen.dto.AnalysisResultDTO;
import com.isalnikov.msqwen.dto.NewsDTO;
import com.isalnikov.msqwen.entity.News;
import com.isalnikov.msqwen.entity.Prompt;
import com.isalnikov.msqwen.entity.User;
import com.isalnikov.msqwen.repository.NewsRepository;
import com.isalnikov.msqwen.repository.PromptRepository;
import com.isalnikov.msqwen.service.AnalysisService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit тесты для {@link NewsAnalysisScheduler}.
 *
 * @author isalnikov
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class NewsAnalysisSchedulerTest {

    @Mock
    private AnalysisService analysisService;

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private PromptRepository promptRepository;

    @InjectMocks
    private NewsAnalysisScheduler newsAnalysisScheduler;

    private News testNews;
    private Prompt testPrompt;
    private User testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(newsAnalysisScheduler, "analyzeEnabled", true);
        ReflectionTestUtils.setField(newsAnalysisScheduler, "maxNewsToAnalyze", 100);

        testUser = new User();
        testUser.setId(1L);
        testUser.setTelegramId(12345L);

        testPrompt = new Prompt();
        testPrompt.setId(1L);
        testPrompt.setUser(testUser);

        testNews = new News();
        testNews.setId(1L);
        testNews.setPrompt(testPrompt);
        testNews.setUser(testUser);
        testNews.setIsAnalyzed(false);
        testNews.setEngagementScore(new BigDecimal("0.5"));
        testNews.setTitle("Test News");
    }

    @Test
    void testAnalyzeNewNews_SchedulerDisabled() {
        // Подготовка
        ReflectionTestUtils.setField(newsAnalysisScheduler, "analyzeEnabled", false);

        // Выполнение
        newsAnalysisScheduler.analyzeNewNews();

        // Проверка
        verify(newsRepository, never()).findAll();
    }

    @Test
    void testAnalyzeNewNews_NoNewsToAnalyze() {
        // Подготовка
        when(newsRepository.findAll()).thenReturn(List.of());

        // Выполнение
        newsAnalysisScheduler.analyzeNewNews();

        // Проверка
        verify(newsRepository).findAll();
        verify(analysisService, never()).analyzeNews(anyLong(), anyLong(), anyList());
    }

    @Test
    void testAnalyzeNewNews_AllNewsAlreadyAnalyzed() {
        // Подготовка
        News analyzedNews = new News();
        analyzedNews.setIsAnalyzed(true);
        when(newsRepository.findAll()).thenReturn(List.of(analyzedNews));

        // Выполнение
        newsAnalysisScheduler.analyzeNewNews();

        // Проверка
        verify(analysisService, never()).analyzeNews(anyLong(), anyLong(), anyList());
    }

    @Test
    void testAnalyzeNewNews_Success() {
        // Подготовка
        when(newsRepository.findAll()).thenReturn(List.of(testNews));
        when(promptRepository.findById(1L)).thenReturn(Optional.of(testPrompt));
        when(analysisService.analyzeNews(eq(1L), eq(1L), anyList()))
                .thenReturn(new AnalysisResultDTO(1L, 1L, 1L, "Result", null, null, LocalDateTime.now(), LocalDateTime.now()));

        // Выполнение
        newsAnalysisScheduler.analyzeNewNews();

        // Проверка
        verify(analysisService).analyzeNews(eq(1L), eq(1L), anyList());
        verify(newsRepository, atLeastOnce()).save(any(News.class));
    }

    @Test
    void testAnalyzeNewNews_PromptNotFound() {
        // Подготовка
        when(newsRepository.findAll()).thenReturn(List.of(testNews));
        when(promptRepository.findById(1L)).thenReturn(Optional.empty());

        // Выполнение
        newsAnalysisScheduler.analyzeNewNews();

        // Проверка
        verify(analysisService, never()).analyzeNews(anyLong(), anyLong(), anyList());
    }

    @Test
    void testAnalyzeNewNews_AnalysisExceptionHandled() {
        // Подготовка
        when(newsRepository.findAll()).thenReturn(List.of(testNews));
        when(promptRepository.findById(1L)).thenReturn(Optional.of(testPrompt));
        when(analysisService.analyzeNews(eq(1L), eq(1L), anyList()))
                .thenThrow(new RuntimeException("Ошибка Qwen CLI"));

        // Выполнение - не должно выбрасывать исключение
        newsAnalysisScheduler.analyzeNewNews();

        // Проверка - исключение обработано
        verify(analysisService).analyzeNews(eq(1L), eq(1L), anyList());
    }
}
