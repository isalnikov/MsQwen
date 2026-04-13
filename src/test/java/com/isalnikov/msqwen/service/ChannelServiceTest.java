package com.isalnikov.msqwen.service;

import com.isalnikov.msqwen.dto.ChannelDTO;
import com.isalnikov.msqwen.entity.Channel;
import com.isalnikov.msqwen.entity.Prompt;
import com.isalnikov.msqwen.entity.User;
import com.isalnikov.msqwen.exception.AccessDeniedException;
import com.isalnikov.msqwen.exception.ResourceNotFoundException;
import com.isalnikov.msqwen.repository.ChannelRepository;
import com.isalnikov.msqwen.repository.PromptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit тесты для {@link ChannelService}.
 *
 * @author isalnikov
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)

class ChannelServiceTest {

    @Mock
    private ChannelRepository channelRepository;

    @Mock
    private PromptRepository promptRepository;

    @InjectMocks
    private ChannelService channelService;

    private User testUser;
    private Prompt testPrompt;
    private Channel testChannel;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setTelegramId(12345L);

        testPrompt = new Prompt();
        testPrompt.setId(1L);
        testPrompt.setUser(testUser);

        testChannel = new Channel(testPrompt, testUser, "Test Channel", "@test", "https://t.me/test", "Desc");
        testChannel.setId(1L);
    }

    @Test
    void createChannel_shouldCreateChannel() {
        when(promptRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testPrompt));
        when(channelRepository.save(any(Channel.class))).thenAnswer(invocation -> {
            Channel c = invocation.getArgument(0);
            c.setId(1L);
            return c;
        });

        ChannelDTO result = channelService.createChannel(1L, 1L, "Test", "@test", "https://t.me/test", "Desc");

        assertNotNull(result);
        assertEquals("Test", result.name());
        verify(channelRepository).save(any(Channel.class));
    }

    @Test
    void createChannel_shouldThrowWhenPromptNotFound() {
        when(promptRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());
        when(promptRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> channelService.createChannel(1L, 1L, "Test", "@test", null, "Desc"));
    }

    @Test
    void createChannel_shouldThrowWhenAccessDenied() {
        when(promptRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());
        when(promptRepository.existsById(1L)).thenReturn(true);

        assertThrows(AccessDeniedException.class,
                () -> channelService.createChannel(1L, 2L, "Test", "@test", null, "Desc"));
    }

    @Test
    void getChannelsByPrompt_shouldReturnChannels() {
        when(promptRepository.existsByIdAndUserId(1L, 1L)).thenReturn(true);
        when(channelRepository.findAllByPromptIdAndUserId(1L, 1L)).thenReturn(List.of(testChannel));

        List<ChannelDTO> result = channelService.getChannelsByPrompt(1L, 1L);

        assertEquals(1, result.size());
        assertEquals("Test Channel", result.get(0).name());
    }

    @Test
    void getChannel_shouldReturnChannel() {
        when(channelRepository.findById(1L)).thenReturn(Optional.of(testChannel));

        ChannelDTO result = channelService.getChannel(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
    }

    @Test
    void getChannel_shouldThrowWhenNotFound() {
        when(channelRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> channelService.getChannel(1L, 1L));
    }

    @Test
    void getChannel_shouldThrowWhenAccessDenied() {
        User otherUser = new User();
        otherUser.setId(2L);
        Channel otherChannel = new Channel(testPrompt, otherUser, "Other", "@other", null, null);
        otherChannel.setId(1L);

        when(channelRepository.findById(1L)).thenReturn(Optional.of(otherChannel));

        assertThrows(AccessDeniedException.class, () -> channelService.getChannel(1L, 1L));
    }

    @Test
    void updateChannel_shouldUpdateChannel() {
        when(channelRepository.findById(1L)).thenReturn(Optional.of(testChannel));
        when(channelRepository.save(any(Channel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChannelDTO result = channelService.updateChannel(1L, 1L, "Updated", "@updated", "https://t.me/updated", "Updated Desc");

        assertEquals("Updated", result.name());
        assertEquals("@updated", result.telegramHandle());
        verify(channelRepository).save(any(Channel.class));
    }

    @Test
    void deleteChannel_shouldDeleteChannel() {
        when(channelRepository.findById(1L)).thenReturn(Optional.of(testChannel));

        assertDoesNotThrow(() -> channelService.deleteChannel(1L, 1L));
        verify(channelRepository).delete(testChannel);
    }

    @Test
    void deleteChannel_shouldThrowWhenAccessDenied() {
        User otherUser = new User();
        otherUser.setId(2L);
        Channel otherChannel = new Channel(testPrompt, otherUser, "Other", "@other", null, null);
        otherChannel.setId(1L);

        when(channelRepository.findById(1L)).thenReturn(Optional.of(otherChannel));

        assertThrows(AccessDeniedException.class, () -> channelService.deleteChannel(1L, 1L));
    }

    @Test
    void getAllUserChannels_shouldReturnAllChannels() {
        when(channelRepository.findAllByUserId(1L)).thenReturn(List.of(testChannel));

        List<ChannelDTO> result = channelService.getAllUserChannels(1L);

        assertEquals(1, result.size());
    }

    @Test
    void countByPrompt_shouldReturnCount() {
        when(channelRepository.countByPromptIdAndUserId(1L, 1L)).thenReturn(5L);

        long count = channelService.countByPrompt(1L, 1L);

        assertEquals(5L, count);
    }
}
