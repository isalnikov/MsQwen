package com.isalnikov.msqwen.service;

import com.isalnikov.msqwen.dto.ChannelDTO;
import com.isalnikov.msqwen.entity.Channel;
import com.isalnikov.msqwen.entity.Prompt;
import com.isalnikov.msqwen.entity.User;
import com.isalnikov.msqwen.exception.AccessDeniedException;
import com.isalnikov.msqwen.exception.ResourceNotFoundException;
import com.isalnikov.msqwen.repository.ChannelRepository;
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
 * Сервис для управления каналами Telegram.
 * 
 * <p>Обеспечивает создание получение обновление и удаление каналов.
 * При удалении канала каскадно удаляются все новости.
 * Все операции изолированы по user_id.</p>
 * 
 * @author isalnikov
 * @version 1.0
 */
@Service
@Transactional(readOnly = true)
public class ChannelService {

    /**
     * Логгер для записи событий сервиса.
     */
    private static final Logger logger = LoggerFactory.getLogger(ChannelService.class);

    /**
     * Репозиторий для доступа к данным каналов.
     */
    private final ChannelRepository channelRepository;

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
     * @param channelRepository репозиторий каналов
     * @param promptRepository репозиторий промптов
     * @param userRepository репозиторий пользователей
     */
    public ChannelService(ChannelRepository channelRepository,
                          PromptRepository promptRepository,
                          UserRepository userRepository) {
        this.channelRepository = channelRepository;
        this.promptRepository = promptRepository;
        this.userRepository = userRepository;
    }

    /**
     * Создаёт новый канал для промпта.
     *
     * @param promptId идентификатор промпта
     * @param userId идентификатор пользователя
     * @param name название канала
     * @param telegramHandle handle канала
     * @param telegramUrl URL канала
     * @param description описание канала
     * @return DTO созданного канала
     * @throws ResourceNotFoundException если пользователь или промпт не найдены
     * @throws AccessDeniedException если промпт не принадлежит пользователю
     */
    @Transactional
    public ChannelDTO createChannel(Long promptId, Long userId, String name,
                                    String telegramHandle, String telegramUrl, String description) {
        logger.info("Создание канала: promptId={}, userId={}, handle={}", promptId, userId, telegramHandle);
        
        // Проверяем что промпт существует и принадлежит пользователю
        Prompt prompt = promptRepository.findByIdAndUserId(promptId, userId)
                .orElseThrow(() -> {
                    if (promptRepository.existsById(promptId)) {
                        return new AccessDeniedException("Доступ к промпту запрещён");
                    }
                    return new ResourceNotFoundException("Промпт не найден");
                });
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
        
        Channel channel = new Channel(prompt, user, name, telegramHandle, telegramUrl, description);
        channel.setCreatedAt(LocalDateTime.now());
        Channel savedChannel = channelRepository.save(channel);
        
        logger.info("Канал успешно создан: id={}", savedChannel.getId());
        return new ChannelDTO(savedChannel);
    }

    /**
     * Возвращает все каналы промпта с проверкой прав.
     *
     * @param promptId идентификатор промпта
     * @param userId идентификатор пользователя
     * @return список DTO каналов
     * @throws AccessDeniedException если промпт не принадлежит пользователю
     */
    public List<ChannelDTO> getAllChannelsByPrompt(Long promptId, Long userId) {
        logger.debug("Получение каналов промпта: promptId={}, userId={}", promptId, userId);
        
        // Проверяем права доступа к промпту
        if (!promptRepository.existsByIdAndUserId(promptId, userId)) {
            if (promptRepository.existsById(promptId)) {
                throw new AccessDeniedException("Доступ к промпту запрещён");
            }
            throw new ResourceNotFoundException("Промпт не найден");
        }
        
        return channelRepository.findAllByPromptIdAndUserId(promptId, userId)
                .stream()
                .map(ChannelDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Возвращает канал по идентификатору с проверкой прав.
     *
     * @param channelId идентификатор канала
     * @param userId идентификатор пользователя
     * @return DTO канала
     * @throws ResourceNotFoundException если канал не найден
     * @throws AccessDeniedException если канал не принадлежит пользователю
     */
    public ChannelDTO getChannelByIdAndUser(Long channelId, Long userId) {
        logger.debug("Получение канала: channelId={}, userId={}", channelId, userId);
        
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> {
                    logger.warn("Канал не найден: channelId={}", channelId);
                    return new ResourceNotFoundException("Канал не найден");
                });
        
        // Проверяем что канал принадлежит пользователю
        if (!channel.getUser().getId().equals(userId)) {
            logger.warn("Доступ запрещён: channelId={}, userId={}", channelId, userId);
            throw new AccessDeniedException("Доступ к каналу запрещён");
        }
        
        return new ChannelDTO(channel);
    }

    /**
     * Обновляет канал с проверкой прав.
     *
     * @param channelId идентификатор канала
     * @param userId идентификатор пользователя
     * @param name новое название
     * @param description новое описание
     * @return DTO обновлённого канала
     * @throws ResourceNotFoundException если канал не найден
     * @throws AccessDeniedException если канал не принадлежит пользователю
     */
    @Transactional
    public ChannelDTO updateChannel(Long channelId, Long userId, String name, String description) {
        logger.info("Обновление канала: channelId={}, userId={}", channelId, userId);
        
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> {
                    logger.warn("Канал не найден для обновления: channelId={}", channelId);
                    return new ResourceNotFoundException("Канал не найден");
                });
        
        if (!channel.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Доступ к каналу запрещён");
        }
        
        channel.setName(name);
        channel.setDescription(description);
        
        Channel updatedChannel = channelRepository.save(channel);
        logger.info("Канал успешно обновлён: id={}", updatedChannel.getId());
        
        return new ChannelDTO(updatedChannel);
    }

