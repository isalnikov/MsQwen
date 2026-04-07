# C4 Level 2: Containers Diagram

## Контейнеры системы

| Контейнер | Технология | Описание |
|-----------|-----------|----------|
| **Spring Boot Application** | Java 25+ / Spring Boot 4+ | Ядро системы: UserService, PromptService, ChannelService, NewsService, AnalysisService, QwenService, CacheService, Scheduler |
| **Telegram Bot** | Java / TelegramLongPollingBot | Обработка команд, inline клавиатуры, state management для диалогов, уведомления |
| **REST API** | Java / Spring MVC | HTTP endpoints (/api/v1/*), Swagger документация, валидация @Valid, @ControllerAdvice |
| **CLI Interface** | Java / CommandLineRunner | Консольные команды (--parse, --analyze, --cleanup, --stats, --version) |
| **H2 Database** | H2 / JPA | Локальная БД: users, prompts, channels, news, analysis_results |
| **Log Files** | File System / Logback | Консольные и файловые логи с ротацией |

## Протоколы взаимодействия

| Источник → Назначение | Протокол | Описание |
|----------------------|----------|----------|
| Пользователь → Telegram Bot | Telegram Protocol | Команды бота, текстовые сообщения |
| Администратор → REST API | HTTP/REST | CRUD операции, Swagger UI |
| Администратор → CLI | Command Line | Параметры запуска, команды очистки |
| Telegram Bot → Spring Boot | Java Method Calls | Вызов сервисов |
| REST API → Spring Boot | Java Method Calls | Вызов сервисов |
| CLI → Spring Boot | Java Method Calls | Вызов сервисов |
| Spring Boot → Telegram API | Jsoup HTTP | Парсинг t.me/s/ |
| Spring Boot → Qwen CLI | ProcessBuilder | Вызов qwen --prompt="..." |
| Spring Boot → H2 Database | JDBC/JPA | Сохранение/чтение данных |
| Spring Boot → Log Files | Logback | Запись логов |

## PlantUML файл
[02_containers.puml](02_containers.puml)
