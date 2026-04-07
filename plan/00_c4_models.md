# Веха 0: C4 моделирование архитектуры

## Цель
Создать полные C4 модели системы MsQwen ПЕРЕД началом разработки. Эти модели станут основой для всех последующих вех и будут использоваться при принятии архитектурных решений.

## Что такое C4 модель

C4 модель — это набор диаграмм для описания архитектуры программного обеспечения на четырёх уровнях детализации:

1. **Context (Контекст)** — система в целом и её взаимодействие с внешним миром
2. **Containers (Контейнеры)** — приложения и хранилища данных внутри системы
3. **Components (Компоненты)** — ключевые компоненты каждого контейнера
4. **Code (Код)** — детали реализации (классы, интерфейсы)

## Важные принципы

- **C4 модели сначала**: Все решения по архитектуре основаны на C4 диаграммах
- **Живая документация**: Модели обновляются при изменениях в коде
- **Используем при разработке**: Каждая веха ссылается на соответствующий уровень C4
- **PlantUML/Structurizr**: Выбираем инструмент для диаграмм

## Задачи

### 0.1 Level 1: System Context Diagram (Контекст системы)

**Цель**: Показать MsQwen в контексте и взаимодействие с внешними системами.

- [ ] Определить систему MsQwen как центральный элемент
- [ ] Показать внешних акторов:
  - **Пользователь Telegram** — взаимодействует через бота
  - **Telegram API** — источник новостей через t.me/s/
  - **Qwen Code CLI** — AI агент для анализа новостей
  - **Администратор** — управляет системой через CLI/REST API
- [ ] Показать внешние зависимости:
  - Telegram Bot API
  - Qwen CLI (установлен локально)
  - H2 Database (встроена, но часть контекста)
- [ ] Описать потоки данных между системой и внешними системами
- [ ] Формат: PlantUML или Structurizr DSL

**Пример структуры**:
```
[Пользователь Telegram] --> (использует) --> [Telegram Bot]
[Telegram Bot] --> (входит в) --> [MsQwen System]
[MsQwen System] --> (парсит) --> [t.me/s/ каналы]
[MsQwen System] --> (анализирует через) --> [Qwen Code CLI]
[MsQwen System] --> (хранит в) --> [H2 Database]
[Администратор] --> (управляет через) --> [MsQwen CLI / REST API]
```

### 0.2 Level 2: Containers Diagram (Контейнеры)

**Цель**: Показать контейнеры внутри MsQwen и их взаимодействие.

- [ ] **Spring Boot Application** (Java 25+):
  - Telegram Bot контейнер
  - REST API контейнер
  - CLI контейнер
  - Parser модуль
  - Scheduler модуль
  - Qwen Integration модуль
  - Cache модуль
- [ ] **H2 Database** (встроенная):
  - Таблицы: users, prompts, channels, news, analysis_results
- [ ] **Log Files** (файловая система):
  - Консольные логи
  - Файловые логи с ротацией
- [ ] Показать протоколы взаимодействия:
  - HTTP/REST между API и клиентами
  - TCP между ботом и Telegram API
  - Process между приложением и Qwen CLI
  - JDBC между приложением и H2

**Пример структуры**:
```
[Telegram Bot] --> (HTTP polling) --> [Telegram API]
[Telegram Bot] --> (вызывает) --> [Service Layer]
[REST API] --> (HTTP/JSON) --> [Service Layer]
[CLI] --> (вызывает) --> [Service Layer]
[Parser] --> (Jsoup HTTP) --> [t.me/s/]
[Scheduler] --> (@Scheduled) --> [Parser, Qwen Service]
[Qwen Service] --> (ProcessBuilder) --> [Qwen CLI]
[Service Layer] --> (JPA/Hibernate) --> [H2 Database]
```

### 0.3 Level 3: Components Diagram (Компоненты)

**Цель**: Показать компоненты внутри Spring Boot Application.

- [ ] **Telegram Bot Components**:
  - BotConfiguration
  - BotCommandHandler
  - BotStateMachine (для диалогов)
  - BotKeyboardBuilder
  - BotNotificationService
- [ ] **REST API Components**:
  - UserController
  - PromptController
  - ChannelController
  - NewsController
  - AnalysisController
  - AdminController
  - GlobalExceptionHandler
