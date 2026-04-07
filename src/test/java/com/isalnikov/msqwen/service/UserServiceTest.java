package com.isalnikov.msqwen.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.isalnikov.msqwen.dto.UserDTO;
import com.isalnikov.msqwen.entity.User;
import com.isalnikov.msqwen.exception.ResourceNotFoundException;
import com.isalnikov.msqwen.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Тесты для UserService с использованием Mockito.
 * 
 * @author isalnikov
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты UserService")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    /**
     * Подготавливает тестовые данные.
     */
    @BeforeEach
    void setUp() {
        testUser = new User(12345L, "testuser", "Test", "User");
        testUser.setId(1L);
    }

    @Test
    @DisplayName("Регистрирует нового пользователя")
    void registerUser_shouldRegisterNewUser() {
        // Arrange
        when(userRepository.findByTelegramId(12345L)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserDTO result = userService.registerUser(12345L, "testuser", "Test", "User");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.telegramId()).isEqualTo(12345L);
        assertThat(result.username()).isEqualTo("testuser");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Возвращает существующего пользователя при повторной регистрации")
    void registerUser_shouldReturnExistingUser() {
        // Arrange
        when(userRepository.findByTelegramId(12345L)).thenReturn(Optional.of(testUser));

        // Act
        UserDTO result = userService.registerUser(12345L, "testuser", "Test", "User");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.telegramId()).isEqualTo(12345L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Находит пользователя по Telegram ID")
    void getUserByTelegramId_shouldReturnUser() {
        // Arrange
        when(userRepository.findByTelegramId(12345L)).thenReturn(Optional.of(testUser));

        // Act
        UserDTO result = userService.getUserByTelegramId(12345L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.telegramId()).isEqualTo(12345L);
    }

    @Test
    @DisplayName("Выбрасывает исключение если пользователь не найден")
    void getUserByTelegramId_shouldThrowException() {
        // Arrange
        when(userRepository.findByTelegramId(99999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getUserByTelegramId(99999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Пользователь не найден");
    }

    @Test
    @DisplayName("Возвращает количество активных пользователей")
    void getActiveUsersCount_shouldReturnCount() {
        // Arrange
        when(userRepository.countByIsActive(true)).thenReturn(5L);

        // Act
        long count = userService.getActiveUsersCount(true);

        // Assert
        assertThat(count).isEqualTo(5);
    }
}
