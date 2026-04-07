# Веха 7: REST API контроллеры

## Связь с C4 моделями
- **Level 3 (Components)**: Контроллеры — компоненты контейнера Spring Boot
- **Level 4 (Code)**: DTO и методы соответствуют диаграмме кода
- **Sequence Diagrams**: Поток запросов от клиента через API к сервисам

## Цель
Реализовать REST API endpoints с Swagger документацией, изоляцией данных по user_id и защитой всех операций согласно C4 моделям.

## Важные принципы
- **Изоляция данных**: Каждый endpoint проверяет user_id перед операцией
- **Безопасность**: Проверка прав доступа в каждом методе
- **Валидация**: @Valid для всех входных DTO
- **Обработка ошибок**: @ControllerAdvice для централизованной обработки
- **Документация**: Swagger/OpenAPI через Springdoc
- **REST API через RestClient** (Spring 6.1+)
- **Rate limiting** для защиты API
- **Админ endpoints** требуют admin роли

## Задачи

### 7.1 Конфигурация
- [ ] Настроить Springdoc OpenAPI
- [ ] Swagger UI доступен по `/swagger-ui.html`
- [ ] API docs по `/v3/api-docs`
- [ ] Префикс всех API: `/api/v1`
- [ ] Настроить CORS для фронтенда (если нужен)
- [ ] Rate limiting конфигурация

### 7.2 UserController (`/api/v1/users`)

**ВАЖНО**: Пользователь может работать только со своими данными.

- [ ] `POST /register` - Регистрация пользователя
  - Request: `{telegramId, username, firstName, lastName}`
  - Response: UserDTO
  - Валидация @Valid
- [ ] `GET /me` - Получить текущего пользователя
  - Query param: telegramId
  - Response: UserDTO
- [ ] `PUT /me` - Обновить данные пользователя
  - Request: UserDTO
  - Проверка что пользователь существует
- [ ] `DELETE /me` - Удалить пользователя
  - CASCADE: промпты, каналы, новости, результаты анализа
  - Подтверждение через header

### 7.3 PromptController (`/api/v1/prompts`)

**ВАЖНО**: При удалении промпта каскадно удаляются каналы, новости, результаты анализа.

- [ ] `POST /` - Создать промпт
  - Request: PromptDTO `{name, description, promptText}`
  - Query param: userId
  - Response: PromptDTO
  - Валидация @Valid
- [ ] `GET /` - Список промптов пользователя
  - Query param: userId
  - Response: List<PromptDTO>
- [ ] `GET /{id}` - Получить промпт по ID
  - Проверка принадлежности пользователю
  - Response: PromptDTO
- [ ] `PUT /{id}` - Обновить промпт
  - Request: PromptDTO
  - Проверка прав
- [ ] `DELETE /{id}` - Удалить промпт (КАСКАДНО)
  - Удаляются каналы, новости, результаты анализа
  - Проверка что промпт принадлежит пользователю

### 7.4 ChannelController (`/api/v1/channels`)

**ВАЖНО**: При удалении канала каскадно удаляются новости.

- [ ] `POST /` - Добавить канал (указывается promptId)
  - Request: ChannelDTO `{name, telegramHandle, telegramUrl, description}`
  - Query params: promptId, userId
  - Response: ChannelDTO
  - Проверка что промпт принадлежит пользователю
- [ ] `GET /prompt/{promptId}` - Список каналов по промпту
  - Query param: userId
  - Response: List<ChannelDTO>
- [ ] `GET /{id}` - Получить канал по ID
  - Проверка принадлежности пользователю
- [ ] `PUT /{id}` - Обновить канал
  - Request: ChannelDTO
  - Проверка прав
- [ ] `DELETE /{id}` - Удалить канал (КАСКАДНО новости)
  - Удаляются все новости канала
  - Проверка что канал принадлежит пользователю

### 7.5 NewsController (`/api/v1/news`)

**ВАЖНО**: Новости содержат метрики вовлечённости (views, forwards, reactions, likes, engagement_score).

- [ ] `GET /prompt/{promptId}` - Новости по промпту
  - Query param: userId
  - Response: List<NewsDTO> с метриками
  - Пагинация (Pageable)
