# Веха 5: Парсер Telegram каналов

## Связь с C4 моделями
- **Level 3 (Components)**: Парсер — компонент контейнера Spring Boot
- **Level 4 (Code)**: Классы парсера соответствуют диаграмме кода
- **Data Flow**: Поток данных от t.me/s/ до H2 Database

## Цель
Реализовать парсинг новостей из публичных Telegram каналов через t.me/s/ с извлечением всех метаданных для последующего анализа согласно C4 моделям.

## Важные принципы
- **Метрики вовлечённости**: views, forwards, reactions, likes, engagement_score
- **Engagement Score**: формула `(views * 0.1 + forwards * 0.3 + reactions * 0.6) / 100`, нормализация (0.0 - 1.0)
- **Все метаданные передаются в Qwen** для анализа
- **Обработка ошибок**: недоступность канала, изменения структуры t.me/s/
- **Jsoup** для парсинга HTML

## Задачи

### 5.1 TelegramParser
- [ ] Создать класс `TelegramParser` с использованием Jsoup
- [ ] Реализовать метод `parseChannel(String channelUrl)`
- [ ] Парсинг списка последних новостей из канала

### 5.2 Парсинг метаданных новости

**ВАЖНО**: Все эти метаданные передаются в Qwen для анализа!

- [ ] `parseMessageId()` - ID сообщения
- [ ] `parseTitle()` - Заголовок (если есть)
- [ ] `parseContent()` - Текст новости
- [ ] `parsePublicationDate()` - Дата публикации
- [ ] `parseViewsCount()` - Количество просмотров (views)
- [ ] `parseForwardsCount()` - Количество пересылок (forwards)
- [ ] `parseReactionsCount()` - Количество реакций
- [ ] `parseLikesCount()` - Количество лайков
- [ ] `parseNewsUrl()` - Прямая ссылка на новость
- [ ] `parseEngagementScore()` - Рассчитать по формуле

### 5.3 Engagement Score

**Формула**: `(views * 0.1 + forwards * 0.3 + reactions * 0.6) / 100`

- [ ] Реализовать алгоритм расчета `engagement_score`
- [ ] Нормализация значения (0.0 - 1.0)
- [ ] Документировать формулу в коде (JavaDoc)
- [ ] Новости с более высокими метриками получают приоритет

**Примечание**: Engagement Score используется для приоритизации новостей при анализе.

### 5.4 News DTO для парсинга
- [ ] Создать `ParsedNews` record для временного хранения
  ```java
  public record ParsedNews(
      Long messageId,
      String title,
      String content,
      LocalDateTime publicationDate,
      Integer viewsCount,
      Integer forwardsCount,
      Integer reactionsCount,
      Integer likesCount,
      Double engagementScore,
      String newsUrl
  ) {}
  ```
- [ ] Конвертация `ParsedNews` → `News` entity
- [ ] Проверка на дубликаты перед сохранением (по messageId + channelId)

### 5.5 Обработка ошибок парсинга
- [ ] Обработка недоступности канала
- [ ] Обработка изменений структуры t.me/s/
- [ ] Логирование ошибок парсинга
- [ ] Retry механизм (опционально)
- [ ] Пропуск новостей с некорректными данными
- [ ] Логирование количества распарсенных новостей

### 5.6 Тесты парсера
- [ ] Unit тесты на парсинг HTML (mock Jsoup)
- [ ] Integration тесты с реальными каналами (опционально)
- [ ] Тесты на обработку ошибок
- [ ] Тесты на расчет engagement_score
- [ ] Тесты на конвертацию ParsedNews → News
- [ ] Тесты на проверку дубликатов

## Критерии готовности
- [ ] Парсер извлекает все метаданные из новостей (views, forwards, reactions, likes)
- [ ] Engagement score рассчитывается корректно по формуле
- [ ] Обработка ошибок реализована
- [ ] Покрытие тестами 90%+
- [ ] Все метаданные передаются в Qwen для анализа
- [ ] Дубликаты новостей проверяются перед сохранением
