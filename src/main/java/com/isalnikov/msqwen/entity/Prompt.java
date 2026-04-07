package com.isalnikov.msqwen.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * JPA Entity промпта для анализа новостей.
 * 
 * <p>Промпт содержит шаблон текста для анализа новостей через Qwen CLI.
 * Каждый пользователь может создать несколько промптов.
 * При удалении промпта каскадно удаляются каналы новости и результаты анализа.</p>
 * 
 * <p>Таблица: prompts</p>
 * 
 * @author isalnikov
 * @version 1.0
 */
@Entity
@Table(name = "prompts")
public class Prompt {

    /**
     * Уникальный идентификатор промпта (первичный ключ).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Пользователь которому принадлежит промпт.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Название промпта.
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Описание промпта.
     */
    @Column(name = "description")
    private String description;

    /**
     * Текст промпта для анализа новостей.
     */
    @Column(name = "prompt_text", nullable = false, columnDefinition = "TEXT")
    private String promptText;

    /**
     * Флаг активности промпта.
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

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
     * Список каналов связанных с этим промптом.
     * Каскадное удаление: при удалении промпта удаляются каналы.
     */
    @OneToMany(mappedBy = "prompt", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Channel> channels = new ArrayList<>();

    /**
     * Список новостей связанных с этим промптом.
     * Каскадное удаление: при удалении промпта удаляются новости.
     */
    @OneToMany(mappedBy = "prompt", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<News> news = new ArrayList<>();

    /**
     * Список результатов анализа для этого промпта.
     * Каскадное удаление: при удалении промпта удаляются результаты.
     */
    @OneToMany(mappedBy = "prompt", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AnalysisResult> analysisResults = new ArrayList<>();

    /**
     * Конструктор по умолчанию для JPA.
     */
    public Prompt() {
    }

    /**
     * Конструктор с основными полями.
     *
     * @param user пользователь владелец промпта
     * @param name название промпта
     * @param description описание
     * @param promptText текст промпта
     */
    public Prompt(User user, String name, String description, String promptText) {
        this.user = user;
        this.name = name;
        this.description = description;
        this.promptText = promptText;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters

    /**
     * Возвращает уникальный идентификатор промпта.
     *
     * @return идентификатор промпта
     */
    public Long getId() {
        return id;
    }

    /**
     * Устанавливает уникальный идентификатор промпта.
     *
     * @param id идентификатор промпта
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Возвращает пользователя владельца промпта.
     *
     * @return пользователь владелец
     */
    public User getUser() {
        return user;
    }

    /**
     * Устанавливает пользователя владельца промпта.
     *
     * @param user пользователь владелец
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Возвращает название промпта.
     *
     * @return название промпта
     */
    public String getName() {
        return name;
    }

    /**
     * Устанавливает название промпта.
     *
     * @param name название промпта
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Возвращает описание промпта.
     *
     * @return описание
     */
    public String getDescription() {
        return description;
    }

    /**
     * Устанавливает описание промпта.
     *
     * @param description описание
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Возвращает текст промпта для анализа новостей.
     *
     * @return текст промпта
     */
    public String getPromptText() {
        return promptText;
    }

    /**
     * Устанавливает текст промпта для анализа новостей.
     *
     * @param promptText текст промпта
     */
    public void setPromptText(String promptText) {
        this.promptText = promptText;
    }

    /**
     * Возвращает флаг активности промпта.
     *
     * @return true если промпт активен
     */
    public Boolean getIsActive() {
        return isActive;
    }

    /**
     * Устанавливает флаг активности промпта.
     *
     * @param isActive флаг активности
     */
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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
     * Возвращает список каналов связанных с промптом.
     *
     * @return список каналов
     */
    public List<Channel> getChannels() {
        return channels;
    }

    /**
     * Устанавливает список каналов.
     *
     * @param channels список каналов
     */
    public void setChannels(List<Channel> channels) {
        this.channels = channels;
    }

    /**
     * Возвращает список новостей связанных с промптом.
     *
     * @return список новостей
     */
    public List<News> getNews() {
        return news;
    }

    /**
     * Устанавливает список новостей.
     *
     * @param news список новостей
     */
    public void setNews(List<News> news) {
        this.news = news;
    }

    /**
     * Возвращает список результатов анализа для промпта.
     *
     * @return список результатов анализа
     */
    public List<AnalysisResult> getAnalysisResults() {
        return analysisResults;
    }

    /**
     * Устанавливает список результатов анализа.
     *
     * @param analysisResults список результатов анализа
     */
    public void setAnalysisResults(List<AnalysisResult> analysisResults) {
        this.analysisResults = analysisResults;
    }

    /**
     * Сравнивает этот промпт с другим по идентификатору.
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
        Prompt prompt = (Prompt) o;
        return Objects.equals(id, prompt.id);
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
     * Возвращает строковое представление промпта.
     *
     * @return строка с информацией о промпте
     */
    @Override
    public String toString() {
        return "Prompt{"
                + "id=" + id
                + ", name='" + name + '\''
                + ", description='" + description + '\''
                + ", isActive=" + isActive
                + ", createdAt=" + createdAt
                + '}';
    }
}
