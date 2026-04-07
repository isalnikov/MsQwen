# C4 Level 3: Components Diagram

## Группы компонентов

### Telegram Bot Components
| Компонент | Описание |
|-----------|----------|
| **BotConfiguration** | Конфигурация бота: токен, username из application.yml |
| **BotCommandHandler** | Обработка всех команд бота (/start, /parse, /analyze, /news, /prompts, /channels, и др.) |
| **BotStateMachine** | State management для многошаговых диалогов (addprompt, addchannel) |
| **BotKeyboardBuilder** | Создание inline клавиатур для выбора промптов, каналов, подтверждения |
| **BotNotificationService** | Отправка уведомлений о завершении парсинга и анализа |

### REST API Components
| Компонент | Endpoints | Описание |
|-----------|-----------|----------|
| **UserController** | /api/v1/users/* | Регистрация, CRUD пользователей |
| **PromptController** | /api/v1/prompts/* | CRUD промптов с изоляцией по userId |
| **ChannelController** | /api/v1/channels/* | CRUD каналов с изоляцией по userId |
| **NewsController** | /api/v1/news/* | Новости, запуск парсинга |
| **AnalysisController** | /api/v1/analysis/* | Запуск анализа, результаты, кеширование |
| **AdminController** | /api/v1/admin/* | Очистка таблиц, статистика (admin only) |
| **GlobalExceptionHandler** | @ControllerAdvice | Централизованная обработка ошибок |

### CLI Components
| Компонент | Описание |
|-----------|----------|
| **CliRunner** | CommandLineRunner реализация, точка входа CLI |
| **ArgumentParser** | Парсинг аргументов (--telegram.token, --scheduler.enabled, и др.) |
| **CliCommandHandler** | Обработка команд (--parse, --analyze, --cleanup, --stats, --version) |

### Service Layer Components
| Компонент | Описание |
|-----------|----------|
| **UserService** | Регистрация, CRUD пользователей, CASCADE удаление |
| **PromptService** | CRUD промптов, каскадное удаление каналов, новостей, результатов анализа |
| **ChannelService** | CRUD каналов, каскадное удаление новостей |
| **NewsService** | CRUD новостей, очистка > 30 дней, метрики вовлечённости |
| **AnalysisService** | Анализ новостей, кеширование с TTL, отправка в Qwen |
| **QwenService** | Вызов Qwen CLI через ProcessBuilder, парсинг ответа |
| **CacheService** | Spring Cache с @Cacheable, @CacheEvict, TTL, LRU |

### Parser Components
| Компонент | Описание |
|-----------|----------|
| **TelegramParser** | Парсинг t.me/s/ через Jsoup |
| **MetadataExtractor** | Извлечение views, forwards, reactions, likes |
| **EngagementScoreCalculator** | Расчет engagement_score по формуле |
| **NewsDeduplicator** | Проверка дубликатов по message_id + channel_id |

### Scheduler Components
| Компонент | Cron | Описание |
|-----------|------|----------|
| **SchedulerConfiguration** | - | @EnableScheduling, VirtualThreadTaskExecutor |
| **NewsParsingScheduler** | */30 min | Парсинг всех активных каналов |
| **NewsAnalysisScheduler** | hourly | Анализ непроанализированных новостей |
| **NewsCleanupScheduler** | daily 3am | Удаление новостей > 30 дней |
| **CacheCleanupScheduler** | daily 4am | Очистка просроченного кеша |

### Data Access Components
| Компонент | Описание |
|-----------|----------|
| **UserRepository** | findByTelegramId, изоляция по userId |
| **PromptRepository** | findAllByUserId, CASCADE удаление |
| **ChannelRepository** | findByPromptIdAndUserId |
| **NewsRepository** | findByIsAnalyzed, очистка по дате, пагинация |
| **AnalysisResultRepository** | findByCacheKey, TTL, история анализов |

### Configuration Components
| Компонент | Описание |
|-----------|----------|
| **DatabaseConfiguration** | H2 настройка, Liquibase миграции |
| **CacheConfiguration** | @EnableCaching, TTL, LRU eviction |
| **OpenApiConfiguration** | Springdoc OpenAPI, Swagger UI |
| **LoggingConfiguration** | Logback консоль + файл, ротация |

## PlantUML файл
[03_components.puml](03_components.puml)