- [ ] **CLI Components**:
  - CliRunner (CommandLineRunner)
  - ArgumentParser
  - CliCommandHandler
- [ ] **Service Layer Components**:
  - UserService
  - PromptService
  - ChannelService
  - NewsService
  - AnalysisService
  - QwenService
  - CacheService
- [ ] **Parser Components**:
  - TelegramParser
  - MetadataExtractor (views, forwards, reactions, likes)
  - EngagementScoreCalculator
  - NewsDeduplicator
- [ ] **Scheduler Components**:
  - SchedulerConfiguration
  - NewsParsingScheduler
  - NewsAnalysisScheduler
  - NewsCleanupScheduler
  - CacheCleanupScheduler
- [ ] **Data Access Components**:
  - UserRepository
  - PromptRepository
  - ChannelRepository
  - NewsRepository
  - AnalysisResultRepository
- [ ] **Configuration Components**:
  - DatabaseConfiguration
  - CacheConfiguration
  - OpenApiConfiguration
  - LoggingConfiguration

### 0.4 Level 4: Code Diagram (Код) — для ключевых классов

**Цель**: Показать структуру ключевых классов и их взаимосвязи.

- [ ] **Entity Classes** (JPA):
  - User (telegram_id, username, firstName, lastName, isActive)
  - Prompt (name, description, promptText, isActive, @OneToMany→Channel, @OneToMany→News)
  - Channel (name, telegramHandle, telegramUrl, description, @OneToMany→News)
  - News (title, content, publicationDate, viewsCount, forwardsCount, reactionsCount, likesCount, engagementScore, newsUrl, isAnalyzed)
  - AnalysisResult (analysisText, cacheKey, cacheExpiresAt, newsIds)
  - Показать связи: @ManyToOne, @OneToMany, CASCADE правила
- [ ] **DTO Records** (Java record):
  - UserDTO
  - PromptDTO
  - ChannelDTO
  - NewsDTO (со всеми метриками)
  - AnalysisResultDTO
  - ErrorResponseDTO
- [ ] **Service Classes** (бизнес-логика):
  - Показать зависимости между сервисами
  - Показать методы с параметрами userId для изоляции данных
- [ ] **Repository Interfaces**:
  - Spring Data JPA интерфейсы с кастомными методами

### 0.5 Диаграмма базы данных (дополнительно)

**Цель**: Визуальная схема всех таблиц и связей.

- [ ] Таблица users (BIGINT PK, telegram_id UNIQUE, username, firstName, lastName, isActive)
- [ ] Таблица prompts (BIGINT PK, user_id FK→users ON DELETE CASCADE)
- [ ] Таблица channels (BIGINT PK, prompt_id FK→prompts ON DELETE CASCADE, user_id FK→users)
- [ ] Таблица news (BIGINT PK, channel_id FK→channels ON DELETE CASCADE, prompt_id FK→prompts, user_id FK→users)
- [ ] Таблица analysis_results (BIGINT PK, prompt_id FK→prompts ON DELETE CASCADE, user_id FK→users)
- [ ] Все индексы на FK полях
- [ ] CASCADE DELETE правила
- [ ] UNIQUE constraints

### 0.6 Диаграмма потоков данных (Data Flow)

**Цель**: Показать как данные движутся через систему.

- [ ] **Парсинг новостей**:
  ```
  t.me/s/ → Jsoup Parser → ParsedNews (метаданные) → News Entity → H2 Database
  ```
- [ ] **Анализ новостей**:
  ```
  H2 Database → NewsService (unanalyzed) → QwenService (prompt с метаданными) 
  → Qwen CLI (ProcessBuilder) → AnalysisResult → H2 Database → Telegram Bot (уведомление)
  ```
- [ ] **Очистка старых новостей**:
  ```
  Scheduler → NewsRepository (publicationDate < 30 days) → DELETE CASCADE
  ```
- [ ] **Пользовательское взаимодействие**:
  ```
  User → Telegram Bot → Service Layer → H2 Database → Bot Response
  ```

### 0.7 Диаграмма последовательности (Sequence Diagrams)

