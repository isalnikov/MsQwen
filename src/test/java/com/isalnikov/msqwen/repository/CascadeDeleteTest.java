package com.isalnikov.msqwen.repository;

import com.isalnikov.msqwen.entity.AnalysisResult;
import com.isalnikov.msqwen.entity.Channel;
import com.isalnikov.msqwen.entity.News;
import com.isalnikov.msqwen.entity.Prompt;
import com.isalnikov.msqwen.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Интеграционные тесты для проверки CASCADE удаления.
 *
 * <p>Проверяют что при удалении сущностей каскадно удаляются все связанные данные.</p>
 *
 * @author isalnikov
 * @version 1.0
 */
@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
@org.springframework.transaction.annotation.Transactional
class CascadeDeleteTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PromptRepository promptRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private NewsRepository newsRepository;

    @Autowired
    private AnalysisResultRepository analysisResultRepository;

    @Autowired
    private EntityManager entityManager;

    private User user;
    private Prompt prompt;
    private Channel channel;
    private News news;
    private AnalysisResult analysisResult;

    @BeforeEach
    void setUp() {
        // Очищаем данные в правильном порядке
        analysisResultRepository.deleteAll();
        newsRepository.deleteAll();
        channelRepository.deleteAll();
        promptRepository.deleteAll();
        userRepository.deleteAll();
        entityManager.flush();

        // Создаём пользователя
        user = new User(1001L, "testuser", "Test", "User");
        userRepository.saveAndFlush(user);

        // Создаём промпт
        prompt = new Prompt(user, "Test Prompt", "Desc", "Text");
        promptRepository.saveAndFlush(prompt);

        // Создаём канал
        channel = new Channel(prompt, user, "Test Channel", "@test", "https://t.me/test", "Desc");
        channelRepository.saveAndFlush(channel);

        // Создаём новость
        news = new News();
        news.setChannel(channel);
        news.setPrompt(prompt);
        news.setUser(user);
        news.setMessageId(1L);
        news.setTitle("Test News");
        news.setContent("Content");
        news.setPublicationDate(LocalDateTime.now());
        news.setViewsCount(100);
        news.setForwardsCount(10);
        news.setReactionsCount(20);
        news.setLikesCount(15);
        news.setIsAnalyzed(false);
        news.setCreatedAt(LocalDateTime.now());
        newsRepository.saveAndFlush(news);

        // Создаём результат анализа
        analysisResult = new AnalysisResult();
        analysisResult.setPrompt(prompt);
        analysisResult.setUser(user);
        analysisResult.setAnalysisText("Analysis");
        analysisResult.setCacheKey("cache123");
        analysisResult.setCacheExpiresAt(LocalDateTime.now().plusHours(1));
        analysisResult.setCreatedAt(LocalDateTime.now());
        analysisResultRepository.saveAndFlush(analysisResult);
    }

    @Test
    void deletingPrompt_shouldCascadeDeleteChannelsNewsAndAnalysis() {
        Long promptId = prompt.getId();
        Long channelId = channel.getId();
        Long newsId = news.getId();
        Long analysisId = analysisResult.getId();

        // Проверяем что всё создано
        assertTrue(promptRepository.existsById(promptId));
        assertTrue(channelRepository.existsById(channelId));
        assertTrue(newsRepository.existsById(newsId));
        assertTrue(analysisResultRepository.existsById(analysisId));

        // Удаляем промпт
        Prompt promptToDelete = promptRepository.findById(promptId).orElseThrow();
        promptRepository.delete(promptToDelete);
        entityManager.flush();

        // Проверяем что всё каскадно удалено
        assertFalse(promptRepository.existsById(promptId));
        assertFalse(channelRepository.existsById(channelId));
        assertFalse(newsRepository.existsById(newsId));
        assertFalse(analysisResultRepository.existsById(analysisId));
    }

    @Test
    void deletingChannel_shouldCascadeDeleteNews() {
        Long channelId = channel.getId();
        Long newsId = news.getId();

        // Проверяем что всё создано
        assertTrue(channelRepository.existsById(channelId));
        assertTrue(newsRepository.existsById(newsId));

        // Удаляем канал
        Channel channelToDelete = channelRepository.findById(channelId).orElseThrow();
        channelRepository.delete(channelToDelete);
        entityManager.flush();

        // Проверяем что канал и новость удалены
        assertFalse(channelRepository.existsById(channelId));
        assertFalse(newsRepository.existsById(newsId));

        // Промпт и результат анализа должны остаться
        assertTrue(promptRepository.existsById(prompt.getId()));
        assertTrue(analysisResultRepository.existsById(analysisResult.getId()));
    }

    @Test
    void deletingUser_shouldCascadeDeleteAllRelatedData() {
        Long userId = user.getId();
        Long promptId = prompt.getId();
        Long channelId = channel.getId();
        Long newsId = news.getId();
        Long analysisId = analysisResult.getId();

        // Проверяем что всё создано
        assertTrue(userRepository.existsById(userId));
        assertTrue(promptRepository.existsById(promptId));
        assertTrue(channelRepository.existsById(channelId));
        assertTrue(newsRepository.existsById(newsId));
        assertTrue(analysisResultRepository.existsById(analysisId));

        // Удаляем пользователя
        User userToDelete = userRepository.findById(userId).orElseThrow();
        userRepository.delete(userToDelete);
        entityManager.flush();

        // Проверяем что всё каскадно удалено
        assertFalse(userRepository.existsById(userId));
        assertFalse(promptRepository.existsById(promptId));
        assertFalse(channelRepository.existsById(channelId));
        assertFalse(newsRepository.existsById(newsId));
        assertFalse(analysisResultRepository.existsById(analysisId));
    }

    @Test
    void deletingAnalysisResult_shouldNotAffectOtherEntities() {
        Long analysisId = analysisResult.getId();
        Long newsId = news.getId();

        // Удаляем результат анализа
        AnalysisResult resultToDelete = analysisResultRepository.findById(analysisId).orElseThrow();
        analysisResultRepository.delete(resultToDelete);
        entityManager.flush();

        // Результат анализа должен быть удалён, но остальные данные должны остаться
        assertFalse(analysisResultRepository.existsById(analysisId));
        assertTrue(newsRepository.existsById(newsId));
        assertTrue(channelRepository.existsById(channel.getId()));
        assertTrue(promptRepository.existsById(prompt.getId()));
    }
}
