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
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * JPA Entity результата анализа новостей через Qwen CLI.
 * 
 * <p>Хранит результат анализа новостей через Qwen Code CLI.
 * Каждый результат привязан к промпту и пользователю.
 * Поддерживает кеширование с TTL (cacheKey cacheExpiresAt).</p>
 * 
 * <p>Таблица: analysis_results</p>
 * 
 * @author isalnikov
 * @version 1.0
 */
@Entity
@Table(name = "analysis_results")
public class AnalysisResult {

    /**
     * Уникальный идентификатор результата анализа (первичный ключ).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Промпт для которого был выполнен анализ.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prompt_id", nullable = false)
    private Prompt prompt;

    /**
     * Пользователь которому принадлежит результат анализа.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Текст результата анализа от Qwen CLI.
     */
    @Column(name = "analysis_text", nullable = false, columnDefinition = "TEXT")
    private String analysisText;

    /**
     * ID новостей участвовавших в анализе (через запятую или JSON).
     */
    @Column(name = "news_ids", columnDefinition = "TEXT")
    private String newsIds;

    /**
     * Уникальный ключ кеша для кеширования результатов.
     */
    @Column(name = "cache_key", unique = true)
    private String cacheKey;

    /**
     * Дата и время истечения срока действия кеша.
     */
    @Column(name = "cache_expires_at")
    private LocalDateTime cacheExpiresAt;

    /**
     * Дата и время создания записи.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Конструктор по умолчанию для JPA.
     */
    public AnalysisResult() {
    }

    // Getters and Setters

    /**
     * Возвращает уникальный идентификатор результата анализа.
     *
     * @return идентификатор результата
     */
    public Long getId() {
        return id;
    }

    /**
     * Устанавливает уникальный идентификатор результата анализа.
     *
     * @param id идентификатор результата
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Возвращает промпт для которого был выполнен анализ.
     *
     * @return промпт
     */
    public Prompt getPrompt() {
        return prompt;
    }

    /**
     * Устанавливает промпт для которого был выполнен анализ.
     *
     * @param prompt промпт
     */
    public void setPrompt(Prompt prompt) {
        this.prompt = prompt;
    }

    /**
     * Возвращает пользователя владельца результата анализа.
     *
     * @return пользователь владелец
     */
    public User getUser() {
        return user;
    }

    /**
     * Устанавливает пользователя владельца результата анализа.
     *
     * @param user пользователь владелец
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Возвращает текст результата анализа от Qwen CLI.
     *
     * @return текст анализа
     */
    public String getAnalysisText() {
        return analysisText;
    }

    /**
     * Устанавливает текст результата анализа от Qwen CLI.
     *
     * @param analysisText текст анализа
     */
    public void setAnalysisText(String analysisText) {
        this.analysisText = analysisText;
    }

    /**
     * Возвращает ID новостей участвовавших в анализе.
     *
     * @return ID новостей
     */
    public String getNewsIds() {
        return newsIds;
    }

    /**
     * Устанавливает ID новостей участвовавших в анализе.
     *
     * @param newsIds ID новостей
     */
    public void setNewsIds(String newsIds) {
        this.newsIds = newsIds;
    }

    /**
     * Возвращает уникальный ключ кеша.
     *
     * @return ключ кеша
     */
    public String getCacheKey() {
        return cacheKey;
    }

    /**
     * Устанавливает уникальный ключ кеша.
     *
     * @param cacheKey ключ кеша
     */
    public void setCacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
    }

    /**
     * Возвращает дату и время истечения срока действия кеша.
     *
     * @return дата истечения кеша
     */
    public LocalDateTime getCacheExpiresAt() {
        return cacheExpiresAt;
    }

    /**
     * Устанавливает дату и время истечения срока действия кеша.
     *
     * @param cacheExpiresAt дата истечения кеша
     */
    public void setCacheExpiresAt(LocalDateTime cacheExpiresAt) {
        this.cacheExpiresAt = cacheExpiresAt;
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
     * Сравнивает этот результат анализа с другим по идентификатору.
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
        AnalysisResult that = (AnalysisResult) o;
        return Objects.equals(id, that.id);
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
     * Возвращает строковое представление результата анализа.
     *
     * @return строка с информацией о результате анализа
     */
    @Override
    public String toString() {
        return "AnalysisResult{"
                + "id=" + id
                + ", cacheKey='" + cacheKey + '\''
                + ", cacheExpiresAt=" + cacheExpiresAt
                + ", createdAt=" + createdAt
                + '}';
    }
}
