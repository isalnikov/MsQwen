package com.isalnikov.msqwen.controller;

import com.isalnikov.msqwen.entity.Channel;
import com.isalnikov.msqwen.entity.Prompt;
import com.isalnikov.msqwen.entity.User;
import com.isalnikov.msqwen.repository.ChannelRepository;
import com.isalnikov.msqwen.repository.PromptRepository;
import com.isalnikov.msqwen.repository.UserRepository;
import com.isalnikov.msqwen.service.ChannelService;
import com.isalnikov.msqwen.service.PromptService;
import com.isalnikov.msqwen.service.UserService;
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
 * Интеграционные тесты для {@link ChannelController}.
 *
 * @author isalnikov
 * @version 1.0
 */
@SpringBootTest

@Transactional
class ChannelControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PromptRepository promptRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PromptService promptService;

    @Autowired
    private ChannelService channelService;

    private Long userId;
    private Long promptId;

    @BeforeEach
    void setUp() {
        channelRepository.deleteAll();
        promptRepository.deleteAll();
        userRepository.deleteAll();

        userId = userService.registerUser(12345L, "testuser", "Test", "User").id();
        promptId = promptService.createPrompt(userId, "Test Prompt", "Desc", "Text").id();
    }

    @Test
    void createChannel_shouldCreateChannel() throws Exception {
        mockMvc.perform(post("/api/v1/channels")
                        .param("promptId", promptId.toString())
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Test Channel\", \"telegramHandle\": \"@testchannel\", \"telegramUrl\": \"https://t.me/testchannel\", \"description\": \"Test Desc\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Channel"))
                .andExpect(jsonPath("$.telegramHandle").value("@testchannel"))
                .andExpect(jsonPath("$.description").value("Test Desc"));
    }

    @Test
    void getChannelsByPrompt_shouldReturnChannels() throws Exception {
        channelService.createChannel(promptId, userId, "Channel 1", "@channel1", "https://t.me/channel1", "Desc 1");

        mockMvc.perform(get("/api/v1/channels/prompt/{promptId}", promptId)
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Channel 1"));
    }

    @Test
    void getChannel_shouldReturnChannel() throws Exception {
        Long channelId = channelService.createChannel(promptId, userId, "Channel 1", "@channel1", null, "Desc").id();

        mockMvc.perform(get("/api/v1/channels/{id}", channelId)
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(channelId))
                .andExpect(jsonPath("$.name").value("Channel 1"));
    }

    @Test
    void getChannel_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/channels/999")
                        .param("userId", userId.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateChannel_shouldUpdateChannel() throws Exception {
        Long channelId = channelService.createChannel(promptId, userId, "Channel 1", "@channel1", null, "Desc").id();

        mockMvc.perform(put("/api/v1/channels/{id}", channelId)
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Updated Name\", \"telegramHandle\": \"@updated\", \"telegramUrl\": \"https://t.me/updated\", \"description\": \"Updated Desc\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.telegramHandle").value("@updated"))
                .andExpect(jsonPath("$.description").value("Updated Desc"));
    }

    @Test
    void deleteChannel_shouldDeleteChannel() throws Exception {
        Long channelId = channelService.createChannel(promptId, userId, "Channel 1", "@channel1", null, "Desc").id();

        mockMvc.perform(delete("/api/v1/channels/{id}", channelId)
                        .param("userId", userId.toString()))
                .andExpect(status().isNoContent());

        assert channelRepository.count() == 0;
    }

    @Test
    void createChannel_shouldReturnBadRequest_forInvalidData() throws Exception {
        mockMvc.perform(post("/api/v1/channels")
                        .param("promptId", promptId.toString())
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"telegramHandle\": \"@test\"}"))
                .andExpect(status().isBadRequest());
    }
}
