package com.isalnikov.msqwen.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * JPA Entity новости из Telegram канала.
 * 
 * <p>Новость содержит метаданные для анализа: просмотры пересылки реакции лайки
 * и рассчитанный индекс вовлечённости (engagement_score).
 * Каждая новость привязана к каналу промпту и пользователю.</p>
 * 
 * <p>Таблица: news</p>
 * 
 * @author isalnikov
 * @version 1.0
 */
@Entity
@Table(name = "news")
public class News {

    /**
     * Уникальный идентификатор новости (первичный ключ).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Канал из которого была получена новость.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    /**
     * Промпт к которому привязана новость.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prompt_id", nullable = false)
    private Prompt prompt;

    /**
     * Пользователь которому принадлежит новость.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * ID сообщения в Telegram канале.
     */
    @Column(name = "message_id")
    private Long messageId;

    /**
     * Заголовок новости.
     */
    @Column(name = "title")
    private String title;

    /**
     * Содержание новости (текст).
     */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /**
     * Дата публикации новости в канале.
     */
    @Column(name = "publication_date")
    private LocalDateTime publicationDate;

    /**
     * Количество просмотров новости.
     */
    @Column(name = "views_count", nullable = false)
    private Integer viewsCount = 0;

    /**
     * Количество пересылок новости.
     */
    @Column(name = "forwards_count", nullable = false)
    private Integer forwardsCount = 0;

    /**
     * Количество реакций на новости.
     */
    @Column(name = "reactions_count", nullable = false)
    private Integer reactionsCount = 0;

    /**
     * Количество лайков новости.
     */
    @Column(name = "likes_count", nullable = false)
    private Integer likesCount = 0;

    /**
     * Индекс вовлечённости новости.
     * Рассчитывается по формуле: (views * 0.1 + forwards * 0.3 + reactions * 0.6) / 100
     */
    @Column(name = "engagement_score", precision = 5, scale = 2)
    private BigDecimal engagementScore = BigDecimal.ZERO;

    /**
     * Прямая ссылка на новость в Telegram.
     */
    @Column(name = "news_url")
    private String newsUrl;

    /**
     * Флаг indicates была ли новость проанализирована.
     */
    @Column(name = "is_analyzed", nullable = false)
    private Boolean isAnalyzed = false;

    /**
     * Дата и время создания записи.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Дата и время последнего обновления.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Конструктор по умолчанию для JPA.
     */
    public News() {
    }

    // Getters and Setters

    /**
     * Возвращает уникальный идентификатор новости.
     *
     * @return идентификатор новости
     */
    public Long getId() {
        return id;
    }

    /**
     * Устанавливает уникальный идентификатор новости.
     *
     * @param id идентификатор новости
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Возвращает канал из которого была получена новость.
     *
     * @return канал
     */
    public Channel getChannel() {
        return channel;
    }

    /**
     * Устанавливает канал из которого была получена новость.
     *
     * @param channel канал
     */
    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    /**
     * Возвращает промпт к которому привязана новость.
     *
     * @return промпт
     */
    public Prompt getPrompt() {
        return prompt;
    }

    /**
     * Устанавливает промпт к которому привязана новость.
     *
     * @param prompt промпт
     */
    public void setPrompt(Prompt prompt) {
        this.prompt = prompt;
    }

    /**
     * Возвращает пользователя владельца новости.
     *
     * @return пользователь владелец
     */
    public User getUser() {
        return user;
    }

    /**
     * Устанавливает пользователя владельца новости.
     *
     * @param user пользователь владелец
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Возвращает ID сообщения в Telegram канале.
     *
     * @return ID сообщения
     */
    public Long getMessageId() {
        return messageId;
    }

    /**
     * Устанавливает ID сообщения в Telegram канале.
     *
     * @param messageId ID сообщения
     */
    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    /**
     * Возвращает заголовок новости.
     *
     * @return заголовок
     */
    public String getTitle() {
        return title;
    }

    /**
     * Устанавливает заголовок новости.
     *
     * @param title заголовок
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Возвращает содержание новости.
     *
     * @return содержание
     */
    public String getContent() {
        return content;
    }

    /**
     * Устанавливает содержание новости.
     *
     * @param content содержание
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Возвращает дату публикации новости.
     *
     * @return дата публикации
     */
    public LocalDateTime getPublicationDate() {
        return publicationDate;
    }

