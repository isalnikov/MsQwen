package com.isalnikov.msqwen.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.isalnikov.msqwen.entity.Channel;
import com.isalnikov.msqwen.entity.News;
import com.isalnikov.msqwen.entity.Prompt;
import com.isalnikov.msqwen.entity.User;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * Тесты для NewsRepository.
 * 
 * <p>Проверяют методы поиска удаления очистки старых новостей и изоляцию данных.</p>
 * 
 * @author isalnikov
 * @version 1.0
 */
@DataJpaTest
@DisplayName("Тесты NewsRepository")
class NewsRepositoryTest {

    @Autowired
    private NewsRepository newsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PromptRepository promptRepository;

    @Autowired
    private ChannelRepository channelRepository;

    private User testUser;
    private Prompt testPrompt;
    private Channel testChannel;

    /**
     * Подготавливает тестовые данные перед каждым тестом.
     */
    @BeforeEach
    void setUp() {
        testUser = new User(12345L, "testuser", "Test", "User");
        userRepository.save(testUser);

        testPrompt = new Prompt(testUser, "Test Prompt", "Description", "Prompt text...");
        promptRepository.save(testPrompt);

        testChannel = new Channel(testPrompt, testUser, "Test Channel", "test_channel",
                "https://t.me/test_channel", "Test description");
        channelRepository.save(testChannel);

        News news = createNews(1L, "Test News", "Content", LocalDateTime.now(), 100, 10, 5, 20);
        newsRepository.save(news);
    }

    private News createNews(Long messageId, String title, String content, LocalDateTime pubDate,
                           int views, int forwards, int reactions, int likes) {
        News news = new News();
        news.setChannel(testChannel);
        news.setPrompt(testPrompt);
        news.setUser(testUser);
        news.setMessageId(messageId);
        news.setTitle(title);
        news.setContent(content);
        news.setPublicationDate(pubDate);
        news.setViewsCount(views);
        news.setForwardsCount(forwards);
        news.setReactionsCount(reactions);
        news.setLikesCount(likes);
        news.setEngagementScore(BigDecimal.valueOf((views * 0.1 + forwards * 0.3 + reactions * 0.6) / 100));
        news.setNewsUrl("https://t.me/test_channel/" + messageId);
        news.setIsAnalyzed(false);
        news.setCreatedAt(LocalDateTime.now());
        return news;
    }

    @Test
    @DisplayName("Находит не проанализированные новости для промпта и пользователя")
    void findAllByPromptIdAndUserIdAndIsAnalyzed_shouldReturnNews() {
        // Act
        List<News> news = newsRepository.findAllByPromptIdAndUserIdAndIsAnalyzed(
                testPrompt.getId(), testUser.getId(), false);

        // Assert
        assertThat(news).hasSize(1);
        assertThat(news.get(0).getTitle()).isEqualTo("Test News");
    }

    @Test
    @DisplayName("Находит новости пользователя с пагинацией")
    void findAllByUserId_shouldReturnPagedNews() {
        // Arrange
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("publicationDate").descending());

        // Act
        Page<News> newsPage = newsRepository.findAllByUserId(testUser.getId(), pageRequest);

        // Assert
        assertThat(newsPage.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Находит новости старше указанной даты")
    void findAllByPublicationDateBefore_shouldReturnOldNews() {
        // Arrange
        LocalDateTime cutoffDate = LocalDateTime.now().plusDays(1);

        // Act
        List<News> oldNews = newsRepository.findAllByPublicationDateBefore(cutoffDate);

        // Assert
        assertThat(oldNews).hasSize(1);
    }

    @Test
    @DisplayName("Возвращает количество новостей пользователя")
    void countByUserId_shouldReturnCount() {
        // Act
        long count = newsRepository.countByUserId(testUser.getId());

        // Assert
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("Возвращает количество не проанализированных новостей")
    void countByUserIdAndIsAnalyzed_shouldReturnCount() {
        // Act
        long count = newsRepository.countByUserIdAndIsAnalyzed(testUser.getId(), false);

        // Assert
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("Удаляет все новости пользователя")
    void deleteAllByUserId_shouldDeleteAllNews() {
        // Act
        newsRepository.deleteAllByUserId(testUser.getId());

        // Assert
        assertThat(newsRepository.count()).isEqualTo(0);
    }
}
