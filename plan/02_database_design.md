# Веха 2: Проектирование базы данных и миграции

## Связь с C4 моделями
- **Level 4 (Code)**: Entity классы соответствуют диаграмме кода
- **Диаграмма БД**: Полностью отражает структуру таблиц и связей

## Цель
Спроектировать и реализовать схему базы данных с правильными индексами и CASCADE правилами согласно C4 моделям.

## Задачи

### 2.1 Проектирование схемы БД

**ВАЖНО**: 
- Каждый пользователь может работать только со своими данными (изоляция по user_id)
- При удалении промпта каскадно удаляются каналы и новости
- При удалении канала каскадно удаляются новости
- Новости старше 30 дней удаляются по расписанию

#### Таблица users
```sql
- id (BIGINT, PRIMARY KEY, AUTO_INCREMENT)
- telegram_id (BIGINT, UNIQUE, NOT NULL)
- username (VARCHAR)
- first_name (VARCHAR)
- last_name (VARCHAR)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
- is_active (BOOLEAN)
```

#### Таблица prompts
```sql
- id (BIGINT, PRIMARY KEY, AUTO_INCREMENT)
- user_id (BIGINT, FOREIGN KEY → users.id, ON DELETE CASCADE)
- name (VARCHAR, NOT NULL)
- description (TEXT)
- prompt_text (TEXT, NOT NULL)
- is_active (BOOLEAN)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

#### Таблица channels
```sql
- id (BIGINT, PRIMARY KEY, AUTO_INCREMENT)
- prompt_id (BIGINT, FOREIGN KEY → prompts.id, ON DELETE CASCADE)
- user_id (BIGINT, FOREIGN KEY → users.id, ON DELETE CASCADE) - для изоляции данных
- name (VARCHAR, NOT NULL)
- telegram_handle (VARCHAR, NOT NULL)
- telegram_url (VARCHAR)
- description (TEXT)
- is_active (BOOLEAN)
- created_at (TIMESTAMP)
- last_parsed_at (TIMESTAMP)
```

#### Таблица news
```sql
- id (BIGINT, PRIMARY KEY, AUTO_INCREMENT)
- channel_id (BIGINT, FOREIGN KEY → channels.id, ON DELETE CASCADE)
- prompt_id (BIGINT, FOREIGN KEY → prompts.id, ON DELETE CASCADE)
- user_id (BIGINT, FOREIGN KEY → users.id, ON DELETE CASCADE) - для изоляции данных
- message_id (BIGINT)
- title (VARCHAR)
- content (TEXT)
- publication_date (TIMESTAMP)
- views_count (INTEGER) - просмотры
- forwards_count (INTEGER) - пересылки
- reactions_count (INTEGER) - реакции
- likes_count (INTEGER) - лайки
- engagement_score (DECIMAL) - индекс вовлечённости
- news_url (VARCHAR)
- is_analyzed (BOOLEAN, DEFAULT FALSE)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

#### Таблица analysis_results
```sql
- id (BIGINT, PRIMARY KEY, AUTO_INCREMENT)
- prompt_id (BIGINT, FOREIGN KEY → prompts.id, ON DELETE CASCADE)
- user_id (BIGINT, FOREIGN KEY → users.id, ON DELETE CASCADE) - для изоляции данных
- analysis_text (TEXT, NOT NULL)
- news_ids (TEXT) - ID новостей, участвовавших в анализе
- created_at (TIMESTAMP)
- cache_key (VARCHAR, UNIQUE)
- cache_expires_at (TIMESTAMP)
```

### 2.2 Liquibase миграции

**ВАЖНО о CASCADE удалении**:
- Пользователь → (1:M) → Промпты (при удалении пользователя удаляются промпты)
- Промпт → (1:M) → Каналы (при удалении промпта удаляются каналы)
- Промпт → (1:M) → Новости (при удалении промпта удаляются новости)
- Канал → (1:M) → Новости (при удалении канала удаляются новости)
- Промпт → (1:M) → Результаты анализа (при удалении промпта удаляются результаты)

**Правила изоляции данных**:
- Все таблицы (кроме users) имеют user_id для проверки прав доступа
- Пользователь может работать только со своими данными
- При запросах всегда фильтруем по user_id

#### Changelog 001: users table
- [ ] Создать `db/changelog/001-create-users-table.xml`

#### Changelog 002: prompts table
- [ ] Создать `db/changelog/002-create-prompts-table.xml`

#### Changelog 003: channels table
- [ ] Создать `db/changelog/003-create-channels-table.xml`

#### Changelog 004: news table
- [ ] Создать `db/changelog/004-create-news-table.xml`

#### Changelog 005: analysis_results table
- [ ] Создать `db/changelog/005-create-analysis-results-table.xml`

#### Changelog 006: indexes and constraints
- [ ] Создать `db/changelog/006-add-indexes.xml`
  - Индексы на foreign keys (prompt_id, channel_id, user_id)
  - Индексы на часто используемых полях (telegram_id, is_analyzed, publication_date, user_id)
  - UNIQUE constraints (telegram_id в users)
  - Индексы для оптимизации запросов с WHERE user_id

#### Changelog 007: cleanup old news procedure
- [ ] Создать `db/changelog/007-cleanup-procedure.xml`
  - Процедура для удаления новостей старше 30 дней
  - Индексы для оптимизации очистки по дате

### 2.3 JPA Entity классы (без Lombok!)

**ВАЖНО**: 
- НЕ использовать Lombok - пишем геттеры, сеттеры, toString, equals, hashCode вручную
- Использовать record для DTO, class для JPA-сущностей
- Внедрение зависимостей через конструктор (final поля)
- Все entity классы должны иметь одну ответственность

- [ ] `User.java` - entity класс
  - @Id, @GeneratedValue для id
  - @Column(unique = true) для telegramId
  - @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true) для prompts
  
- [ ] `Prompt.java` - entity класс
  - @ManyToOne для user (user_id)
  - @OneToMany(mappedBy = "prompt", cascade = CascadeType.ALL, orphanRemoval = true) для channels
  - @OneToMany(mappedBy = "prompt", cascade = CascadeType.ALL, orphanRemoval = true) для news
  - @OneToMany(mappedBy = "prompt", cascade = CascadeType.ALL, orphanRemoval = true) для analysisResults
  
- [ ] `Channel.java` - entity класс
  - @ManyToOne для prompt (prompt_id)
  - @ManyToOne для user (user_id)
  - @OneToMany(mappedBy = "channel", cascade = CascadeType.ALL, orphanRemoval = true) для news
  
- [ ] `News.java` - entity класс
  - @ManyToOne для channel (channel_id)
  - @ManyToOne для prompt (prompt_id)
  - @ManyToOne для user (user_id)
  - Все метрики: viewsCount, forwardsCount, reactionsCount, likesCount, engagementScore
  
- [ ] `AnalysisResult.java` - entity класс
  - @ManyToOne для prompt (prompt_id)
  - @ManyToOne для user (user_id)
  - @Column(unique = true) для cacheKey
  - TTL для кеша (cacheExpiresAt)

## Критерии готовности
- [ ] Все миграции Liquibase созданы и применяются
- [ ] JPA entity классы написаны без Lombok
- [ ] Индексы проставлены на FK и часто используемых полях
- [ ] CASCADE DELETE настроен и протестирован
- [ ] База данных H2 создается при запуске приложения
- [ ] Изоляция данных по user_id реализована во всех entity
- [ ] CASCADE правила работают корректно (тесты на удаление)
- [ ] News entity содержит все метрики вовлечённости
- [ ] AnalysisResult содержит news_ids для отслеживания проанализированных новостей
