# Веха 3: Слой репозиториев (Data Access Layer)

## Связь с C4 моделями
- **Level 3 (Components)**: Репозитории — компоненты контейнера Spring Boot
- **Level 4 (Code)**: Методы репозиториев соответствуют диаграмме кода

## Цель
Реализовать Spring Data JPA репозитории для доступа к данным с правильной изоляцией по user_id и поддержкой CASCADE операций согласно C4 моделям.

## Важные принципы
- **Изоляция данных**: Все запросы должны фильтроваться по user_id
- **Безопасность**: Проверка прав доступа на уровне репозитория
- **Оптимизация**: Использовать индексы, избегать N+1 запросов
- **Без SQL инъекций**: JPA защищает, но нужно правильно писать @Query

## Задачи

### 3.1 Репозитории

#### UserRepository
- [ ] `Optional<User> findByTelegramId(Long telegramId)`
- [ ] `User save(User user)`
- [ ] `void deleteAll()` (для админской очистки)
- [ ] `List<User> findAllByIsActive(Boolean isActive)`
- [ ] `boolean existsByTelegramId(Long telegramId)`

#### PromptRepository
- [ ] `List<Prompt> findAllByUserId(Long userId)`
- [ ] `Optional<Prompt> findByIdAndUserId(Long id, Long userId)`
- [ ] `void deleteByIdAndUserId(Long id, Long userId)` (CASCADE: каналы, новости, analysis)
- [ ] `boolean existsByIdAndUserId(Long id, Long userId)`
- [ ] `long countByUserId(Long userId)`
- [ ] `List<Prompt> findAllByUserIdAndIsActive(Long userId, Boolean isActive)`

#### ChannelRepository
- [ ] `List<Channel> findAllByPromptIdAndUserId(Long promptId, Long userId)`
- [ ] `List<Channel> findAllByPromptIdAndIsActiveAndUserId(Long promptId, Boolean isActive, Long userId)`
- [ ] `Optional<Channel> findByIdAndPromptIdAndUserId(Long id, Long promptId, Long userId)`
- [ ] `void deleteByIdAndPromptIdAndUserId(Long id, Long promptId, Long userId)` (CASCADE: новости)
- [ ] `boolean existsByIdAndUserId(Long id, Long userId)` - для проверки прав
- [ ] `long countByPromptIdAndUserId(Long promptId, Long userId)`
- [ ] `List<Channel> findAllByUserId(Long userId)` - все каналы пользователя

#### NewsRepository
- [ ] `List<News> findAllByChannelIdAndIsAnalyzedAndUserId(Long channelId, Boolean isAnalyzed, Long userId)`
- [ ] `List<News> findAllByPromptIdAndIsAnalyzedAndUserId(Long promptId, Boolean isAnalyzed, Long userId)`
- [ ] `List<News> findAllByPublicationDateBefore(LocalDateTime date)` (для очистки старых > 30 дней)
- [ ] `void deleteAllByChannelIdAndUserId(Long channelId, Long userId)`
- [ ] `void deleteAllByPromptIdAndUserId(Long promptId, Long userId)`
- [ ] `void deleteAllByUserId(Long userId)` - все новости пользователя
- [ ] `long countByPromptIdAndUserId(Long promptId, Long userId)` (для статистики)
- [ ] `long countByUserId(Long userId)` - общее количество новостей
- [ ] `long countByIsAnalyzedAndUserId(Boolean isAnalyzed, Long userId)`
- [ ] `List<News> findAllByUserIdOrderByPublicationDateDesc(Long userId, Pageable pageable)` - с пагинацией
- [ ] `List<News> findAllByPromptIdAndUserIdOrderByPublicationDateDesc(Long promptId, Long userId, Pageable pageable)`

#### AnalysisResultRepository
- [ ] `Optional<AnalysisResult> findByCacheKey(String cacheKey)`
- [ ] `AnalysisResult save(AnalysisResult result)`
- [ ] `void deleteAllByPromptIdAndUserId(Long promptId, Long userId)`
- [ ] `List<AnalysisResult> findAllByPromptIdAndUserIdOrderByCreatedAtDesc(Long promptId, Long userId)`
- [ ] `List<AnalysisResult> findAllByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable)`
- [ ] `long countByPromptIdAndUserId(Long promptId, Long userId)`
- [ ] `void deleteAllByUserId(Long userId)` - все результаты анализа пользователя
- [ ] `void deleteByCacheExpiresAtBefore(LocalDateTime date)` - очистка просроченного кеша

### 3.2 Кастомные запросы
- [ ] Реализовать @Query для сложных запросов с несколькими JOIN
- [ ] Добавить @Modifying для update/delete запросов
- [ ] Настроить @Transactional для операций записи
- [ ] Использовать @EntityGraph для предотвращения N+1 запросов
- [ ] Оптимизировать запросы с индексами (WHERE user_id, WHERE prompt_id)
- [ ] Использовать Pageable для пагинации результатов

### 3.3 Проверка прав доступа
- [ ] Все методы репозиториев принимают userId для проверки прав
- [ ] При удалении сущности проверять принадлежность пользователю
- [ ] Не возвращать данные чужих пользователей
- [ ] Для админских операций - отдельные методы без фильтрации по userId

## Критерии готовности
- [ ] Все репозитории созданы
- [ ] Методы для изоляции данных по user_id реализованы
- [ ] Все запросы фильтруются по userId (кроме админских)
- [ ] Написаны unit тесты на репозитории (с @DataJpaTest)
- [ ] Покрытие тестами 90%+
- [ ] CASCADE операции работают корректно
- [ ] Нет N+1 запросов (используется @EntityGraph)
- [ ] Пагинация работает для больших выборок
