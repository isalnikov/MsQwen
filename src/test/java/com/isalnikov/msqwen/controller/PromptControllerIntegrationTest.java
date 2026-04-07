package com.isalnikov.msqwen.controller;

import com.isalnikov.msqwen.entity.Prompt;
import com.isalnikov.msqwen.entity.User;
import com.isalnikov.msqwen.repository.PromptRepository;
import com.isalnikov.msqwen.repository.UserRepository;
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
 * Интеграционные тесты для {@link PromptController}.
 *
 * @author isalnikov
 * @version 1.0
 */
@SpringBootTest

@Transactional
class PromptControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PromptRepository promptRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PromptService promptService;

    private Long userId;

    @BeforeEach
    void setUp() {
        promptRepository.deleteAll();
        userRepository.deleteAll();
        userId = userService.registerUser(12345L, "testuser", "Test", "User").id();
    }

    @Test
    void createPrompt_shouldCreatePrompt() throws Exception {
        mockMvc.perform(post("/api/v1/prompts")
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Test Prompt\", \"description\": \"Test Description\", \"promptText\": \"Analyze this news\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Prompt"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.promptText").value("Analyze this news"))
                .andExpect(jsonPath("$.userId").value(userId));
    }

    @Test
    void getUserPrompts_shouldReturnPrompts() throws Exception {
        // Создаём промпт
        promptService.createPrompt(userId, "Prompt 1", "Description 1", "Text 1");

        mockMvc.perform(get("/api/v1/prompts")
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Prompt 1"));
    }

    @Test
    void getUserPrompts_shouldReturnEmpty() throws Exception {
        mockMvc.perform(get("/api/v1/prompts")
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getPrompt_shouldReturnPrompt() throws Exception {
        Long promptId = promptService.createPrompt(userId, "Prompt 1", "Desc", "Text").id();

        mockMvc.perform(get("/api/v1/prompts/{id}", promptId)
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(promptId))
                .andExpect(jsonPath("$.name").value("Prompt 1"));
    }

    @Test
    void getPrompt_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/prompts/999")
                        .param("userId", userId.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatePrompt_shouldUpdatePrompt() throws Exception {
        Long promptId = promptService.createPrompt(userId, "Prompt 1", "Desc", "Text").id();

        mockMvc.perform(put("/api/v1/prompts/{id}", promptId)
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Updated Name\", \"description\": \"Updated Desc\", \"promptText\": \"Updated Text\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.description").value("Updated Desc"))
                .andExpect(jsonPath("$.promptText").value("Updated Text"));
    }

    @Test
    void deletePrompt_shouldDeletePrompt() throws Exception {
        Long promptId = promptService.createPrompt(userId, "Prompt 1", "Desc", "Text").id();

        mockMvc.perform(delete("/api/v1/prompts/{id}", promptId)
                        .param("userId", userId.toString()))
                .andExpect(status().isNoContent());

        assert promptRepository.count() == 0;
    }

    @Test
    void createPrompt_shouldReturnBadRequest_forInvalidData() throws Exception {
        mockMvc.perform(post("/api/v1/prompts")
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\": \"Test\"}"))
                .andExpect(status().isBadRequest());
    }
}
