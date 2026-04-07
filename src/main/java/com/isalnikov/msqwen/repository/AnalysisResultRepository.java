package com.isalnikov.msqwen.repository;

import com.isalnikov.msqwen.entity.AnalysisResult;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA репозиторий для работы с результатами анализа новостей.
 * 
 * <p>Обеспечивает доступ к таблице analysis_results в базе данных.
 * Все методы учитывают изоляцию данных по user_id - пользователь может работать
 * только со своими результатами анализа.
 * Поддерживает кеширование через cacheKey.</p>
 * 
 * @author isalnikov
 * @version 1.0
 */
@Repository
public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, Long> {

    /**
     * Находит результат анализа по ключу кеша.
     *
     * @param cacheKey уникальный ключ кеша
     * @return Optional с результатом анализа если найден
     */
    Optional<AnalysisResult> findByCacheKey(String cacheKey);

    /**
     * Находит все результаты анализа для промпта и пользователя.
     *
     * @param promptId идентификатор промпта
     * @param userId идентификатор пользователя
     * @return список результатов анализа
     */
    List<AnalysisResult> findAllByPromptIdAndUserId(Long promptId, Long userId);

    /**
     * Находит результаты анализа для промпта и пользователя с пагинацией.
     *
     * @param promptId идентификатор промпта
     * @param userId идентификатор пользователя
     * @param pageable пагинация
     * @return страница результатов
     */
    Page<AnalysisResult> findAllByPromptIdAndUserId(Long promptId, Long userId, Pageable pageable);

    /**
     * Находит результаты анализа для промпта и пользователя отсортированные по дате.
     *
     * @param promptId идентификатор промпта
     * @param userId идентификатор пользователя
     * @return список результатов от новых к старым
     */
    List<AnalysisResult> findAllByPromptIdAndUserIdOrderByCreatedAtDesc(Long promptId, Long userId);

    /**
     * Находит все результаты анализа пользователя с пагинацией.
     *
     * @param userId идентификатор пользователя
     * @param pageable пагинация
     * @return страница результатов
     */
    Page<AnalysisResult> findAllByUserId(Long userId, Pageable pageable);

    /**
     * Находит результаты анализа пользователя отсортированные по дате.
     *
     * @param userId идентификатор пользователя
     * @return список результатов от новых к старым
     */
    List<AnalysisResult> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Возвращает количество результатов анализа у промпта и пользователя.
     *
     * @param promptId идентификатор промпта
     * @param userId идентификатор пользователя
     * @return количество результатов
     */
    long countByPromptIdAndUserId(Long promptId, Long userId);

    /**
     * Возвращает общее количество результатов анализа у пользователя.
     *
     * @param userId идентификатор пользователя
     * @return количество результатов
     */
    long countByUserId(Long userId);

    /**
     * Удаляет все результаты анализа по идентификатору промпта и пользователя.
     *
     * @param promptId идентификатор промпта
     * @param userId идентификатор пользователя
     */
    void deleteAllByPromptIdAndUserId(Long promptId, Long userId);

    /**
     * Удаляет все результаты анализа пользователя.
     *
     * @param userId идентификатор пользователя
     */
    void deleteAllByUserId(Long userId);

    /**
     * Удаляет просроченные записи кеша.
     *
     * @param now текущая дата и время
     */
    @Modifying
    @Query("DELETE FROM AnalysisResult a WHERE a.cacheExpiresAt < :now")
    void deleteByCacheExpiresAtBefore(@Param("now") LocalDateTime now);
}
