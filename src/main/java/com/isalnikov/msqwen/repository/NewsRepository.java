package com.isalnikov.msqwen.repository;

import com.isalnikov.msqwen.entity.News;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA репозиторий для работы с новостями.
 * 
 * <p>Обеспечивает доступ к таблице news в базе данных.
 * Все методы учитывают изоляцию данных по user_id - пользователь может работать
 * только со своими новостями.</p>
 * 
 * @author isalnikov
 * @version 1.0
 */
@Repository
public interface NewsRepository extends JpaRepository<News, Long> {

    /**
     * Находит все не проанализированные новости для канала и пользователя.
     *
     * @param channelId идентификатор канала
     * @param userId идентификатор пользователя
     * @param isAnalyzed флаг анализа
     * @return список новостей
     */
    List<News> findAllByChannelIdAndUserIdAndIsAnalyzed(Long channelId, Long userId, Boolean isAnalyzed);

    /**
     * Находит все не проанализированные новости для промпта и пользователя.
     *
     * @param promptId идентификатор промпта
     * @param userId идентификатор пользователя
     * @param isAnalyzed флаг анализа
     * @return список новостей
     */
    List<News> findAllByPromptIdAndUserIdAndIsAnalyzed(Long promptId, Long userId, Boolean isAnalyzed);

    /**
     * Находит новости по промпту и пользователю с пагинацией.
     *
     * @param promptId идентификатор промпта
     * @param userId идентификатор пользователя
     * @param pageable пагинация
     * @return страница новостей
     */
    Page<News> findAllByPromptIdAndUserId(Long promptId, Long userId, Pageable pageable);

    /**
     * Находит все новости пользователя с пагинацией.
     *
     * @param userId идентификатор пользователя
     * @param pageable пагинация
     * @return страница новостей
     */
    Page<News> findAllByUserId(Long userId, Pageable pageable);

    /**
     * Находит новости старше указанной даты (для очистки старых новостей).
     *
     * @param cutoffDate дата cutoff
     * @return список старых новостей
     */
    List<News> findAllByPublicationDateBefore(LocalDateTime cutoffDate);

    /**
     * Возвращает количество новостей у промпта и пользователя.
     *
     * @param promptId идентификатор промпта
     * @param userId идентификатор пользователя
     * @return количество новостей
     */
    long countByPromptIdAndUserId(Long promptId, Long userId);

    /**
     * Возвращает общее количество новостей у пользователя.
     *
     * @param userId идентификатор пользователя
     * @return количество новостей
     */
    long countByUserId(Long userId);

    /**
     * Возвращает количество проанализированных/непроанализированных новостей у пользователя.
     *
     * @param userId идентификатор пользователя
     * @param isAnalyzed флаг анализа
     * @return количество новостей
     */
    long countByUserIdAndIsAnalyzed(Long userId, Boolean isAnalyzed);

    /**
     * Находит проанализированные новости для промпта и пользователя.
     *
     * @param promptId идентификатор промпта
     * @param userId идентификатор пользователя
     * @param isAnalyzed флаг анализа
     * @return список новостей
     */
    List<News> findAllByPromptIdAndUserIdAndIsAnalyzedOrderByPublicationDateDesc(
            Long promptId, Long userId, Boolean isAnalyzed);

    /**
     * Находит все новости для пользователя с сортировкой по дате.
     *
     * @param userId идентификатор пользователя
     * @param pageable пагинация
     * @return страница новостей
     */
    Page<News> findAllByUserIdOrderByPublicationDateDesc(Long userId, Pageable pageable);

    /**
     * Проверяет существует ли новость с указанным messageId в канале.
     *
     * @param channelId идентификатор канала
     * @param messageId идентификатор сообщения в Telegram
     * @return true если новость существует
     */
    boolean existsByChannelIdAndMessageId(Long channelId, Long messageId);

    /**
     * Удаляет все новости по идентификатору канала и пользователя.
     *
     * @param channelId идентификатор канала
     * @param userId идентификатор пользователя
     */
    void deleteAllByChannelIdAndUserId(Long channelId, Long userId);

    /**
     * Удаляет все новости по идентификатору промпта и пользователя.
     *
     * @param promptId идентификатор промпта
     * @param userId идентификатор пользователя
     */
    void deleteAllByPromptIdAndUserId(Long promptId, Long userId);

    /**
     * Удаляет все новости пользователя.
     *
     * @param userId идентификатор пользователя
     */
    void deleteAllByUserId(Long userId);
}
