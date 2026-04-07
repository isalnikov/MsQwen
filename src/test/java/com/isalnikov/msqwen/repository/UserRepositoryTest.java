package com.isalnikov.msqwen.repository;

import static org.assertj.core.api.Assertions.assertThat;

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
 * Тесты для UserRepository.
 * 
 * <p>Проверяют методы поиска сохранения и удаления пользователей.</p>
 * 
 * @author isalnikov
 * @version 1.0
 */
@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
@org.springframework.transaction.annotation.Transactional
@DisplayName("Тесты UserRepository")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private User testUser;

    /**
     * Подготавливает тестовые данные перед каждым тестом.
     */
    @BeforeEach
    void setUp() {
        testUser = new User(12345L, "testuser", "Test", "User");
        userRepository.save(testUser);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Находит пользователя по Telegram ID")
    void findByTelegramId_shouldFindUser() {
        // Act
        Optional<User> found = userRepository.findByTelegramId(12345L);

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
        assertThat(found.get().getFirstName()).isEqualTo("Test");
    }

    @Test
    @DisplayName("Не находит пользователя по несуществующему Telegram ID")
    void findByTelegramId_shouldReturnEmptyForNonExistentId() {
        // Act
        Optional<User> found = userRepository.findByTelegramId(99999L);

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Проверяет существование пользователя по Telegram ID")
    void existsByTelegramId_shouldReturnTrue() {
        // Act
        boolean exists = userRepository.existsByTelegramId(12345L);

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Проверяет отсутствие пользователя по Telegram ID")
    void existsByTelegramId_shouldReturnFalse() {
        // Act
        boolean exists = userRepository.existsByTelegramId(99999L);

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Находит всех активных пользователей")
    void findAllByIsActive_shouldReturnActiveUsers() {
        // Arrange
        User inactiveUser = new User(67890L, "inactive", "Inactive", "User");
        inactiveUser.setIsActive(false);
        userRepository.save(inactiveUser);
        entityManager.flush();

        // Act
        List<User> activeUsers = userRepository.findAllByIsActive(true);

        // Assert
        assertThat(activeUsers).hasSize(1);
        assertThat(activeUsers.get(0).getTelegramId()).isEqualTo(12345L);
    }

    @Test
    @DisplayName("Возвращает количество активных пользователей")
    void countByIsActive_shouldReturnCount() {
        // Act
        long count = userRepository.countByIsActive(true);

        // Assert
        assertThat(count).isEqualTo(1);
    }
}