- [ ] `GET /{id}` - Получить новость по ID
  - Проверка принадлежности пользователю
  - Response: NewsDTO со всеми метриками
- [ ] `POST /parse` - Запустить парсинг вручную
  - Request: ParseRequestDTO `{promptId, userId}`
  - Асинхронная операция
- [ ] `DELETE /prompt/{promptId}` - Удалить новости промпта
  - Проверка прав
  - Каскадное удаление

### 7.6 AnalysisController (`/api/v1/analysis`)

**ВАЖНО**: Результаты кешируются, при пустом кеше выполняется свежий анализ.

- [ ] `POST /` - Запустить анализ новостей
  - Request: AnalyzeRequestDTO `{promptId, userId}`
  - Response: AnalysisResultDTO
  - Если кеш пуст - выполнить анализ через Qwen CLI
- [ ] `GET /prompt/{promptId}` - Результаты анализа по промпту
  - Query param: userId
  - Response: List<AnalysisResultDTO>
  - Пагинация
- [ ] `GET /{id}` - Получить результат по ID
  - Проверка принадлежности пользователю
- [ ] `DELETE /prompt/{promptId}` - Удалить результаты анализа
  - Проверка прав

### 7.7 AdminController (`/api/v1/admin`)

**ВАЖНО**: Эти endpoints требуют admin роли!

- [ ] `DELETE /cleanup/all` - Очистка всех таблиц (admin only)
  - News, Channels, Prompts, AnalysisResults, Users
  - Требует подтверждения через header
- [ ] `DELETE /cleanup/news` - Очистка таблицы новостей
  - Удаление всех новостей (все пользователи)
- [ ] `DELETE /cleanup/channels` - Очистка таблицы каналов
- [ ] `DELETE /cleanup/users` - Очистка пользователей
  - CASCADE: все связанные данные
- [ ] `GET /stats` - Статистика системы
  - Количество пользователей, промптов, каналов, новостей
  - Response: SystemStatsDTO

### 7.8 DTO для API

**ВАЖНО**: Использовать `record` для DTO, все поля с Javadoc.

- [ ] `UserDTO` record
  ```java
  public record UserDTO(
      Long id,
      Long telegramId,
      String username,
      String firstName,
      String lastName,
      Boolean isActive,
      LocalDateTime createdAt
  ) {}
  ```
- [ ] `PromptDTO` record
- [ ] `ChannelDTO` record
- [ ] `NewsDTO` record (со всеми метриками: views, forwards, reactions, likes, engagementScore)
- [ ] `AnalysisResultDTO` record
- [ ] Request/Response DTO для всех endpoints
- [ ] Все DTO имеют @Valid аннотации для валидации

### 7.9 Обработка ошибок
- [ ] Создать `@ControllerAdvice` для глобальной обработки
- [ ] `ResourceNotFoundException` - ресурс не найден (404)
- [ ] `AccessDeniedException` - доступ запрещен (403)
- [ ] `ValidationException` - ошибка валидации (400)
- [ ] `GlobalExceptionHandler` - обработка и логирование
- [ ] Error response DTO:
  ```java
  public record ErrorResponseDTO(
      Integer status,
      String error,
      String message,
      LocalDateTime timestamp
  ) {}
  ```

### 7.10 Безопасность API
- [ ] Проверка прав доступа в каждом методе (userId)
- [ ] Валидация input данных через `@Valid`
- [ ] Rate limiting (настройка через application.yml)
- [ ] Защита от SQL инъекций (JPA защищает)
- [ ] Защита от XSS (валидация входных данных)
- [ ] CORS настройка (если нужен фронтенд)

## Критерии готовности
- [ ] Все endpoints реализованы
- [ ] Swagger документация доступна и актуальна
- [ ] Обработка ошибок централизована
- [ ] Input валидация работает через @Valid
- [ ] Integration тесты на контроллеры (90%+ покрытие)
- [ ] Изоляция данных по user_id работает
- [ ] CASCADE удаление работает корректно
- [ ] Rate limiting настроен
- [ ] Admin endpoints требуют admin роли
- [ ] Все DTO созданы как records с Javadoc