**Цель**: Показать последовательность операций для ключевых сценариев.

- [ ] **Сценарий 1: Пользователь регистрируется и создает промпт**
  ```
  User → /start → Bot → UserService.registerUser() → UserRepository.save() → Bot Response
  User → /addprompt → Bot → Dialog → PromptService.createPrompt() → PromptRepository.save() → Bot Response
  User → /addchannel → Bot → Dialog → ChannelService.createChannel() → ChannelRepository.save() → Bot Response
  ```
- [ ] **Сценарий 2: Парсинг и анализ новостей**
  ```
  Scheduler → Parser.parseAllActiveChannels() → Jsoup → t.me/s/ → ParsedNews
  → NewsService.saveNews() → NewsRepository.save() → Bot Notification
  Scheduler → AnalysisService.analyzeNews() → QwenService.sendToQwen() → Qwen CLI
  → AnalysisResult → NewsService.markNewsAsAnalyzed() → Bot Notification
  ```
- [ ] **Сценарий 3: Пользователь удаляет промпт**
  ```
  User → /deleteprompt → Bot → PromptService.deletePrompt() 
  → CASCADE: ChannelService.deleteChannels() → NewsService.deleteNews()
  → AnalysisService.deleteAnalysis() → PromptRepository.deleteById() → Bot Response
  ```

### 0.8 Инструменты для C4

- [ ] Выбрать инструмент:
  - **PlantUML** (рекомендуется) — текстовые диаграммы, версионируемые
  - **Structurizr** — специализированный C4 инструмент
  - **Mermaid.js** — встроенная поддержка в Markdown
- [ ] Создать диаграммы в выбранном формате
- [ ] Сохранить в папке `docs/c4/` или `src/main/resources/c4/`
- [ ] Добавить ссылки на диаграммы в README.md

### 0.9 Верификация C4 моделей

- [ ] Проверить что все компоненты системы отражены в диаграммах
- [ ] Проверить что все связи между компонентами показаны
- [ ] Проверить что изоляция данных по user_id отражена в моделях
- [ ] Проверить что CASCADE правила показаны в диаграмме БД
- [ ] Проверить что потоки данных соответствуют требованиям README.md
- [ ] Согласовать модели с требованиями проекта

## Связь C4 моделей с вехами разработки

| Веха | Связанный уровень C4 |
|------|---------------------|
| 01: Настройка проекта | Level 2 (Containers) |
| 02: База данных | Level 4 (Code) + Диаграмма БД |
| 03: Репозитории | Level 3 (Components) + Level 4 (Code) |
| 04: Сервисы | Level 3 (Components) + Level 4 (Code) |
| 05: Парсер | Level 3 (Components) |
| 06: Планировщик | Level 2 (Containers) + Level 3 (Components) |
| 07: REST API | Level 3 (Components) + Level 4 (Code) |
| 08: Telegram бот | Level 3 (Components) + Level 4 (Code) |
| 09: CLI | Level 3 (Components) |
| 10: Qwen интеграция | Level 3 (Components) + Level 4 (Code) |
| 11: Docker | Level 2 (Containers) |

## Критерии готовности

- [ ] Level 1: Context Diagram создана и отражает все внешние взаимодействия
- [ ] Level 2: Containers Diagram показывает все контейнеры и протоколы
- [ ] Level 3: Components Diagram показывает все ключевые компоненты Spring Boot
- [ ] Level 4: Code Diagram показывает структуру Entity классов и DTO
- [ ] Диаграмма базы данных создана с CASCADE и индексами
- [ ] Диаграмма потоков данных отражает все сценарии
- [ ] Sequence Diagrams для ключевых сценариев созданы
- [ ] C4 модели сохранены в репозитории
- [ ] Все вехи разработки ссылаются на соответствующие C4 диаграммы
- [ ] Модели согласованы с требованиями README.md

## Примечания

- C4 модели — это **живая документация**, они должны обновляться при изменениях
- Каждая веха разработки должна **ссылаться** на соответствующие C4 диаграммы
- При возникновении вопросов к архитектуре — **сначала смотреть C4 модели**
- C4 модели помогают **новичкам быстро понять систему**
- Используются для **код-ревью** и **проверки архитектурных решений