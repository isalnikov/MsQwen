package com.isalnikov.msqwen.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.isalnikov.msqwen.dto.PromptDTO;
import com.isalnikov.msqwen.entity.Prompt;
import com.isalnikov.msqwen.entity.User;
import com.isalnikov.msqwen.exception.AccessDeniedException;
import com.isalnikov.msqwen.exception.ResourceNotFoundException;
import com.isalnikov.msqwen.repository.PromptRepository;
import com.isalnikov.msqwen.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Тесты для PromptService с использованием Mockito.
 * 
 * @author isalnikov
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты PromptService")
class PromptServiceTest {

    @Mock
    private PromptRepository promptRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PromptService promptService;

    private User testUser;
    private Prompt testPrompt;

    /**
     * Подготавливает тестовые данные.
     */
    @BeforeEach
    void setUp() {
        testUser = new User(12345L, "testuser", "Test", "User");
        testUser.setId(1L);
        
        testPrompt = new Prompt(testUser, "Test Prompt", "Description", "Prompt text");
        testPrompt.setId(1L);
    }

    @Test
    @DisplayName("Создаёт новый промпт для пользователя")
    void createPrompt_shouldCreatePrompt() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(promptRepository.save(any(Prompt.class))).thenReturn(testPrompt);

        // Act
        PromptDTO result = promptService.createPrompt(1L, "Test Prompt", "Description", "Prompt text");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Test Prompt");
        verify(promptRepository).save(any(Prompt.class));
    }

    @Test
    @DisplayName("Выбрасывает исключение если пользователь не найден")
    void createPrompt_shouldThrowExceptionForNonExistentUser() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> promptService.createPrompt(999L, "Test", "Desc", "Text"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Пользователь не найден");
    }

    @Test
    @DisplayName("Возвращает все промпты пользователя")
    void getAllPromptsByUser_shouldReturnPrompts() {
        // Arrange
        when(promptRepository.findAllByUserId(1L)).thenReturn(List.of(testPrompt));

        // Act
        List<PromptDTO> result = promptService.getAllPromptsByUser(1L);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Test Prompt");
    }

    @Test
    @DisplayName("Возвращает промпт с проверкой прав")
    void getPromptByIdAndUser_shouldReturnPrompt() {
        // Arrange
        when(promptRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testPrompt));

        // Act
        PromptDTO result = promptService.getPromptByIdAndUser(1L, 1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Test Prompt");
    }

    @Test
    @DisplayName("Выбрасывает AccessDeniedException для чужого промпта")
    void getPromptByIdAndUser_shouldThrowAccessDeniedException() {
        // Arrange
        when(promptRepository.findByIdAndUserId(1L, 2L)).thenReturn(Optional.empty());
        when(promptRepository.existsById(1L)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> promptService.getPromptByIdAndUser(1L, 2L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Доступ к промпту запрещён");
    }

    @Test
    @DisplayName("Удаляет промпт с проверкой прав")
    void deletePrompt_shouldDeleteWithOwnershipCheck() {
        // Arrange
        when(promptRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testPrompt));

        // Act
        promptService.deletePrompt(1L, 1L);

        // Assert
        verify(promptRepository).delete(testPrompt);
    }

    @Test
    @DisplayName("Возвращает количество промптов пользователя")
    void countByUser_shouldReturnCount() {
        // Arrange
        when(promptRepository.countByUserId(1L)).thenReturn(3L);

        // Act
        long count = promptService.countByUser(1L);

        // Assert
        assertThat(count).isEqualTo(3);
    }
}
