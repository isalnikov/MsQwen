package com.isalnikov.msqwen.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * JPA Entity класса пользователя системы.
 * 
 * <p>Представляет пользователя Telegram который использует систему MsQwen.
 * Каждый пользователь может создавать промпты каналы и управлять новостями.</p>
 * 
 * <p>Таблица: users</p>
 * 
 * @author isalnikov
 * @version 1.0
 */
@Entity
@Table(name = "users")
public class User {

    /**
     * Уникальный идентификатор пользователя (первичный ключ).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Telegram ID пользователя (уникальный).
     */
    @Column(name = "telegram_id", unique = true, nullable = false)
    private Long telegramId;

    /**
     * Имя пользователя в Telegram.
     */
    @Column(name = "username")
    private String username;

    /**
     * Имя пользователя.
     */
    @Column(name = "first_name")
    private String firstName;

    /**
     * Фамилия пользователя.
     */
    @Column(name = "last_name")
    private String lastName;

    /**
     * Флаг активности пользователя.
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Дата и время создания записи.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Дата и время последнего обновления записи.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Конструктор по умолчанию для JPA.
     */
    public User() {
    }

    /**
     * Конструктор с основными полями.
     *
     * @param telegramId Telegram ID пользователя
     * @param username имя пользователя
     * @param firstName имя
     * @param lastName фамилия
     */
    public User(Long telegramId, String username, String firstName, String lastName) {
        this.telegramId = telegramId;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Возвращает уникальный идентификатор пользователя.
     *
     * @return идентификатор пользователя
     */
    public Long getId() {
        return id;
    }

    /**
     * Устанавливает уникальный идентификатор пользователя.
     *
     * @param id идентификатор пользователя
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Возвращает Telegram ID пользователя.
     *
     * @return Telegram ID
     */
    public Long getTelegramId() {
        return telegramId;
    }

    /**
     * Устанавливает Telegram ID пользователя.
     *
     * @param telegramId Telegram ID
     */
    public void setTelegramId(Long telegramId) {
        this.telegramId = telegramId;
    }

    /**
     * Возвращает имя пользователя в Telegram.
     *
     * @return имя пользователя
     */
    public String getUsername() {
        return username;
    }

    /**
     * Устанавливает имя пользователя в Telegram.
     *
     * @param username имя пользователя
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Возвращает имя пользователя.
     *
     * @return имя
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Устанавливает имя пользователя.
     *
     * @param firstName имя
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Возвращает фамилию пользователя.
     *
     * @return фамилия
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Устанавливает фамилию пользователя.
     *
     * @param lastName фамилия
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Возвращает флаг активности пользователя.
     *
     * @return true если пользователь активен
     */
    public Boolean getIsActive() {
        return isActive;
    }

    /**
     * Устанавливает флаг активности пользователя.
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
     * Сравнивает этого пользователя с другим по идентификатору и telegramId.
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
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(telegramId, user.telegramId);
    }

    /**
     * Возвращает хеш-код объекта на основе идентификатора и telegramId.
     *
     * @return хеш-код
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, telegramId);
    }

    /**
     * Возвращает строковое представление пользователя.
     *
     * @return строка с информацией о пользователе
     */
    @Override
    public String toString() {
        return "User{"
                + "id=" + id
                + ", telegramId=" + telegramId
                + ", username='" + username + '\''
                + ", firstName='" + firstName + '\''
                + ", lastName='" + lastName + '\''
                + ", isActive=" + isActive
                + ", createdAt=" + createdAt
                + '}';
    }
}
