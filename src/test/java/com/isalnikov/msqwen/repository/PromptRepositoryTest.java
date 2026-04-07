package com.isalnikov.msqwen.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.isalnikov.msqwen.entity.Prompt;
import com.isalnikov.msqwen.entity.User;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Тесты для PromptRepository.
 * 
 * <p>Проверяют методы поиска сохранения удаления и изоляцию данных по user_id.</p>
 * 
 * @author isalnikov
 * @version 1.0
 */
@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
@org.springframework.transaction.annotation.Transactional
@DisplayName("Тесты PromptRepository")
class PromptRepositoryTest {

    @Autowired
    private PromptRepository promptRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private User testUser;
    private Prompt testPrompt;

    /**
     * Подготавливает тестовые данные перед каждым тестом.
     */
    @BeforeEach
    void setUp() {
        testUser = new User(12345L, "testuser", "Test", "User");
        userRepository.save(testUser);

        testPrompt = new Prompt(testUser, "Test Prompt", "Test Description", "Analyze this news...");
        promptRepository.save(testPrompt);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Находит все промпты пользователя")
    void findAllByUserId_shouldReturnUserPrompts() {
        // Act
        List<Prompt> prompts = promptRepository.findAllByUserId(testUser.getId());

        // Assert
        assertThat(prompts).hasSize(1);
        assertThat(prompts.get(0).getName()).isEqualTo("Test Prompt");
    }

    @Test
    @DisplayName("Находит активные промпты пользователя")
    void findAllByUserIdAndIsActive_shouldReturnActivePrompts() {
        // Act
        List<Prompt> prompts = promptRepository.findAllByUserIdAndIsActive(testUser.getId(), true);

        // Assert
        assertThat(prompts).hasSize(1);
    }

    @Test
    @DisplayName("Находит промпт по ID и пользователю")
    void findByIdAndUserId_shouldReturnPrompt() {
        // Act
        Optional<Prompt> found = promptRepository.findByIdAndUserId(testPrompt.getId(), testUser.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Prompt");
    }

    @Test
    @DisplayName("Не находит чужой промпт")
    void findByIdAndUserId_shouldReturnEmptyForOtherUser() {
        // Act
        Optional<Prompt> found = promptRepository.findByIdAndUserId(testPrompt.getId(), 99999L);

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Проверяет существование промпта у пользователя")
    void existsByIdAndUserId_shouldReturnTrue() {
        // Act
        boolean exists = promptRepository.existsByIdAndUserId(testPrompt.getId(), testUser.getId());

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Возвращает количество промптов пользователя")
    void countByUserId_shouldReturnCount() {
        // Act
        long count = promptRepository.countByUserId(testUser.getId());

        // Assert
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("Удаляет промпт пользователя с проверкой прав")
    void deleteByIdAndUserId_shouldDeletePrompt() {
        // Act
        promptRepository.deleteByIdAndUserId(testPrompt.getId(), testUser.getId());
        entityManager.flush();

        // Assert
        Optional<Prompt> deleted = promptRepository.findById(testPrompt.getId());
        assertThat(deleted).isEmpty();
    }
}