    /**
     * Удаляет канал с проверкой прав.
     * Каскадно удаляются все новости канала.
     *
     * @param channelId идентификатор канала
     * @param userId идентификатор пользователя
     * @throws ResourceNotFoundException если канал не найден
     * @throws AccessDeniedException если канал не принадлежит пользователю
     */
    @Transactional
    public void deleteChannel(Long channelId, Long userId) {
        logger.info("Удаление канала: channelId={}, userId={}", channelId, userId);
        
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> {
                    logger.warn("Канал не найден для удаления: channelId={}", channelId);
                    return new ResourceNotFoundException("Канал не найден");
                });
        
        if (!channel.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Доступ к каналу запрещён");
        }
        
        channelRepository.delete(channel);
        logger.info("Канал успешно удалён: id={}", channelId);
    }

    /**
     * Возвращает все активные каналы промпта.
     *
     * @param promptId идентификатор промпта
     * @param userId идентификатор пользователя
     * @return список DTO активных каналов
     */
    public List<ChannelDTO> getActiveChannels(Long promptId, Long userId) {
        logger.debug("Получение активных каналов: promptId={}, userId={}", promptId, userId);
        
        if (!promptRepository.existsByIdAndUserId(promptId, userId)) {
            if (promptRepository.existsById(promptId)) {
                throw new AccessDeniedException("Доступ к промпту запрещён");
            }
            throw new ResourceNotFoundException("Промпт не найден");
        }
        
        return channelRepository.findAllByPromptIdAndUserIdAndIsActive(promptId, userId, true)
                .stream()
                .map(ChannelDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Возвращает количество каналов промпта.
     *
     * @param promptId идентификатор промпта
     * @param userId идентификатор пользователя
     * @return количество каналов
     */
    public long countByPrompt(Long promptId, Long userId) {
        return channelRepository.countByPromptIdAndUserId(promptId, userId);
    }

    /**
     * Возвращает все каналы пользователя (для статистики и бота).
     *
     * @param userId идентификатор пользователя
     * @return список DTO всех каналов
     */
    public List<ChannelDTO> getAllUserChannels(Long userId) {
        logger.debug("Получение всех каналов пользователя: userId={}", userId);

        return channelRepository.findAllByUserId(userId)
                .stream()
                .map(ChannelDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Возвращает все каналы промпта (для контроллеров).
     *
     * @param promptId идентификатор промпта
     * @param userId идентификатор пользователя
     * @return список DTO каналов
     */
    public List<ChannelDTO> getChannelsByPrompt(Long promptId, Long userId) {
        logger.debug("Получение каналов промпта: promptId={}, userId={}", promptId, userId);

        if (!promptRepository.existsByIdAndUserId(promptId, userId)) {
            if (promptRepository.existsById(promptId)) {
                throw new AccessDeniedException("Доступ к промпту запрещён");
            }
            throw new ResourceNotFoundException("Промпт не найден");
        }

        return channelRepository.findAllByPromptIdAndUserId(promptId, userId)
                .stream()
                .map(ChannelDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Возвращает канал по ID с проверкой прав.
     *
     * @param channelId идентификатор канала
     * @param userId идентификатор пользователя
     * @return DTO канала
     */
    public ChannelDTO getChannel(Long channelId, Long userId) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> {
                    logger.warn("Канал не найден: channelId={}", channelId);
                    return new ResourceNotFoundException("Канал не найден");
                });

        if (!channel.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Доступ к каналу запрещён");
        }

        return new ChannelDTO(channel);
    }

    /**
     * Обновляет канал с проверкой прав.
     *
     * @param channelId идентификатор канала
     * @param userId идентификатор пользователя
     * @param name новое название
     * @param telegramHandle новый handle
     * @param telegramUrl новый URL
     * @param description новое описание
     * @return DTO обновлённого канала
     */
    @Transactional
    public ChannelDTO updateChannel(Long channelId, Long userId, String name,
                                    String telegramHandle, String telegramUrl, String description) {
        logger.info("Обновление канала: channelId={}, userId={}", channelId, userId);

        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> {
                    logger.warn("Канал не найден для обновления: channelId={}", channelId);
                    return new ResourceNotFoundException("Канал не найден");
                });

        if (!channel.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Доступ к каналу запрещён");
        }

        channel.setName(name);
        channel.setTelegramHandle(telegramHandle);
        channel.setTelegramUrl(telegramUrl);
        channel.setDescription(description);

        Channel updatedChannel = channelRepository.save(channel);
        logger.info("Канал успешно обновлён: id={}", updatedChannel.getId());

        return new ChannelDTO(updatedChannel);
    }
}
