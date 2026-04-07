package com.isalnikov.msqwen.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.isalnikov.msqwen.entity.News;
import com.isalnikov.msqwen.repository.NewsRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit тесты для {@link NewsCleanupScheduler}.
 *
 * @author isalnikov
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class NewsCleanupSchedulerTest {

    @Mock
    private NewsRepository newsRepository;

    @InjectMocks
    private NewsCleanupScheduler newsCleanupScheduler;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(newsCleanupScheduler, "cleanupEnabled", true);
        ReflectionTestUtils.setField(newsCleanupScheduler, "cleanupDays", 30);
    }

    @Test
    void testDeleteOldNews_SchedulerDisabled() {
        // Подготовка
        ReflectionTestUtils.setField(newsCleanupScheduler, "cleanupEnabled", false);

        // Выполнение
        newsCleanupScheduler.deleteOldNews();

        // Проверка
        verify(newsRepository, never()).findAllByPublicationDateBefore(any());
    }

    @Test
    void testDeleteOldNews_NoOldNews() {
        // Подготовка
        when(newsRepository.findAllByPublicationDateBefore(any(LocalDateTime.class)))
                .thenReturn(List.of());

        // Выполнение
        newsCleanupScheduler.deleteOldNews();

        // Проверка
        verify(newsRepository).findAllByPublicationDateBefore(any(LocalDateTime.class));
        verify(newsRepository, never()).deleteAll(anyList());
    }

    @Test
    void testDeleteOldNews_Success() {
        // Подготовка
        News oldNews1 = new News();
        oldNews1.setId(1L);
        News oldNews2 = new News();
        oldNews2.setId(2L);

        when(newsRepository.findAllByPublicationDateBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(oldNews1, oldNews2));

        // Выполнение
        newsCleanupScheduler.deleteOldNews();

        // Проверка
        verify(newsRepository).findAllByPublicationDateBefore(any(LocalDateTime.class));
        verify(newsRepository).deleteAll(List.of(oldNews1, oldNews2));
    }

    @Test
    void testDeleteOldNews_ExceptionHandled() {
        // Подготовка
        when(newsRepository.findAllByPublicationDateBefore(any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("Ошибка БД"));

        // Выполнение - не должно выбрасывать исключение
        newsCleanupScheduler.deleteOldNews();

        // Проверка - исключение обработано
        verify(newsRepository).findAllByPublicationDateBefore(any(LocalDateTime.class));
    }
}
