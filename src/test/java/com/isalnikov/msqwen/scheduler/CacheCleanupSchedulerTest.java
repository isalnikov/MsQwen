package com.isalnikov.msqwen.scheduler;

import static org.mockito.Mockito.*;

import com.isalnikov.msqwen.repository.AnalysisResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit тесты для {@link CacheCleanupScheduler}.
 *
 * @author isalnikov
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class CacheCleanupSchedulerTest {

    @Mock
    private AnalysisResultRepository analysisResultRepository;

    @InjectMocks
    private CacheCleanupScheduler cacheCleanupScheduler;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(cacheCleanupScheduler, "cacheCleanupEnabled", true);
    }

    @Test
    void testCleanupExpiredCache_SchedulerDisabled() {
        // Подготовка
        ReflectionTestUtils.setField(cacheCleanupScheduler, "cacheCleanupEnabled", false);

        // Выполнение
        cacheCleanupScheduler.cleanupExpiredCache();

        // Проверка
        verify(analysisResultRepository, never()).deleteByCacheExpiresAtBefore(any());
    }

    @Test
    void testCleanupExpiredCache_Success() {
        // Подготовка
        when(analysisResultRepository.count()).thenReturn(100L, 80L);

        // Выполнение
        cacheCleanupScheduler.cleanupExpiredCache();

        // Проверка
        verify(analysisResultRepository).deleteByCacheExpiresAtBefore(any());
        verify(analysisResultRepository, times(2)).count();
    }

    @Test
    void testCleanupExpiredCache_ExceptionHandled() {
        // Подготовка
        when(analysisResultRepository.count()).thenThrow(new RuntimeException("Ошибка БД"));

        // Выполнение - не должно выбрасывать исключение
        cacheCleanupScheduler.cleanupExpiredCache();

        // Проверка - исключение обработано
        verify(analysisResultRepository).count();
    }
}
