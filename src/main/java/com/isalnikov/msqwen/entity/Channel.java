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
 * JPA Entity канала Telegram для парсинга новостей.
 * 
 * <p>Канал представляет источник новостей из Telegram.
 * Каждый канал принадлежит промпту и пользователю.
 * При удалении канала каскадно удаляются все новости.</p>
 * 
 * <p>Таблица: channels</p>
 * 
 * @author isalnikov
 * @version 1.0
 */
@Entity
@Table(name = "channels")
public class Channel {

    /**
     * Уникальный идентификатор канала (первичный ключ).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Промпт к которому привязан канал.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prompt_id", nullable = false)
    private Prompt prompt;

    /**
     * Пользователь которому принадлежит канал.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Название канала.
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Telegram handle канала (например @polit_news).
     */
    @Column(name = "telegram_handle", nullable = false)
    private String telegramHandle;

    /**
     * URL канала в Telegram.
     */
    @Column(name = "telegram_url")
    private String telegramUrl;

    /**
     * Описание канала.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Флаг активности канала.
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Дата и время создания записи.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Дата и время последнего парсинга канала.
     */
    @Column(name = "last_parsed_at")
    private LocalDateTime lastParsedAt;

    /**
     * Список новостей из этого канала.
     * Каскадное удаление: при удалении канала удаляются новости.
     */
    @OneToMany(mappedBy = "channel", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<News> news = new ArrayList<>();

    /**
     * Конструктор по умолчанию для JPA.
     */
    public Channel() {
    }

    /**
     * Конструктор с основными полями.
     *
     * @param prompt промпт владелец канала
     * @param user пользователь владелец канала
     * @param name название канала
     * @param telegramHandle Telegram handle канала
     * @param telegramUrl URL канала
     * @param description описание канала
     */
    public Channel(Prompt prompt, User user, String name, String telegramHandle,
                   String telegramUrl, String description) {
        this.prompt = prompt;
        this.user = user;
        this.name = name;
        this.telegramHandle = telegramHandle;
        this.telegramUrl = telegramUrl;
        this.description = description;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters

    /**
     * Возвращает уникальный идентификатор канала.
     *
     * @return идентификатор канала
     */
    public Long getId() {
        return id;
    }

    /**
     * Устанавливает уникальный идентификатор канала.
     *
     * @param id идентификатор канала
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Возвращает промпт владельца канала.
     *
     * @return промпт владелец
     */
    public Prompt getPrompt() {
        return prompt;
    }

    /**
     * Устанавливает промпт владельца канала.
     *
     * @param prompt промпт владелец
     */
    public void setPrompt(Prompt prompt) {
        this.prompt = prompt;
    }

    /**
     * Возвращает пользователя владельца канала.
     *
     * @return пользователь владелец
     */
    public User getUser() {
        return user;
    }

    /**
     * Устанавливает пользователя владельца канала.
     *
     * @param user пользователь владелец
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Возвращает название канала.
     *
     * @return название канала
     */
    public String getName() {
        return name;
    }

    /**
     * Устанавливает название канала.
     *
     * @param name название канала
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Возвращает Telegram handle канала.
     *
     * @return Telegram handle
     */
    public String getTelegramHandle() {
        return telegramHandle;
    }

    /**
     * Устанавливает Telegram handle канала.
     *
     * @param telegramHandle Telegram handle
     */
    public void setTelegramHandle(String telegramHandle) {
        this.telegramHandle = telegramHandle;
    }

    /**
     * Возвращает URL канала в Telegram.
     *
     * @return URL канала
     */
    public String getTelegramUrl() {
        return telegramUrl;
    }

    /**
     * Устанавливает URL канала в Telegram.
     *
     * @param telegramUrl URL канала
     */
    public void setTelegramUrl(String telegramUrl) {
        this.telegramUrl = telegramUrl;
    }

    /**
     * Возвращает описание канала.
     *
     * @return описание канала
     */
    public String getDescription() {
        return description;
    }

    /**
     * Устанавливает описание канала.
     *
     * @param description описание канала
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Возвращает флаг активности канала.
     *
     * @return true если канал активен
     */
    public Boolean getIsActive() {
        return isActive;
    }

    /**
     * Устанавливает флаг активности канала.
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
     * Возвращает дату и время последнего парсинга.
     *
     * @return дата последнего парсинга
     */
    public LocalDateTime getLastParsedAt() {
        return lastParsedAt;
    }

    /**
     * Устанавливает дату и время последнего парсинга.
     *
     * @param lastParsedAt дата последнего парсинга
     */
    public void setLastParsedAt(LocalDateTime lastParsedAt) {
        this.lastParsedAt = lastParsedAt;
    }

    /**
     * Возвращает список новостей из этого канала.
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
     * Сравнивает этот канал с другим по идентификатору.
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
        Channel channel = (Channel) o;
        return Objects.equals(id, channel.id);
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
     * Возвращает строковое представление канала.
     *
     * @return строка с информацией о канале
     */
    @Override
    public String toString() {
        return "Channel{"
                + "id=" + id
                + ", name='" + name + '\''
                + ", telegramHandle='" + telegramHandle + '\''
                + ", isActive=" + isActive
                + ", createdAt=" + createdAt
                + '}';
    }
}