    /**
     * Устанавливает дату публикации новости.
     *
     * @param publicationDate дата публикации
     */
    public void setPublicationDate(LocalDateTime publicationDate) {
        this.publicationDate = publicationDate;
    }

    /**
     * Возвращает количество просмотров новости.
     *
     * @return количество просмотров
     */
    public Integer getViewsCount() {
        return viewsCount;
    }

    /**
     * Устанавливает количество просмотров новости.
     *
     * @param viewsCount количество просмотров
     */
    public void setViewsCount(Integer viewsCount) {
        this.viewsCount = viewsCount;
    }

    /**
     * Возвращает количество пересылок новости.
     *
     * @return количество пересылок
     */
    public Integer getForwardsCount() {
        return forwardsCount;
    }

    /**
     * Устанавливает количество пересылок новости.
     *
     * @param forwardsCount количество пересылок
     */
    public void setForwardsCount(Integer forwardsCount) {
        this.forwardsCount = forwardsCount;
    }

    /**
     * Возвращает количество реакций на новости.
     *
     * @return количество реакций
     */
    public Integer getReactionsCount() {
        return reactionsCount;
    }

    /**
     * Устанавливает количество реакций на новости.
     *
     * @param reactionsCount количество реакций
     */
    public void setReactionsCount(Integer reactionsCount) {
        this.reactionsCount = reactionsCount;
    }

    /**
     * Возвращает количество лайков новости.
     *
     * @return количество лайков
     */
    public Integer getLikesCount() {
        return likesCount;
    }

    /**
     * Устанавливает количество лайков новости.
     *
     * @param likesCount количество лайков
     */
    public void setLikesCount(Integer likesCount) {
        this.likesCount = likesCount;
    }

    /**
     * Возвращает индекс вовлечённости новости.
     *
     * @return индекс вовлечённости
     */
    public BigDecimal getEngagementScore() {
        return engagementScore;
    }

    /**
     * Устанавливает индекс вовлечённости новости.
     *
     * @param engagementScore индекс вовлечённости
     */
    public void setEngagementScore(BigDecimal engagementScore) {
        this.engagementScore = engagementScore;
    }

    /**
     * Возвращает прямую ссылку на новость в Telegram.
     *
     * @return URL новости
     */
    public String getNewsUrl() {
        return newsUrl;
    }

    /**
     * Устанавливает прямую ссылку на новость в Telegram.
     *
     * @param newsUrl URL новости
     */
    public void setNewsUrl(String newsUrl) {
        this.newsUrl = newsUrl;
    }

    /**
     * Возвращает флаг была ли новость проанализирована.
     *
     * @return true если новость проанализирован
     */
    public Boolean getIsAnalyzed() {
        return isAnalyzed;
    }

    /**
     * Устанавливает флаг была ли новость проанализирована.
     *
     * @param isAnalyzed флаг анализа
     */
    public void setIsAnalyzed(Boolean isAnalyzed) {
        this.isAnalyzed = isAnalyzed;
    }

    /**
     * Возвращает дату и время создания записи.
     *
     * @return дата создания
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Устанавливает дату и время создания записи.
     *
     * @param createdAt дата создания
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Возвращает дату и время последнего обновления.
     *
     * @return дата обновления
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Устанавливает дату и время последнего обновления.
     *
     * @param updatedAt дата обновления
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Сравнивает эту новость с другой по идентификатору.
     *
     * @param o объект для сравнения
     * @return true если объекты равны
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        News news = (News) o;
        return Objects.equals(id, news.id);
    }

    /**
     * Возвращает хеш-код объекта на основе идентификатора.
     *
     * @return хеш-код
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Возвращает строковое представление новости.
     *
     * @return строка с информацией о новости
     */
    @Override
    public String toString() {
        return "News{"
                + "id=" + id
                + ", title='" + title + '\''
                + ", viewsCount=" + viewsCount
                + ", forwardsCount=" + forwardsCount
                + ", reactionsCount=" + reactionsCount
                + ", engagementScore=" + engagementScore
                + ", isAnalyzed=" + isAnalyzed
                + ", publicationDate=" + publicationDate
                + '}';
    }
}
