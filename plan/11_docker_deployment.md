# Веха 11: Docker контейнеризация

## Связь с C4 моделями
- **Level 2 (Containers)**: Docker — физическое воплощение контейнеров
- **Level 1 (Context)**: Docker Compose показывает все контейнеры системы

## Цель
Создать Docker образ и docker-compose конфигурацию для простого развертывания с поддержкой Java 25+ и Spring Boot 4+ согласно C4 моделям.

## Важные принципы
- **Java 25+ Base Image**: eclipse-temurin:25-jre-alpine или новее
- **Spring Boot 4+**: Совместимость с последними версиями
- **Volumes**: Для персистентности данных (data/, logs/)
- **Environment Variables**: Все настраиваемые параметры
- **Virtual Threads**: Поддержка в Docker

## Задачи

### 11.1 Dockerfile
- [ ] Создать `Dockerfile` в корне проекта
- [ ] Базовый образ: `eclipse-temurin:25-jre-alpine` (Java 25+)
- [ ] Копирование JAR файла
- [ ] Настройка entrypoint с параметрами
- [ ] Настройка переменных окружения:
  - TELEGRAM_BOT_TOKEN
  - TELEGRAM_CHAT_ID
  - QWEN_API_KEY
  - GITHUB_TOKEN (для коммитов)
- [ ] Expose портов (8080 для API)
- [ ] Поддержка Virtual Threads (настройки JVM)

Пример структуры:
```dockerfile
FROM eclipse-temurin:25-jre-alpine
VOLUME /tmp /data /logs
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENV TELEGRAM_BOT_TOKEN=""
ENV TELEGRAM_CHAT_ID=""
ENV QWEN_API_KEY=""
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java","-jar","/app.jar"]
```

### 11.2 docker-compose.yml
- [ ] Создать `docker-compose.yml`
- [ ] Сервис msqwen (приложение)
- [ ] Настройка networks
- [ ] Настройка volumes для персистентности данных:
  - msqwen-data:/data (H2 база данных)
  - msqwen-logs:/logs (лог файлы)
- [ ] Environment variables из README

Пример структуры:
```yaml
version: '3.8'
services:
  msqwen:
    build: .
    ports:
      - "8080:8080"
    environment:
      - TELEGRAM_BOT_TOKEN=${TELEGRAM_BOT_TOKEN}
      - TELEGRAM_CHAT_ID=${TELEGRAM_CHAT_ID}
      - QWEN_API_KEY=${QWEN_API_KEY}
      - GITHUB_TOKEN=${GITHUB_TOKEN}
      - SPRING_PROFILES_ACTIVE=prod
    volumes:
      - msqwen-data:/data
      - msqwen-logs:/logs
volumes:
  msqwen-data:
  msqwen-logs:
```

### 11.3 .dockerignore
- [ ] Создать `.dockerignore`
- [ ] Исключить: `.git`, `target/`, `*.log`, `.idea/`, `.vscode/`

### 11.4 Конфигурация для Docker
- [ ] Создать `application-docker.yml`
- [ ] Настройка путей к БД для volumes
- [ ] Настройка логирования в файл

### 11.5 Сборка образа
- [ ] Maven плагин для сборки Docker (опционально)
- [ ] Скрипт сборки `build-docker.sh`

### 11.6 Документация
- [ ] Инструкция по запуску в Docker
- [ ] Примеры docker-compose команд
- [ ] Настройка переменных окружения

## Критерии готовности
- [ ] Dockerfile создан и работает
- [ ] java 25+ base image
- [ ] docker-compose.yml настроен
- [ ] Образ собирается успешно
- [ ] Контейнер запускается и работает
- [ ] Данные персистентны в volumes (data/, logs/)
- [ ] Все environment variables передаются
- [ ] Virtual Threads работают в Docker
- [ ] Логирование в файл настроено
