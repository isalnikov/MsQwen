package com.isalnikov.msqwen.repository;

import com.isalnikov.msqwen.entity.Channel;
import com.isalnikov.msqwen.entity.Prompt;
import com.isalnikov.msqwen.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Интеграционные тесты для проверки изоляции данных по user_id.
 *
 * <p>Проверяют что пользователи не могут видеть данные других пользователей.</p>
 *
 * @author isalnikov
 * @version 1.0
 */
@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
@org.springframework.transaction.annotation.Transactional
class DataIsolationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PromptRepository promptRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private NewsRepository newsRepository;

    @Autowired
    private EntityManager entityManager;

    private User user1;
    private User user2;
    private Prompt prompt1;
    private Prompt prompt2;

    @BeforeEach
    void setUp() {
        // Очищаем данные
        newsRepository.deleteAll();
        channelRepository.deleteAll();
        promptRepository.deleteAll();
        userRepository.deleteAll();

        // Создаём двух пользователей
        user1 = new User(1001L, "user1", "User", "One");
        user2 = new User(1002L, "user2", "User", "Two");
        userRepository.saveAndFlush(user1);
        userRepository.saveAndFlush(user2);

        // Создаём промпты для каждого
        prompt1 = new Prompt(user1, "Prompt 1", "Desc 1", "Text 1");
        prompt2 = new Prompt(user2, "Prompt 2", "Desc 2", "Text 2");
        promptRepository.saveAndFlush(prompt1);
        promptRepository.saveAndFlush(prompt2);

        // Создаём каналы для каждого промпта
        Channel channel1 = new Channel(prompt1, user1, "Channel 1", "@ch1", "https://t.me/ch1", "Desc");
        Channel channel2 = new Channel(prompt2, user2, "Channel 2", "@ch2", "https://t.me/ch2", "Desc");
        channelRepository.saveAndFlush(channel1);
        channelRepository.saveAndFlush(channel2);
    }

    @Test
    void userShouldNotSeeOtherUsersPrompts() {
        // Пользователь 1 должен видеть только свои промпты
        var user1Prompts = promptRepository.findAllByUserId(user1.getId());
        assertEquals(1, user1Prompts.size());
        assertEquals("Prompt 1", user1Prompts.get(0).getName());

        var user2Prompts = promptRepository.findAllByUserId(user2.getId());
        assertEquals(1, user2Prompts.size());
        assertEquals("Prompt 2", user2Prompts.get(0).getName());
    }

    @Test
    void userShouldNotSeeOtherUsersChannels() {
        // Пользователь 1 должен видеть только свои каналы
        var user1Channels = channelRepository.findAllByUserId(user1.getId());
        assertEquals(1, user1Channels.size());
        assertEquals("Channel 1", user1Channels.get(0).getName());

        var user2Channels = channelRepository.findAllByUserId(user2.getId());
        assertEquals(1, user2Channels.size());
        assertEquals("Channel 2", user2Channels.get(0).getName());
    }

    @Test
    void promptShouldOnlyReturnChannelsForOwner() {
        // Каналы должны возвращаться только для владельца промпта
        var channels1 = channelRepository.findAllByPromptIdAndUserId(prompt1.getId(), user1.getId());
        assertEquals(1, channels1.size());

        var channels2 = channelRepository.findAllByPromptIdAndUserId(prompt2.getId(), user2.getId());
        assertEquals(1, channels2.size());

        // Чужие каналы не должны возвращаться
        var wrongChannels = channelRepository.findAllByPromptIdAndUserId(prompt1.getId(), user2.getId());
        assertTrue(wrongChannels.isEmpty());
    }

    @Test
    void existsByIdAndUserId_shouldReturnFalseForOtherUser() {
        // Проверка существования промпта должна возвращать false для чужого пользователя
        assertTrue(promptRepository.existsByIdAndUserId(prompt1.getId(), user1.getId()));
        assertFalse(promptRepository.existsByIdAndUserId(prompt1.getId(), user2.getId()));

        assertTrue(promptRepository.existsByIdAndUserId(prompt2.getId(), user2.getId()));
        assertFalse(promptRepository.existsByIdAndUserId(prompt2.getId(), user1.getId()));
    }

    @Test
    void findByIdAndUserId_shouldReturnEmptyForOtherUser() {
        // Поиск промпта должен возвращать пустой результат для чужого пользователя
        assertTrue(promptRepository.findByIdAndUserId(prompt1.getId(), user1.getId()).isPresent());
        assertTrue(promptRepository.findByIdAndUserId(prompt1.getId(), user2.getId()).isEmpty());
    }
}
