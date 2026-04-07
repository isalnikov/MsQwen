# План разработки проекта MsQwen

## Обзор вех

| № | Веха | Описание |
|---|------|----------|
| 00 | [C4 модели](00_c4_models.md) | **ПЕРВЫЙ ШАГ**: Context, Containers, Components, Code диаграммы |
| 01 | [Настройка проекта](01_project_setup.md) | Maven, структура, Git, зависимости |
| 02 | [База данных](02_database_design.md) | Liquibase миграции, JPA Entity, индексы |
| 03 | [Репозитории](03_repository_layer.md) | Spring Data JPA репозитории |
| 04 | [Сервисы](04_service_layer.md) | Бизнес-логика, изоляция данных |
| 05 | [Парсер](05_parser_module.md) | Парсинг Telegram каналов через Jsoup |
| 06 | [Планировщик](06_scheduler_module.md) | @Scheduled задачи, cron |
| 07 | [REST API](07_rest_api.md) | Контроллеры, Swagger, DTO |
| 08 | [Telegram бот](08_telegram_bot.md) | Бот команды, клавиатуры |
| 09 | [CLI](09_cli_module.md) | Консольный интерфейс |
| 10 | [Qwen интеграция](10_qwen_integration.md) | Вызов Qwen CLI, анализ новостей |
| 11 | [Docker](11_docker_deployment.md) | Dockerfile, docker-compose |
| 12 | [Тестирование](12_testing_coverage.md) | TDD, JUnit 5, Mockito, JaCoCo 90%+ |
| 13 | [Финал](13_final_polish.md) | Документация, сборка, релиз |

## Порядок выполнения

```
00 → 01 → 02 → 03 → 04 → 05 → 06 → 07 → 08 → 09 → 10 → 11 → 12 → 13
       ^
       |
  C4 модели — основа для всей разработки
```

## Ключевые принципы

- **C4 модели сначала**: Создаем Context, Containers, Components, Code диаграммы ПЕРЕД началом разработки
- **TDD**: Тесты пишутся ДО кода
- **SOLID**: Каждый класс — одна ответственность
- **DRY**: Нет дублированию кода
- **KISS**: Код простой и понятный
- **YAGNI**: Не добавляем лишнее
- **Покрытие тестами**: 90%+
- **Без Lombok**: Пишем чистый и понятный код без магии
- **Java 25+**: Используем все современные фичи (Virtual Threads, Pattern Matching, Sequenced Collections, switch expressions, text blocks)
- **Spring Boot 4+**: Только современные подходы и аннотации
- **Изоляция данных**: Каждый пользователь работает только со своими данными
- **Каскадное удаление**: При удалении промпта удаляются каналы и новости

## Быстрый старт

```bash
# После завершения разработки
mvn clean package
java -jar target/MsQwen-1.0-SNAPSHOT.jar --help
```

## Docker запуск

```bash
docker-compose up -d
```

## Контакты

Проект: **MsQwen**  
Версия: **1.0-SNAPSHOT**
