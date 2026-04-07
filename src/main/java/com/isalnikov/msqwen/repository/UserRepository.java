package com.isalnikov.msqwen.repository;

import com.isalnikov.msqwen.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA репозиторий для работы с пользователями.
 * 
 * <p>Обеспечивает доступ к таблице users в базе данных.
 * Все методы для поиска по telegramId учитывают уникальность этого поля.</p>
 * 
 * @author isalnikov
 * @version 1.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Находит пользователя по Telegram ID.
     *
     * @param telegramId Telegram ID пользователя
     * @return Optional с пользователем если найден
     */
    Optional<User> findByTelegramId(Long telegramId);

    /**
     * Проверяет существует ли пользователь с указанным Telegram ID.
     *
     * @param telegramId Telegram ID для проверки
     * @return true если пользователь существует
     */
    boolean existsByTelegramId(Long telegramId);

    /**
     * Находит всех активных пользователей.
     *
     * @param isActive флаг активности
     * @return список активных пользователей
     */
    List<User> findAllByIsActive(Boolean isActive);

    /**
     * Возвращает количество активных пользователей.
     *
     * @param isActive флаг активности
     * @return количество пользователей
     */
    long countByIsActive(Boolean isActive);
}
