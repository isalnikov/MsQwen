# Веха 4: Слой бизнес-логики (Service Layer)

## Связь с C4 моделями
- **Level 3 (Components)**: Сервисы — ключевые компоненты контейнера Spring Boot
- **Level 4 (Code)**: Методы сервисов соответствуют диаграмме кода
- **Data Flow**: Потоки данных проходят через сервисы

## Цель
Реализовать бизнес-логику приложения с соблюдением SOLID принципов и полной поддержкой CASCADE операций, изоляции данных и метрик вовлечённости согласно C4 моделям.

## Важные принципы
- **Изоляция данных**: Каждый сервис проверяет user_id перед операцией
- **Каскадное удаление**: При удалении промпта удаляются каналы, новости, результаты анализа
- **Метрики вовлечённости**: views, forwards, reactions, likes, engagement_score
- **Qwen анализ**: Передача всех метаданных в промпт для анализа
- **Кеширование**: Spring Cache с TTL для результатов анализа
- **Логирование**: `private static final Logger logger = LoggerFactory.getLogger(ClassName.class);`
- **Без System.out.println()**: Только логирование

## Задачи

### 4.1 UserService

**ВАЖНО**: Пользователь регистрируется при первом взаимодействии с ботом или через API.

- [ ] `User registerUser(Long telegramId, String username, String firstName, String lastName)`
  - Проверка на существование (если уже зарегистрирован - вернуть существующего)
  - Установка isActive = true
- [ ] `Optional<User> getUserByTelegramId(Long telegramId)`
- [ ] `User updateUser(User user)`
  - Проверка что user существует и принадлежит текущему пользователю
- [ ] `void deleteUser(Long telegramId)`
  - CASCADE: удалить промпты, каналы, новости, результаты анализа
- [ ] `void deleteAllUsers()` (admin only)
  - Очистка всей таблицы пользователей
- [ ] `long getActiveUsersCount()` - статистика

### 4.2 PromptService

**ВАЖНО**: 
- Пользователь может создать несколько промптов
- При удалении промпта каскадно удаляются: каналы, новости, результаты анализа
- Каждый промпт принадлежит только одному пользователю

- [ ] `Prompt createPrompt(Long userId, String name, String description, String promptText)`
  - Валидация входных данных
  - Проверка что имя не пустое
  - Сохранение с userId
- [ ] `List<Prompt> getAllPromptsByUser(Long userId)`
  - Возвращать только промпты текущего пользователя
- [ ] `Optional<Prompt> getPromptByIdAndUser(Long promptId, Long userId)`
  - Проверка принадлежности промпта пользователю
- [ ] `Prompt updatePrompt(Long promptId, Long userId, String name, String description, String promptText)`
  - Проверка прав, обновление только своих данных
- [ ] `void deletePrompt(Long promptId, Long userId)` (КАСКАДНО)
  - Удаляются все каналы промпта
  - Удаляются все новости каналов
  - Удаляются все результаты анализа
  - Проверка что промпт принадлежит пользователю
- [ ] `void validatePromptOwnership(Long promptId, Long userId)`
  - Выбросить AccessDeniedException если не принадлежит
- [ ] `long countByUserId(Long userId)` - статистика
- [ ] `List<Prompt> getActivePrompts(Long userId)` - только активные

### 4.3 ChannelService

**ВАЖНО**:
- Каналы принадлежат промпту и пользователю
- При удалении канала каскадно удаляются новости
- Каналы могут дублироваться между разными промптами (в рамках разных промптов)

- [ ] `Channel createChannel(Long promptId, Long userId, String name, String telegramHandle, String telegramUrl, String description)`
  - Проверка что промпт принадлежит пользователю
  - Валидация telegramHandle (формат)
  - Сохранение с promptId и userId
- [ ] `List<Channel> getAllChannelsByPrompt(Long promptId, Long userId)`
  - Возвращать только каналы текущего пользователя
- [ ] `Optional<Channel> getChannelByIdAndUser(Long channelId, Long userId)`
  - Проверка принадлежности канала пользователю
- [ ] `Channel updateChannel(Long channelId, Long userId, String name, String description)`
  - Проверка прав, обновление только своих данных
- [ ] `void deleteChannel(Long channelId, Long userId)` (КАСКАДНО)
  - Удаляются все новости канала
  - Проверка что канал принадлежит пользователю
- [ ] `void validateChannelOwnership(Long channelId, Long userId)`
  - Выбросить AccessDeniedException если не принадлежит
- [ ] `List<Channel> getActiveChannels(Long promptId, Long userId)` - только активные
- [ ] `long countByPromptIdAndUserId(Long promptId, Long userId)` - статистика

### 4.4 NewsService

**ВАЖНО**:
- Новости принадлежат каналу, промпту и пользователю
- Каждая новость имеет метрики: views, forwards, reactions, likes, engagement_score
- Новости старше 30 дней удаляются по расписанию
- При анализе news помечаются как is_analyzed = true

- [ ] `List<News> getNewsByPrompt(Long promptId, Long userId, int limit)`
  - Проверка прав, сортировка по дате публикации
- [ ] `News getNewsByIdAndUser(Long newsId, Long userId)`
  - Проверка принадлежности новости пользователю
