package com.isalnikov.msqwen.repository;

import com.isalnikov.msqwen.entity.Channel;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA репозиторий для работы с каналами Telegram.
 * 
 * <p>Обеспечивает доступ к таблице channels в базе данных.
 * Все методы учитывают изоляцию данных по user_id - пользователь может работать
 * только со своими каналами.</p>
 * 
 * @author isalnikov
 * @version 1.0
 */
@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long> {

    /**
     * Находит все каналы принадлежащие указанному промпту.
     *
     * @param promptId идентификатор промпта
     * @return список каналов промпта
     */
    List<Channel> findAllByPromptId(Long promptId);

    /**
     * Находит все активные каналы принадлежащие указанному промпту и пользователю.
     *
     * @param promptId идентификатор промпта
     * @param userId идентификатор пользователя
     * @param isActive флаг активности
     * @return список активных каналов
     */
    List<Channel> findAllByPromptIdAndUserIdAndIsActive(Long promptId, Long userId, Boolean isActive);

    /**
     * Находит все каналы принадлежащие указанному промпту и пользователю.
     *
     * @param promptId идентификатор промпта
     * @param userId идентификатор пользователя
     * @return список каналов
     */
    List<Channel> findAllByPromptIdAndUserId(Long promptId, Long userId);

    /**
     * Находит канал по идентификатору промпту и пользователю (для проверки прав).
     *
     * @param id идентификатор канала
     * @param promptId идентификатор промпта
     * @param userId идентификатор пользователя
     * @return Optional с каналом если найден и принадлежит пользователю
     */
    Optional<Channel> findByIdAndPromptIdAndUserId(Long id, Long promptId, Long userId);

    /**
     * Проверяет существует ли канал с указанным идентификатором у пользователя.
     *
     * @param id идентификатор канала
     * @param userId идентификатор пользователя
     * @return true если канал существует и принадлежит пользователю
     */
    boolean existsByIdAndUserId(Long id, Long userId);

    /**
     * Возвращает количество каналов у промпта и пользователя.
     *
     * @param promptId идентификатор промпта
     * @param userId идентификатор пользователя
     * @return количество каналов
     */
    long countByPromptIdAndUserId(Long promptId, Long userId);

    /**
     * Удаляет канал по идентификатору промпту и пользователю (с проверкой прав).
     * Каскадно удаляются все новости канала.
     *
     * @param id идентификатор канала
     * @param promptId идентификатор промпта
     * @param userId идентификатор пользователя
     */
    void deleteByIdAndPromptIdAndUserId(Long id, Long promptId, Long userId);

    /**
     * Находит все каналы принадлежащие пользователю (для статистики).
     *
     * @param userId идентификатор пользователя
     * @return список каналов пользователя
     */
    List<Channel> findAllByUserId(Long userId);
}
