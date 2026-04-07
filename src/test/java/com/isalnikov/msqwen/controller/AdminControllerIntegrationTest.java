package com.isalnikov.msqwen.controller;

import com.isalnikov.msqwen.entity.Channel;
import com.isalnikov.msqwen.entity.News;
import com.isalnikov.msqwen.entity.Prompt;
import com.isalnikov.msqwen.entity.User;
import com.isalnikov.msqwen.repository.*;
import com.isalnikov.msqwen.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты для {@link AdminController}.
 *
 * @author isalnikov
 * @version 1.0
 */
@SpringBootTest

@Transactional
class AdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PromptRepository promptRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private NewsRepository newsRepository;

    @Autowired
    private AnalysisResultRepository analysisResultRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PromptService promptService;

    @Autowired
    private ChannelService channelService;

    @BeforeEach
    void setUp() {
        analysisResultRepository.deleteAll();
        newsRepository.deleteAll();
        channelRepository.deleteAll();
        promptRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void getSystemStats_shouldReturnStats() throws Exception {
        // Создаём тестовые данные
        Long userId = userService.registerUser(12345L, "testuser", "Test", "User").id();
        promptService.createPrompt(userId, "Prompt 1", "Desc", "Text");
        Long promptId = promptService.createPrompt(userId, "Prompt 2", "Desc", "Text").id();
        channelService.createChannel(promptId, userId, "Channel 1", "@channel1", null, "Desc");

        mockMvc.perform(get("/api/v1/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").isNumber())
                .andExpect(jsonPath("$.totalPrompts").isNumber())
                .andExpect(jsonPath("$.totalChannels").isNumber())
                .andExpect(jsonPath("$.totalNews").isNumber())
                .andExpect(jsonPath("$.totalAnalyzed").isNumber());
    }

    @Test
    void cleanupNews_shouldCleanupWithoutConfirmation() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/cleanup/news"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cleanupNews_shouldCleanupWithConfirmation() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/cleanup/news")
                        .header("X-Confirmation", "CONFIRM"))
                .andExpect(status().isNoContent());
    }

    @Test
    void cleanupChannels_shouldCleanupWithConfirmation() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/cleanup/channels")
                        .header("X-Confirmation", "CONFIRM"))
                .andExpect(status().isNoContent());
    }

    @Test
    void cleanupUsers_shouldCleanupWithConfirmation() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/cleanup/users")
                        .header("X-Confirmation", "CONFIRM"))
                .andExpect(status().isNoContent());

        assert userRepository.count() == 0;
    }

    @Test
    void cleanupAll_shouldCleanupAllTables() throws Exception {
        // Создаём тестовые данные
        Long userId = userService.registerUser(12345L, "testuser", "Test", "User").id();
        Long promptId = promptService.createPrompt(userId, "Prompt 1", "Desc", "Text").id();
        channelService.createChannel(promptId, userId, "Channel 1", "@channel1", null, "Desc");

        mockMvc.perform(delete("/api/v1/admin/cleanup/all")
                        .header("X-Confirmation", "CONFIRM"))
                .andExpect(status().isNoContent());

        assert userRepository.count() == 0;
        assert promptRepository.count() == 0;
        assert channelRepository.count() == 0;
    }
}
