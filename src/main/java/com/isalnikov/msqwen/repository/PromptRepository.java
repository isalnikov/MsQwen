package com.isalnikov.msqwen.repository;

import com.isalnikov.msqwen.entity.Prompt;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA репозиторий для работы с промптами.
 * 
 * <p>Обеспечивает доступ к таблице prompts в базе данных.
 * Все методы учитывают изоляцию данных по user_id - пользователь может работать
 * только со своими промптами.</p>
 * 
 * @author isalnikov
 * @version 1.0
 */
@Repository
public interface PromptRepository extends JpaRepository<Prompt, Long> {

    /**
     * Находит все промпты принадлежащие пользователю.
     *
     * @param userId идентификатор пользователя
     * @return список промптов пользователя
     */
    List<Prompt> findAllByUserId(Long userId);

    /**
     * Находит все активные промпты принадлежащие пользователю.
     *
     * @param userId идентификатор пользователя
     * @param isActive флаг активности
     * @return список активных промптов пользователя
     */
    List<Prompt> findAllByUserIdAndIsActive(Long userId, Boolean isActive);

    /**
     * Находит промпт по идентификатору и пользователю (для проверки прав).
     *
     * @param id идентификатор промпта
     * @param userId идентификатор пользователя
     * @return Optional с промптом если найден и принадлежит пользователю
     */
    Optional<Prompt> findByIdAndUserId(Long id, Long userId);

    /**
     * Проверяет существует ли промпт с указанным идентификатором у пользователя.
     *
     * @param id идентификатор промпта
     * @param userId идентификатор пользователя
     * @return true если промпт существует и принадлежит пользователю
     */
    boolean existsByIdAndUserId(Long id, Long userId);

    /**
     * Возвращает количество промптов у пользователя.
     *
     * @param userId идентификатор пользователя
     * @return количество промптов
     */
    long countByUserId(Long userId);

    /**
     * Удаляет промпт по идентификатору и пользователю (с проверкой прав).
     * Каскадно удаляются каналы новости и результаты анализа.
     *
     * @param id идентификатор промпта
     * @param userId идентификатор пользователя
     */
    void deleteByIdAndUserId(Long id, Long userId);

    /**
     * Возвращает количество активных промптов у пользователя.
     *
     * @param userId идентификатор пользователя
     * @param isActive флаг активности
     * @return количество активных промптов
     */
    long countByUserIdAndIsActive(Long userId, Boolean isActive);
}