- [ ] `void markNewsAsAnalyzed(List<Long> newsIds, Long userId)`
  - Обновить is_analyzed = true только для своих новостей
- [ ] `void deleteOldNews(LocalDateTime cutoffDate)` (очистка > 30 дней)
  - Удаление новостей старше 30 дней (все пользователи)
  - Логирование количества удалённых записей
- [ ] `void deleteAllNewsByPrompt(Long promptId, Long userId)`
  - Проверка прав
- [ ] `void deleteAllNews(Long userId)` - все новости пользователя
- [ ] `NewsStatistics getNewsStatistics(Long userId)`
  - Общее количество новостей
  - Количество проанализированных
  - Количество непроанализированных
  - Средняя engagement score
- [ ] `List<News> getUnanalyzedNews(Long promptId, Long userId)`
  - Получить новости для анализа (is_analyzed = false)
- [ ] `void saveNews(List<News> news, Long userId)`
  - Сохранение новых новостей после парсинга
  - Проверка на дубликаты (по message_id и channel_id)

### 4.5 AnalysisService

**ВАЖНО**:
- Анализ отправляет ВСЕ метаданные новости в Qwen CLI
- Результаты кешируются с TTL
- При анализе news помечаются как is_analyzed = true
- Каждый результат анализа принадлежит пользователю

- [ ] `AnalysisResult analyzeNews(Long promptId, Long userId, List<News> news)`
  - Проверка что промпт принадлежит пользователю
  - Формирование промпта с метаданными новостей
  - Отправка в Qwen CLI через QwenService
  - Сохранение результата с userId, promptId, news_ids
  - Пометить новости как is_analyzed = true
  - Отправить результат клиенту (Telegram bot)
- [ ] `Optional<AnalysisResult> getCachedAnalysis(String cacheKey)`
  - Проверка кеша перед новым анализом
  - Если кеш пуст - выполнить свежий анализ
- [ ] `void saveAnalysisResult(AnalysisResult result)`
  - Сохранение с TTL для кеша
- [ ] `List<AnalysisResult> getAnalysisHistory(Long promptId, Long userId)`
  - История аналиков по промпту
- [ ] `void deleteAnalysisByPrompt(Long promptId, Long userId)`
  - Удаление результатов анализа
- [ ] `void deleteAllAnalysis(Long userId)` - все результаты пользователя
- [ ] `String buildPromptWithContext(String promptTemplate, List<News> news)`
  - Форматирование метаданных для Qwen:
    ```
    Новость #{id}
    Заголовок: {title}
    Содержание: {content}
    Дата: {publicationDate}
    Просмотры: {viewsCount}
    Пересылки: {forwardsCount}
    Реакции: {reactionsCount}
    Лайки: {likesCount}
    Engagement Score: {engagementScore}
    Ссылка: {newsUrl}
    ```
- [ ] `String generateCacheKey(List<News> news)`
  - Генерация cache key на основе хеша новостей

### 4.6 QwenService (AI интеграция)

**ВАЖНО**:
- Интеграция через ProcessBuilder или Runtime.exec()
- Qwen CLI вызывается с полным промптом содержащим метаданные новостей
- Обработка ошибок и таймаутов

- [ ] `String sendToQwen(String promptText)` - отправка промпта в Qwen CLI
  - Вызов через ProcessBuilder
  - Чтение stdout, обработка stderr
  - Обработка таймаута
- [ ] `QwenResponse parseQwenResponse(String rawResponse)`
  - Парсинг ответа (текст или JSON)
  - Валидация ответа
- [ ] `String executeQwenCommand(String command)`
  - Выполнение команды qwen --prompt="..."
  - Логирование ошибок
- [ ] Обработка ошибок:
  - Таймаут выполнения Qwen CLI
  - Ошибки запуска процесса
  - Retry механизм при неудаче

### 4.7 CacheService

**ВАЖНО**:
- Spring Cache с @Cacheable, @CacheEvict
- TTL для кеша результатов анализа
- LRU eviction policy

- [ ] Настроить Spring Cache с @EnableCaching
- [ ] `@Cacheable(value = "analysis", key = "#cacheKey")` для кеширования анализа
- [ ] `@CacheEvict(value = "analysis", key = "#cacheKey")` для сброса кеша
- [ ] Реализовать TTL для кеша (настройка через application.yml)
- [ ] Настроить LRU eviction policy
- [ ] `void cleanupExpiredCache()` - очистка просроченных записей
- [ ] `void clearAllCache()` - полная очистка кеша

## Критерии готовности
- [ ] Все сервисы реализованы
- [ ] Внедрение зависимостей через конструктор (@Autowired только в тестах)
- [ ] Изоляция данных по user_id во всех методах
- [ ] Каскадное удаление работает корректно (тесты на удаление промпта/канала)
- [ ] Метрики вовлечённости сохраняются и используются
- [ ] Qwen анализ отправляет все метаданные в промпт
- [ ] Статус is_analyzed обновляется после анализа
- [ ] Кеширование с TTL работает
- [ ] Написаны unit тесты с Mockito (90%+ покрытие)
- [ ] Логирование через LoggerFactory во всех сервисах
- [ ] Обработка ошибок через @ControllerAdvice
- [ ] Валидация входных данных через @Valid
- [ ] REST API через RestClient (Spring 6.1+)
