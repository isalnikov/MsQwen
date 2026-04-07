package com.isalnikov.msqwen.controller;

import com.isalnikov.msqwen.dto.UserCreateRequestDTO;
import com.isalnikov.msqwen.dto.UserDTO;
import com.isalnikov.msqwen.entity.User;
import com.isalnikov.msqwen.repository.UserRepository;
import com.isalnikov.msqwen.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты для {@link UserController}.
 *
 * <p>Тестируют REST API endpoints через MockMvc.
 * Используют реальную H2 базу данных (in-memory).</p>
 *
 * @author isalnikov
 * @version 1.0
 */
@SpringBootTest

@Transactional
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void registerUser_shouldCreateNewUser() throws Exception {
        UserCreateRequestDTO request = new UserCreateRequestDTO(
                12345L, "testuser", "Test", "User"
        );

        ResultActions result = mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"telegramId\": 12345, \"username\": \"testuser\", \"firstName\": \"Test\", \"lastName\": \"User\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.telegramId").value(12345))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.isActive").value(true));

        // Проверяем что пользователь сохранён в БД
        assert userRepository.count() == 1;
    }

    @Test
    void registerUser_shouldReturnExistingUser() throws Exception {
        // Создаём пользователя
        userService.registerUser(12345L, "testuser", "Test", "User");

        // Регистрируем того же пользователя
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"telegramId\": 12345, \"username\": \"testuser\", \"firstName\": \"Test\", \"lastName\": \"User\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.telegramId").value(12345));

        // Проверяем что создан только один пользователь
        assert userRepository.count() == 1;
    }

    @Test
    void getCurrentUser_shouldReturnUser() throws Exception {
        // Создаём пользователя
        userService.registerUser(12345L, "testuser", "Test", "User");

        mockMvc.perform(get("/api/v1/users/me")
                        .param("telegramId", "12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.telegramId").value(12345))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void getCurrentUser_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")
                        .param("telegramId", "99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_shouldUpdateUser() throws Exception {
        // Создаём пользователя
        userService.registerUser(12345L, "testuser", "Test", "User");

        mockMvc.perform(put("/api/v1/users/me")
                        .param("telegramId", "12345")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"telegramId\": 12345, \"username\": \"updateduser\", \"firstName\": \"Updated\", \"lastName\": \"Name\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updateduser"))
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("Name"));
    }

    @Test
    void deleteUser_shouldDeleteUser() throws Exception {
        // Создаём пользователя
        userService.registerUser(12345L, "testuser", "Test", "User");
        assert userRepository.count() == 1;

        mockMvc.perform(delete("/api/v1/users/me")
                        .param("telegramId", "12345"))
                .andExpect(status().isNoContent());

        assert userRepository.count() == 0;
    }

    @Test
    void registerUser_shouldReturnBadRequest_forInvalidData() throws Exception {
        // telegramId null - должно вызвать ошибку валидации
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"testuser\"}"))
                .andExpect(status().isBadRequest());
    }
}
