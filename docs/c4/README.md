# C4 Architecture Models - MsQwen "Госпожа Qwen"

## Обзор

Этот каталог содержит C4 модели архитектуры системы MsQwen. C4 модель — это набор диаграмм для описания архитектуры программного обеспечения на четырёх уровнях детализации.

## Структура файлов

| Файл | Уровень | Описание |
|------|---------|----------|
| [01_system_context.puml](01_system_context.puml) | **Level 1: Context** | Система в контексте внешних акторов и систем |
| [01_system_context.md](01_system_context.md) | **Level 1: Context** | Документация Context Diagram |
| [02_containers.puml](02_containers.puml) | **Level 2: Containers** | Контейнеры внутри системы |
| [02_containers.md](02_containers.md) | **Level 2: Containers** | Документация Containers Diagram |
| [03_components.puml](03_components.puml) | **Level 3: Components** | Компоненты Spring Boot приложения |
| [03_components.md](03_components.md) | **Level 3: Components** | Документация Components Diagram |
| [04_code_entities.puml](04_code_entities.puml) | **Level 4: Code** | JPA Entity классы и связи |
| [05_database_schema.puml](05_database_schema.puml) | **Database** | Схема базы данных с индексами и CASCADE |
| [06_data_flows.puml](06_data_flows.puml) | **Data Flow** | Потоки данных через систему |
| [07_sequences.puml](07_sequences.puml) | **Sequence** | Диаграммы последовательности для сценариев |

## Просмотр диаграмм

Для просмотра PlantUML файлов используйте:
- **IntelliJ IDEA** с плагином PlantUML Integration
- **VS Code** с плагином PlantUML
- **Онлайн**: [PlantUML Web Server](http://www.plantuml.com/plantuml/uml/)
- **CLI**: `java -jar plantuml.jar *.puml`

## Уровни C4

### Level 1: System Context
Показывает MsQwen в контексте и взаимодействие с:
- Пользователь Telegram
- Администратор
- Telegram API (t.me/s/)
- Qwen Code CLI
- H2 Database

### Level 2: Containers
Показывает контейнеры внутри системы:
- Spring Boot Application (ядро)
- Telegram Bot
- REST API
- CLI Interface
- H2 Database
- Log Files

### Level 3: Components
Показывает компоненты внутри Spring Boot:
- Telegram Bot Components
- REST API Controllers
- CLI Components
- Service Layer
- Parser Components
- Scheduler Components
- Data Access Components
- Configuration Components

### Level 4: Code
Показывает структуру JPA Entity классов:
- User, Prompt, Channel, News, AnalysisResult
- Связи: @OneToMany, @ManyToOne
- CASCADE правила
- Методы и поля

## Дополнительные диаграммы

### Database Schema
- Все таблицы: users, prompts, channels, news, analysis_results
- Foreign Keys и CASCADE DELETE правила
- Индексы на FK и часто используемых полях
- UNIQUE constraints

### Data Flow Diagrams
1. **Парсинг новостей**: t.me/s/ → Jsoup → Metadata → Engagement Score → H2
2. **Анализ новостей**: H2 → Qwen CLI → AnalysisResult → Notification
3. **Очистка старых новостей**: Scheduler → DELETE > 30 дней
4. **Удаление промпта (CASCADE)**: Prompt → Channels → News → AnalysisResults

### Sequence Diagrams
1. **Регистрация + создание промпта + добавление канала**
2. **Парсинг новостей + анализ + получение результатов**
3. **Удаление промпта (CASCADE)**

## Связь с планами разработки

| Веха | Связанный уровень C4 |
|------|---------------------|
| 01: Настройка проекта | Level 2 (Containers) |
| 02: База данных | Level 4 (Code) + Database Schema |
| 03: Репозитории | Level 3 (Components) + Level 4 (Code) |
| 04: Сервисы | Level 3 (Components) + Level 4 (Code) |
| 05: Парсер | Level 3 (Components) |
| 06: Планировщик | Level 2 (Containers) + Level 3 (Components) |
| 07: REST API | Level 3 (Components) + Level 4 (Code) |
| 08: Telegram бот | Level 3 (Components) + Level 4 (Code) |
| 09: CLI | Level 3 (Components) |
| 10: Qwen интеграция | Level 3 (Components) + Level 4 (Code) |
| 11: Docker | Level 2 (Containers) |

## Живая документация

C4 модели — это **живая документация**. Они должны обновляться при:
- Добавлении новых компонентов
- Изменении структуры базы данных
- Изменении потоков данных
- Добавлении новых сценариев

**При изменении кода — обновляйте C4 модели!**
