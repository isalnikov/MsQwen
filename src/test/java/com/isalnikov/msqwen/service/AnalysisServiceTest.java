package com.isalnikov.msqwen.service;

import com.isalnikov.msqwen.dto.AnalysisResultDTO;
import com.isalnikov.msqwen.dto.NewsDTO;
import com.isalnikov.msqwen.entity.AnalysisResult;
import com.isalnikov.msqwen.entity.Prompt;
import com.isalnikov.msqwen.entity.User;
import com.isalnikov.msqwen.exception.AccessDeniedException;
import com.isalnikov.msqwen.repository.AnalysisResultRepository;
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
 * Unit тесты для {@link AnalysisService}.
 *
 * @author isalnikov
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)

class AnalysisServiceTest {

    @Mock
    private AnalysisResultRepository analysisResultRepository;

    @Mock
    private PromptRepository promptRepository;

    @Mock
    private QwenService qwenService;

    @InjectMocks
    private AnalysisService analysisService;

    private User testUser;
    private Prompt testPrompt;
    private AnalysisResult testResult;
    private NewsDTO testNewsDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);

        testPrompt = new Prompt();
        testPrompt.setId(1L);
        testPrompt.setUser(testUser);
        testPrompt.setPromptText("Test prompt");

        testResult = new AnalysisResult();
        testResult.setId(1L);
        testResult.setPrompt(testPrompt);
        testResult.setUser(testUser);
        testResult.setAnalysisText("Analysis result");
        testResult.setCacheKey("cache123");
        testResult.setCacheExpiresAt(LocalDateTime.now().plusHours(1));
        testResult.setCreatedAt(LocalDateTime.now());

        testNewsDTO = new NewsDTO(1L, 1L, 1L, 1L, 1L, "Test News", "Content", LocalDateTime.now(),
                100, 10, 20, 15, new BigDecimal("0.25"), "https://t.me/test/1", false, LocalDateTime.now());
    }

    @Test
    void analyzeNews_shouldAnalyzeAndSave() {
        when(promptRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testPrompt));
        when(analysisResultRepository.findByCacheKey(anyString())).thenReturn(Optional.empty());
        when(qwenService.sendToQwen(anyString())).thenReturn("Analysis result");
        when(analysisResultRepository.save(any(AnalysisResult.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AnalysisResultDTO result = analysisService.analyzeNews(1L, 1L, List.of(testNewsDTO));

        assertNotNull(result);
        assertEquals("Analysis result", result.analysisText());
        verify(analysisResultRepository).save(any(AnalysisResult.class));
    }

    @Test
    void analyzeNews_shouldReturnCachedResult() {
        when(promptRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testPrompt));
        when(analysisResultRepository.findByCacheKey(anyString())).thenReturn(Optional.of(testResult));

        AnalysisResultDTO result = analysisService.analyzeNews(1L, 1L, List.of(testNewsDTO));

        assertNotNull(result);
        verify(qwenService, never()).sendToQwen(anyString());
        verify(analysisResultRepository, never()).save(any(AnalysisResult.class));
    }

    @Test
    void analyzeNews_shouldThrowWhenAccessDenied() {
        when(promptRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());
        when(promptRepository.existsById(1L)).thenReturn(true);

        assertThrows(AccessDeniedException.class,
                () -> analysisService.analyzeNews(1L, 2L, List.of(testNewsDTO)));
    }

    @Test
    void getCachedAnalysis_shouldReturnCached() {
        when(analysisResultRepository.findByCacheKey("cache123")).thenReturn(Optional.of(testResult));

        AnalysisResultDTO result = analysisService.getCachedAnalysis("cache123");

        assertNotNull(result);
        assertEquals("Analysis result", result.analysisText());
    }

    @Test
    void getCachedAnalysis_shouldReturnNullWhenExpired() {
        AnalysisResult expiredResult = new AnalysisResult();
        expiredResult.setId(1L);
        expiredResult.setCacheExpiresAt(LocalDateTime.now().minusHours(1));

        when(analysisResultRepository.findByCacheKey("cache123")).thenReturn(Optional.of(expiredResult));

        AnalysisResultDTO result = analysisService.getCachedAnalysis("cache123");

        assertNull(result);
    }

    @Test
    void getAnalysisHistoryWithPrompt_shouldReturnHistory() {
        when(promptRepository.existsByIdAndUserId(1L, 1L)).thenReturn(true);
        when(analysisResultRepository.findAllByPromptIdAndUserIdOrderByCreatedAtDesc(1L, 1L))
                .thenReturn(List.of(testResult));

        List<AnalysisResultDTO> result = analysisService.getAnalysisHistory(1L, 1L);

        assertEquals(1, result.size());
    }

    @Test
    void getAnalysisHistoryWithPrompt_shouldThrowWhenAccessDenied() {
        when(promptRepository.existsByIdAndUserId(1L, 1L)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> analysisService.getAnalysisHistory(1L, 1L));
    }

    @Test
    void getAnalysisHistoryWithPagination_shouldReturnPage() {
        when(analysisResultRepository.findAllByUserId(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(testResult)));

        Page<AnalysisResultDTO> result = analysisService.getAnalysisHistory(1L, Pageable.ofSize(10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void buildPromptWithContext_shouldIncludeMetadata() {
        String prompt = analysisService.buildPromptWithContext("Base prompt", List.of(testNewsDTO));

        assertTrue(prompt.contains("Base prompt"));
        assertTrue(prompt.contains("Test News"));
        assertTrue(prompt.contains("100")); // views
        assertTrue(prompt.contains("10")); // forwards
        assertTrue(prompt.contains("20")); // reactions
        assertTrue(prompt.contains("15")); // likes
        assertTrue(prompt.contains("0.25")); // engagement
    }

    @Test
    void generateCacheKey_shouldGenerateConsistentKey() {
        String key1 = analysisService.generateCacheKey(List.of(testNewsDTO));
        String key2 = analysisService.generateCacheKey(List.of(testNewsDTO));

        assertEquals(key1, key2);
    }

    @Test
    void deleteAnalysisByPrompt_shouldDelete() {
        when(promptRepository.existsByIdAndUserId(1L, 1L)).thenReturn(true);

        assertDoesNotThrow(() -> analysisService.deleteAnalysisByPrompt(1L, 1L));
        verify(analysisResultRepository).deleteAllByPromptIdAndUserId(1L, 1L);
    }

    @Test
    void deleteAnalysisByPrompt_shouldThrowWhenAccessDenied() {
        when(promptRepository.existsByIdAndUserId(1L, 1L)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> analysisService.deleteAnalysisByPrompt(1L, 1L));
    }
}
