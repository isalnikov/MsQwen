package com.isalnikov.msqwen.scheduler;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.isalnikov.msqwen.entity.Channel;
import com.isalnikov.msqwen.entity.User;
import com.isalnikov.msqwen.parser.NewsParserService;
import com.isalnikov.msqwen.repository.ChannelRepository;
import com.isalnikov.msqwen.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit тесты для {@link NewsParsingScheduler}.
 *
 * @author isalnikov
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class NewsParsingSchedulerTest {

    @Mock
    private NewsParserService newsParserService;

    @Mock
    private ChannelRepository channelRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NewsParsingScheduler newsParsingScheduler;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(newsParsingScheduler, "parseEnabled", true);
    }

    @Test
    void testParseAllActiveChannels_SchedulerDisabled() {
        // Подготовка
        ReflectionTestUtils.setField(newsParsingScheduler, "parseEnabled", false);

        // Выполнение
        newsParsingScheduler.parseAllActiveChannels();

        // Проверка
        verify(userRepository, never()).findAll();
        verify(newsParserService, never()).parseAllChannels(anyLong(), anyList());
    }

    @Test
    void testParseAllActiveChannels_NoUsers() {
        // Подготовка
        when(userRepository.findAll()).thenReturn(List.of());

        // Выполнение
        newsParsingScheduler.parseAllActiveChannels();

        // Проверка
        verify(userRepository).findAll();
        verify(channelRepository, never()).findAllByUserIdAndIsActive(anyLong(), anyBoolean());
    }

    @Test
    void testParseAllActiveChannels_NoChannels() {
        // Подготовка
        User user = new User();
        user.setId(1L);
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(channelRepository.findAllByUserIdAndIsActive(1L, true)).thenReturn(List.of());

        // Выполнение
        newsParsingScheduler.parseAllActiveChannels();

        // Проверка
        verify(channelRepository).findAllByUserIdAndIsActive(1L, true);
        verify(newsParserService, never()).parseAllChannels(anyLong(), anyList());
    }

    @Test
    void testParseAllActiveChannels_Success() {
        // Подготовка
        User user = new User();
        user.setId(1L);

        Channel channel = new Channel();
        channel.setId(1L);
        channel.setTelegramHandle("@test");
        channel.setIsActive(true);

        when(userRepository.findAll()).thenReturn(List.of(user));
        when(channelRepository.findAllByUserIdAndIsActive(1L, true)).thenReturn(List.of(channel));
        when(newsParserService.parseAllChannels(eq(1L), anyList())).thenReturn(5);

        // Выполнение
        newsParsingScheduler.parseAllActiveChannels();

        // Проверка
        verify(newsParserService).parseAllChannels(eq(1L), eq(List.of(channel)));
    }

    @Test
    void testParseAllActiveChannels_ExceptionHandled() {
        // Подготовка
        when(userRepository.findAll()).thenThrow(new RuntimeException("Ошибка БД"));

        // Выполнение - не должно выбрасывать исключение
        newsParsingScheduler.parseAllActiveChannels();

        // Проверка - исключение обработано внутри
        verify(userRepository).findAll();
    }
}
