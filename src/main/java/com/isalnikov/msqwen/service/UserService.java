package com.isalnikov.msqwen.service;

import com.isalnikov.msqwen.dto.UserDTO;
import com.isalnikov.msqwen.entity.User;
import com.isalnikov.msqwen.exception.ResourceNotFoundException;
import com.isalnikov.msqwen.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис для управления пользователями.
 * 
 * <p>Обеспечивает регистрацию получение обновление и удаление пользователей.
 * Все операции транзакционны и логируются.</p>
 * 
 * @author isalnikov
 * @version 1.0
 */
@Service
@Transactional(readOnly = true)
public class UserService {

    /**
     * Логгер для записи событий сервиса.
     */
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    /**
     * Репозиторий для доступа к данным пользователей.
     */
    private final UserRepository userRepository;

    /**
     * Конструктор с внедрением зависимостей через конструктор.
     *
     * @param userRepository репозиторий пользователей
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Регистрирует нового пользователя.
     * Если пользователь с таким telegramId уже существует - возвращает его.
     *
     * @param telegramId Telegram ID пользователя
     * @param username имя пользователя
     * @param firstName имя
     * @param lastName фамилия
     * @return DTO зарегистрированного пользователя
     */
    @Transactional
    public UserDTO registerUser(Long telegramId, String username, String firstName, String lastName) {
        logger.info("Регистрация пользователя: telegramId={}, username={}", telegramId, username);
        
        // Проверяем существует ли пользователь
        var existingUser = userRepository.findByTelegramId(telegramId);
        if (existingUser.isPresent()) {
            logger.info("Пользователь уже существует: telegramId={}", telegramId);
            return new UserDTO(existingUser.get());
        }
        
        // Создаём нового пользователя
        User user = new User(telegramId, username, firstName, lastName);
        user.setCreatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);
        
        logger.info("Пользователь успешно зарегистрирован: id={}", savedUser.getId());
        return new UserDTO(savedUser);
    }

    /**
     * Находит пользователя по Telegram ID.
     *
     * @param telegramId Telegram ID
     * @return DTO пользователя
     * @throws ResourceNotFoundException если пользователь не найден
     */
    public UserDTO getUserByTelegramId(Long telegramId) {
        logger.debug("Поиск пользователя по telegramId={}", telegramId);
        
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> {
                    logger.warn("Пользователь не найден: telegramId={}", telegramId);
                    return new ResourceNotFoundException("Пользователь не найден");
                });
        
        return new UserDTO(user);
    }

    /**
     * Обновляет данные пользователя.
     *
     * @param telegramId Telegram ID пользователя
     * @param username новое имя пользователя
     * @param firstName новое имя
     * @param lastName новая фамилия
     * @return DTO обновлённого пользователя
     * @throws ResourceNotFoundException если пользователь не найден
     */
    @Transactional
    public UserDTO updateUser(Long telegramId, String username, String firstName, String lastName) {
        logger.info("Обновление пользователя: telegramId={}", telegramId);
        
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> {
                    logger.warn("Пользователь не найден для обновления: telegramId={}", telegramId);
                    return new ResourceNotFoundException("Пользователь не найден");
                });
        
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUpdatedAt(LocalDateTime.now());
        
        User updatedUser = userRepository.save(user);
        logger.info("Пользователь успешно обновлён: id={}", updatedUser.getId());
        
        return new UserDTO(updatedUser);
    }

    /**
     * Удаляет пользователя по Telegram ID.
     * Каскадно удаляются все связанные данные (промпты каналы новости анализы).
     *
     * @param telegramId Telegram ID пользователя
     * @throws ResourceNotFoundException если пользователь не найден
     */
    @Transactional
    public void deleteUser(Long telegramId) {
        logger.info("Удаление пользователя: telegramId={}", telegramId);
        
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> {
                    logger.warn("Пользователь не найден для удаления: telegramId={}", telegramId);
                    return new ResourceNotFoundException("Пользователь не найден");
                });
        
        userRepository.delete(user);
        logger.info("Пользователь успешно удалён: telegramId={}", telegramId);
    }

    /**
     * Возвращает всех активных пользователей.
     *
     * @param isActive флаг активности
     * @return список DTO пользователей
     */
    public List<UserDTO> getActiveUsers(Boolean isActive) {
        logger.debug("Получение списка пользователей: isActive={}", isActive);
        
        return userRepository.findAllByIsActive(isActive)
                .stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Возвращает количество активных пользователей.
     *
     * @param isActive флаг активности
     * @return количество пользователей
     */
    public long getActiveUsersCount(Boolean isActive) {
        return userRepository.countByIsActive(isActive);
    }
}
