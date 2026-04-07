package com.isalnikov.msqwen.service;

import com.isalnikov.msqwen.dto.PromptDTO;
import com.isalnikov.msqwen.entity.Prompt;
import com.isalnikov.msqwen.entity.User;
import com.isalnikov.msqwen.exception.AccessDeniedException;
import com.isalnikov.msqwen.exception.ResourceNotFoundException;
import com.isalnikov.msqwen.repository.PromptRepository;
import com.isalnikov.msqwen.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис для управления промптами.
 * 
 * <p>Обеспечивает создание получение обновление и удаление промптов.
 * При удалении промпта каскадно удаляются каналы новости и результаты анализа.
 * Все операции изолированы по user_id - пользователь работает только со своими данными.</p>
 * 
 * @author isalnikov
 * @version 1.0
 */
@Service
@Transactional(readOnly = true)
public class PromptService {

    /**
     * Логгер для записи событий сервиса.
     */
    private static final Logger logger = LoggerFactory.getLogger(PromptService.class);

    /**
     * Репозиторий для доступа к данным промптов.
     */
    private final PromptRepository promptRepository;

    /**
     * Репозиторий для доступа к данным пользователей.
     */
    private final UserRepository userRepository;

    /**
     * Конструктор с внедрением зависимостей через конструктор.
     *
     * @param promptRepository репозиторий промптов
     * @param userRepository репозиторий пользователей
     */
    public PromptService(PromptRepository promptRepository, UserRepository userRepository) {
        this.promptRepository = promptRepository;
        this.userRepository = userRepository;
    }

    /**
     * Создаёт новый промпт для пользователя.
     *
     * @param userId идентификатор пользователя
     * @param name название промпта
     * @param description описание
     * @param promptText текст промпта
     * @return DTO созданного промпта
     * @throws ResourceNotFoundException если пользователь не найден
     */
    @Transactional
    public PromptDTO createPrompt(Long userId, String name, String description, String promptText) {
        logger.info("Создание промпта для пользователя: userId={}, name={}", userId, name);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("Пользователь не найден для создания промпта: userId={}", userId);
                    return new ResourceNotFoundException("Пользователь не найден");
                });
        
        Prompt prompt = new Prompt(user, name, description, promptText);
        prompt.setCreatedAt(LocalDateTime.now());
        Prompt savedPrompt = promptRepository.save(prompt);
        
        logger.info("Промпт успешно создан: id={}", savedPrompt.getId());
        return new PromptDTO(savedPrompt);
    }

    /**
     * Возвращает все промпты пользователя.
     *
     * @param userId идентификатор пользователя
     * @return список DTO промптов
     */
    public List<PromptDTO> getAllPromptsByUser(Long userId) {
        logger.debug("Получение всех промптов пользователя: userId={}", userId);
        
        return promptRepository.findAllByUserId(userId)
                .stream()
                .map(PromptDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Возвращает промпт по идентификатору с проверкой прав.
     *
     * @param promptId идентификатор промпта
     * @param userId идентификатор пользователя
     * @return DTO промпта
     * @throws ResourceNotFoundException если промпт не найден
     * @throws AccessDeniedException если промпт не принадлежит пользователю
     */
    public PromptDTO getPromptByIdAndUser(Long promptId, Long userId) {
        logger.debug("Получение промпта: promptId={}, userId={}", promptId, userId);
        
        Prompt prompt = promptRepository.findByIdAndUserId(promptId, userId)
                .orElseThrow(() -> {
                    // Проверяем существует ли промпт вообще
                    if (promptRepository.existsById(promptId)) {
                        logger.warn("Доступ запрещён: promptId={}, userId={}", promptId, userId);
                        return new AccessDeniedException("Доступ к промпту запрещён");
                    }
                    logger.warn("Промпт не найден: promptId={}", promptId);
                    return new ResourceNotFoundException("Промпт не найден");
                });
        
        return new PromptDTO(prompt);
    }

    /**
     * Обновляет промпт с проверкой прав.
     *
     * @param promptId идентификатор промпта
     * @param userId идентификатор пользователя
     * @param name новое название
     * @param description новое описание
     * @param promptText новый текст промпта
     * @return DTO обновлённого промпта
     * @throws ResourceNotFoundException если промпт не найден
     * @throws AccessDeniedException если промпт не принадлежит пользователю
     */
    @Transactional
    public PromptDTO updatePrompt(Long promptId, Long userId, String name, String description, String promptText) {
        logger.info("Обновление промпта: promptId={}, userId={}", promptId, userId);
        
        Prompt prompt = promptRepository.findByIdAndUserId(promptId, userId)
                .orElseThrow(() -> {
                    if (promptRepository.existsById(promptId)) {
                        return new AccessDeniedException("Доступ к промпту запрещён");
                    }
                    return new ResourceNotFoundException("Промпт не найден");
                });
        
        prompt.setName(name);
        prompt.setDescription(description);
        prompt.setPromptText(promptText);
        prompt.setUpdatedAt(LocalDateTime.now());
        
        Prompt updatedPrompt = promptRepository.save(prompt);
        logger.info("Промпт успешно обновлён: id={}", updatedPrompt.getId());
        
        return new PromptDTO(updatedPrompt);
    }

    /**
     * Удаляет промпт с проверкой прав.
     * Каскадно удаляются каналы новости и результаты анализа.
     *
     * @param promptId идентификатор промпта
     * @param userId идентификатор пользователя
     * @throws ResourceNotFoundException если промпт не найден
     * @throws AccessDeniedException если промпт не принадлежит пользователю
     */
    @Transactional
    public void deletePrompt(Long promptId, Long userId) {
        logger.info("Удаление промпта: promptId={}, userId={}", promptId, userId);
        
        Prompt prompt = promptRepository.findByIdAndUserId(promptId, userId)
                .orElseThrow(() -> {
                    if (promptRepository.existsById(promptId)) {
                        return new AccessDeniedException("Доступ к промпту запрещён");
                    }
                    return new ResourceNotFoundException("Промпт не найден");
                });
        
        promptRepository.delete(prompt);
        logger.info("Промпт успешно удалён: id={}", promptId);
    }

    /**
     * Возвращает количество промптов пользователя.
     *
     * @param userId идентификатор пользователя
     * @return количество промптов
     */
    public long countByUser(Long userId) {
        return promptRepository.countByUserId(userId);
    }

    /**
     * Возвращает все активные промпты пользователя.
     *
     * @param userId идентификатор пользователя
     * @return список DTO активных промптов
     */
    public List<PromptDTO> getActivePrompts(Long userId) {
        logger.debug("Получение активных промптов пользователя: userId={}", userId);
        
        return promptRepository.findAllByUserIdAndIsActive(userId, true)
                .stream()
                .map(PromptDTO::new)
                .collect(Collectors.toList());
    }
}
