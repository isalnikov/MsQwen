# MsQwen — Госпожа Qwen AI

[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](Dockerfile)

Парсинг и AI-анализ новостей из публичных Telegram-каналов через Qwen Code CLI.

---

## 📋 Оглавление

- [Описание](#-описание)
- [Возможности](#-возможности)
- [Архитектура](#-архитектура)
- [Быстрый старт](#-быстрый-старт)
- [Установка и запуск](#-установка-и-запуск)
- [Telegram бот](#-telegram-бот)
- [REST API](#-rest-api)
- [CLI](#-cli)
- [Docker](#-docker)
- [Конфигурация](#-конфигурация)
- [Тестирование](#-тестирование)
- [Структура проекта](#-структура-проекта)
- [Технологии](#-технологии)
- [Лицензия](#-лицензия)

---

## 📖 Описание

**MsQwen** — приложение на Spring Boot для автоматического парсинга новостей из публичных Telegram-каналов (через `https://t.me/s/`) и их анализа через **Qwen Code CLI** с использованием профессиональных промптов.

### Как это работает

1. **Парсинг**: Приложение вычитывает новости из указанных Telegram-каналов через Jsoup
2. **Сохранение**: Все новости с метаданными (просмотры, репосты, реакции, лайки) сохраняются в H2 БД
3. **AI-анализ**: Новости отправляются в Qwen CLI для анализа по заданному промпту
4. **Результат**: Анализ сохраняется в БД и доступен через API, Telegram бота или CLI

### Ключевые особенности

- 🔍 **Метрики вовлечённости**: views, forwards, reactions, likes, engagement_score
- 🤖 **AI-анализ**: Qwen Code CLI с настраиваемыми промптами
- ⏰ **Планировщик**: Автоматический парсинг и анализ по расписанию
- 💾 **Кеширование**: Результаты анализа кешируются с TTL
- 📊 **Приоритизация**: Новости с высоким engagement_score анализируются первыми
- 🔒 **Изоляция данных**: Каждый пользователь работает только со своими данными
- 🗑️ **CASCADE удаление**: При удалении промпта удаляются каналы, новости, результаты анализа

---

## ✨ Возможности

- [x] Парсинг новостей из Telegram каналов (через `t.me/s/`)
- [x] Сохранение новостей в H2 базу данных с метаданными
- [x] REST API endpoints для CRUD операций
- [x] Swagger/OpenAPI документация
- [x] Telegram бот с интерактивными командами
- [x] Планировщик задач (Spring @Scheduled)
- [x] Docker контейнеризация
- [x] Кеширование результатов анализа (Spring Cache)
- [x] CLI интерфейс для администрирования
- [x] Миграции БД через Liquibase
- [x] Rate limiting для API
- [x] Health check endpoints
- [x] Логирование с ротацией файлов
- [x] CORS настройка
- [x] Автоматическая очистка старых новостей (>30 дней)

---

## 🏗️ Архитектура

### C4 Модели

Документация архитектуры доступна в [docs/c4/](docs/c4/):

- **Level 1**: [System Context](docs/c4/01_system_context.md)
- **Level 2**: [Containers](docs/c4/02_containers.md)
- **Level 3**: [Components](docs/c4/03_components.md)
- **Level 4**: [Code Entities](docs/c4/04_code_entities.puml)
- **Database Schema**: [ER Diagram](docs/c4/05_database_schema.puml)
- **Data Flows**: [Потоки данных](docs/c4/06_data_flows.puml)
- **Sequences**: [Диаграммы последовательности](docs/c4/07_sequences.puml)

### Схема данных

```
User (1) ──┬── (N) Prompt (1) ──┬── (N) Channel (1) ──┬── (N) News
           │                    │                      └── (N) AnalysisResult
           │                    └── (N) AnalysisResult
           └── (N) News
```

---

## 🚀 Быстрый старт

### Требования

- Java 25+
- Maven 3.8+
- Qwen Code CLI (установлен и доступен в PATH)

### Сборка и запуск

```bash
# Клонировать репозиторий
git clone https://github.com/isalnikov/MsQwen.git
cd MsQwen

# Собрать проект
mvn clean package

# Запустить
java -jar target/MsQwen-1.0-SNAPSHOT.jar
```

### Docker запуск

```bash
# Собрать и запустить через docker-compose
docker compose up -d

# Или собрать образ вручную
./build-docker.sh --run
```

---

## 📦 Установка и запуск

### Из JAR файла

```bash
# Запуск с параметрами
java -jar target/MsQwen-1.0-SNAPSHOT.jar \
  --telegram.bot.token=YOUR_BOT_TOKEN \
  --scheduler.enabled=true

# Или через переменные окружения
export TELEGRAM_BOT_TOKEN=your_token_here
java -jar target/MsQwen-1.0-SNAPSHOT.jar
```

### Из исходников

```bash
mvn clean package -DskipTests
java -jar target/MsQwen-1.0-SNAPSHOT.jar
```

---

## 🤖 Telegram бот

### Команды бота

| Команда | Описание |
|---------|----------|
| `/start` | Приветствие и главное меню |
| `/help` | Справка по всем командам |
| `/status` | Статистика системы |
| `/prompts` | Список активных промптов |
| `/addprompt` | Добавить новый промпт |
| `/deleteprompt` | Удалить промпт |
| `/channels` | Список отслеживаемых каналов |
| `/addchannel` | Добавить новый канал |
| `/deletechannel` | Удалить канал |
| `/parse` | Запустить парсинг новостей |
| `/analyze` | Проанализировать новости через Qwen CLI |
| `/news` | Получить результаты анализа |
| `/version` | Информация о сборке |
| `/cancel` | Отменить текущий диалог |

### Примеры использования

```
/start → 👋 Добро пожаловать в MsQwen!
/addprompt → Введите название промпта:
  → "Анализ политики"
  → Введите описание:
  → "Политические новости"
  → Введите текст промпта:
  → (текст промпта для AI)
  → ✅ Промпт создан!

/addchannel → Введите название канала:
  → "PolitNews"
  → Введите handle:
  → "@politnews"
  → ✅ Канал добавлен!

/parse → ⏳ Запускаю парсинг 3 каналов...
  → ✅ Парсинг завершён! Добавлено новостей: 15

/analyze → ⏳ Запускаю анализ 15 новостей...
  → ✅ Анализ завершён!
  → 📝 Результат: (AI-анализ новостей)
```

---

## 🌐 REST API

### Документация

Swagger UI доступен по адресу: `http://localhost:8080/swagger-ui.html`

API docs: `http://localhost:8080/v3/api-docs`

### Основные endpoints

| Метод | Endpoint | Описание |
|-------|----------|----------|
| **Users** | | |
| POST | `/api/v1/users/register` | Регистрация пользователя |
| GET | `/api/v1/users/me?telegramId={id}` | Получить пользователя |
| PUT | `/api/v1/users/me?telegramId={id}` | Обновить пользователя |
| DELETE | `/api/v1/users/me?telegramId={id}` | Удалить пользователя |
| **Prompts** | | |
| POST | `/api/v1/prompts?userId={id}` | Создать промпт |
| GET | `/api/v1/prompts?userId={id}` | Список промптов |
| GET | `/api/v1/prompts/{id}?userId={id}` | Получить промпт |
| PUT | `/api/v1/prompts/{id}?userId={id}` | Обновить промпт |
| DELETE | `/api/v1/prompts/{id}?userId={id}` | Удалить промпт |
| **Channels** | | |
| POST | `/api/v1/channels?promptId={id}&userId={id}` | Добавить канал |
| GET | `/api/v1/channels/prompt/{id}?userId={id}` | Каналы промпта |
| GET | `/api/v1/channels/{id}?userId={id}` | Получить канал |
| PUT | `/api/v1/channels/{id}?userId={id}` | Обновить канал |
| DELETE | `/api/v1/channels/{id}?userId={id}` | Удалить канал |
| **News** | | |
| GET | `/api/v1/news/prompt/{id}?userId={id}` | Новости промпта |
| GET | `/api/v1/news/{id}?userId={id}` | Получить новость |
| POST | `/api/v1/news/parse` | Запустить парсинг |
| DELETE | `/api/v1/news/prompt/{id}?userId={id}` | Удалить новости |
| **Analysis** | | |
| POST | `/api/v1/analysis` | Запустить анализ |
| GET | `/api/v1/analysis/prompt/{id}?userId={id}` | История анализов |
| GET | `/api/v1/analysis/{id}?userId={id}` | Результат анализа |
| DELETE | `/api/v1/analysis/prompt/{id}?userId={id}` | Удалить результаты |
| **Admin** | | |
| GET | `/api/v1/admin/stats` | Статистика системы |
| DELETE | `/api/v1/admin/cleanup/news` | Очистить новости |
| DELETE | `/api/v1/admin/cleanup/channels` | Очистить каналы |
| DELETE | `/api/v1/admin/cleanup/users` | Очистить пользователей |
| DELETE | `/api/v1/admin/cleanup/all` | Полная очистка |

### Примеры curl запросов

```bash
# Регистрация пользователя
curl -X POST http://localhost:8080/api/v1/users/register \
  -H "Content-Type: application/json" \
  -d '{"telegramId": 12345, "username": "testuser", "firstName": "Test"}'

# Создание промпта
curl -X POST "http://localhost:8080/api/v1/prompts?userId=1" \
  -H "Content-Type: application/json" \
  -d '{"name": "Политика", "description": "Политические новости", "promptText": "Проанализируй..."}'

# Добавление канала
curl -X POST "http://localhost:8080/api/v1/channels?promptId=1&userId=1" \
  -H "Content-Type: application/json" \
  -d '{"name": "PolitNews", "telegramHandle": "@politnews", "telegramUrl": "https://t.me/politnews"}'

# Запуск парсинга
curl -X POST "http://localhost:8080/api/v1/news/parse" \
  -H "Content-Type: application/json" \
  -d '{"promptId": 1}'

# Получение новостей
curl "http://localhost:8080/api/v1/news/prompt/1?userId=1"

# Запуск анализа
curl -X POST "http://localhost:8080/api/v1/analysis" \
  -H "Content-Type: application/json" \
  -d '{"promptId": 1}'

# Статистика системы
curl http://localhost:8080/api/v1/admin/stats

# Health check
curl http://localhost:8080/actuator/health
```

---

## 💻 CLI

### Команды командной строки

```bash
# Справка
java -jar msqwen.jar --help

# Парсинг новостей
java -jar msqwen.jar --parse --user.id=1

# Анализ новостей
java -jar msqwen.jar --analyze --prompt.id=1 --user.id=1

# Статистика
java -jar msqwen.jar --stats --user.id=1

# Очистка данных
java -jar msqwen.jar --cleanup --target=news --user.id=1

# Версия
java -jar msqwen.jar --version
```

---

## 🐳 Docker

### Быстрый старт

```bash
# 1. Создать .env файл
cp .env.example .env
# Отредактировать .env и указать TELEGRAM_BOT_TOKEN

# 2. Запустить
docker compose up -d

# 3. Проверить статус
docker compose ps

# 4. Посмотреть логи
docker compose logs -f msqwen
```

### Ручная сборка

```bash
# Собрать образ
docker build -t msqwen .

# Запустить
docker run -p 8080:8080 \
  -e TELEGRAM_BOT_TOKEN=your_token \
  -e SCHEDULER_ENABLED=true \
  msqwen
```

### Volumes

| Volume | Назначение |
|--------|------------|
| `msqwen-data` | H2 база данных (`/app/data`) |
| `msqwen-logs` | Лог файлы (`/app/logs`) |

---

## ⚙️ Конфигурация

### Переменные окружения

| Переменная | Описание | По умолчанию |
|------------|----------|--------------|
| `TELEGRAM_BOT_TOKEN` | Токен Telegram бота | - |
| `TELEGRAM_BOT_USERNAME` | Имя бота | `MsQwenBot` |
| `QWEN_CLI_PATH` | Путь к Qwen CLI | `qwen` |
| `QWEN_CLI_TIMEOUT` | Таймаут Qwen CLI (сек) | `300` |
| `SCHEDULER_ENABLED` | Включить планировщик | `true` |
| `SCHEDULER_PARSE_CRON` | Cron парсинга | `0 */30 * * * *` |
| `SCHEDULER_ANALYZE_CRON` | Cron анализа | `0 0 * * * *` |
| `SCHEDULER_CLEANUP_CRON` | Cron очистки | `0 0 3 * * *` |
| `CACHE_ANALYSIS_TTL` | Время жизни кеша (сек) | `3600` |
| `NEWS_CLEANUP_DAYS` | Хранить новости (дней) | `30` |
| `RATE_LIMIT_ENABLED` | Включить rate limiting | `true` |
| `RATE_LIMIT_REQUESTS_PER_MINUTE` | Запросов в минуту | `60` |
| `CORS_ALLOWED_ORIGINS` | Разрешённые CORS origin | `http://localhost:3000,http://localhost:8080` |

Все параметры можно задать в `application.yml` или через переменные окружения.

---

## 🧪 Тестирование

```bash
# Запустить все тесты
mvn test

# Генерация отчёта JaCoCo
mvn jacoco:report

# Отчёт доступен в target/site/jacoco/index.html
```

### Покрытие тестами

| Компонент | Статус |
|-----------|--------|
| Entity классы | ✅ |
| Repository классы | ✅ |
| Service классы | ✅ |
| Controller классы | ✅ |
| Parser классы | ✅ |
| Scheduler классы | ✅ |
| Bot классы | ✅ |
| CLI классы | ✅ |
| QwenService | ✅ |

### Типы тестов

- **Unit тесты**: Mockito + JUnit 5
- **Integration тесты**: @SpringBootTest + H2 in-memory
- **Repository тесты**: @DataJpaTest
- **Controller тесты**: @AutoConfigureMockMvc
- **Тесты изоляции данных**: Проверка user_id
- **Тесты CASCADE удаления**: Проверка каскадных операций

---

## 📁 Структура проекта

```
MsQwen/
├── src/
│   ├── main/
│   │   ├── java/com/isalnikov/msqwen/
│   │   │   ├── bot/              # Telegram бот
│   │   │   │   ├── TelegramBotCommandHandler.java
│   │   │   │   └── TelegramKeyboardFactory.java
│   │   │   ├── cli/              # CLI интерфейс
│   │   │   │   ├── CliArgumentParser.java
│   │   │   │   ├── CliCommandHandler.java
│   │   │   │   └── CliRunner.java
│   │   │   ├── config/           # Конфигурация
│   │   │   │   ├── RateLimiterFilter.java
│   │   │   │   ├── SchedulerConfig.java
│   │   │   │   ├── TelegramBotConfig.java
│   │   │   │   ├── TelegramBotHandler.java
│   │   │   │   └── WebConfig.java
│   │   │   ├── controller/       # REST API
│   │   │   │   ├── AdminController.java
│   │   │   │   ├── AnalysisController.java
│   │   │   │   ├── ChannelController.java
│   │   │   │   ├── NewsController.java
│   │   │   │   ├── PromptController.java
│   │   │   │   └── UserController.java
│   │   │   ├── dto/              # Data Transfer Objects
│   │   │   ├── entity/           # JPA сущности
│   │   │   ├── exception/        # Исключения
│   │   │   ├── parser/           # Парсер Telegram
│   │   │   ├── repository/       # Spring Data репозитории
│   │   │   ├── scheduler/        # Планировщик
│   │   │   └── service/          # Бизнес-логика
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-docker.yml
│   │       └── qwen-prompt.txt    # Промпт для AI анализа
│   └── test/
│       └── java/com/isalnikov/msqwen/
├── docs/                         # C4 модели
├── plan/                         # План разработки
├── data/                         # H2 БД (gitignore)
├── logs/                         # Логи (gitignore)
├── .github/workflows/ci.yml      # CI/CD
├── Dockerfile
├── docker-compose.yml
├── .env.example
├── .dockerignore
├── build-docker.sh
├── msqwen_logo.ico
└── pom.xml
```

---

## 🛠️ Технологии

| Технология | Версия | Назначение |
|------------|--------|------------|
| **Java** | 25+ | Язык программирования |
| **Spring Boot** | 3.2.0 | Фреймворк |
| **Spring Data JPA** | - | ORM для работы с БД |
| **H2 Database** | - | Локальная база данных |
| **Liquibase** | - | Миграции БД |
| **Jsoup** | 1.17.2 | Парсинг HTML |
| **Telegram Bots API** | 6.9.7.1 | Telegram бот |
| **Springdoc OpenAPI** | 2.3.0 | Swagger документация |
| **JUnit 5** | - | Тестирование |
| **Mockito** | 5.15.2 | Mock объектов |
| **JaCoCo** | 0.8.12 | Покрытие кода |
| **Maven** | - | Сборка проекта |

### Принципы разработки

- **SOLID** — каждый класс имеет одну ответственность
- **DRY** — нет дублирования кода
- **KISS** — код простой и понятный
- **YAGNI** — нет лишней функциональности
- **TDD** — тесты пишутся до кода
- **Без Lombok** — чистый и понятный код
- **Java 25+** — используются все современные фичи:
  - `record` для DTO
  - `var` для локальных переменных
  - `switch expressions`
  - `text blocks` для многострочных строк
  - `Virtual Threads` для конкурентных задач

---

## 📄 Лицензия

Этот проект лицензирован по лицензии **MIT** — см. файл [LICENSE](LICENSE) для подробностей.

---

## 👨‍💻 Автор

**isalnikov** — [GitHub](https://github.com/isalnikov/MsQwen)

---

## 📊 Статус проекта

| Веха | Статус |
|------|--------|
| 00. C4 модели | ✅ Завершена |
| 01. Настройка проекта | ✅ Завершена |
| 02. База данных | ✅ Завершена |
| 03. Репозитории | ✅ Завершена |
| 04. Сервисы | ✅ Завершена |
| 05. Парсер | ✅ Завершена |
| 06. Планировщик | ✅ Завершена |
| 07. REST API | ✅ Завершена |
| 08. Telegram бот | ✅ Завершена |
| 09. CLI | ✅ Завершена |
| 10. Qwen интеграция | ✅ Завершена |
| 11. Docker | ✅ Завершена |
| 12. Тестирование | ✅ Завершена |
| 13. Финал | ⏳ В процессе |

---

<p align="center">
  <strong>MsQwen v1.0-SNAPSHOT</strong><br>
  <em>Парсинг и AI-анализ новостей из Telegram каналов</em>
</p>
