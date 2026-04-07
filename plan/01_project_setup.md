# Веха 1: Настройка проекта и инфраструктуры

## Связь с C4 моделями
- **Level 2 (Containers)**: Структура проекта соответствует контейнерной диаграмме
- **Level 3 (Components)**: Пакетная организация следует компонентной диаграмме

## Цель
Создать базовую структуру проекта, настроить сборку и зависимости согласно C4 моделям.

## Задачи

### 1.1 Maven конфигурация
- [ ] Создать `pom.xml` с зависимостями:
  - Spring Boot 4.x (starter-web, starter-data-jpa, starter-cache, starter-validation)
  - H2 Database
  - Jsoup (парсинг HTML)
  - Jackson (JSON сериализация, ObjectMapper для сериализации)
  - Liquibase (миграции БД)
  - Spring Boot Starter Test (JUnit 5, Mockito)
  - Springdoc OpenAPI (Swagger документация)
  - Spring Boot Starter Cache
  - Java Telegram Bot API (или ручной REST клиент)
  - RestClient (Spring 6.1+, встроен в Spring Boot 4)
- [ ] Настроить Java 25+ в `pom.xml` (использовать современные фичи)
- [ ] Настроить jacoco плагин для покрытия тестами
- [ ] **ВАЖНО**: НЕ использовать Lombok - пишем чистый и понятный код
- [ ] Использовать только стандартные аннотации Java и Spring

### 1.2 Структура проекта
```
src/main/java/com/isalnikov/msqwen/
├── bot/
├── cli/
├── config/
├── controller/
├── dto/
├── entity/
├── repository/
├── service/
└── MsQwenApplication.java
```

### 1.3 Конфигурация
- [ ] Создать `application.yml` с базовыми настройками
- [ ] Настроить профили (dev, prod)
- [ ] Настроить переменные окружения
- [ ] Настроить логирование (консоль + файл с ротацией)
- [ ] Включить использование современных фич Java:
  - Virtual Threads (где уместно)
  - Pattern Matching for instanceof
  - Sequenced Collections
  - Switch expressions
  - Text blocks
  - Record классы для DTO

### 1.4 Git настройка
- [ ] Создать `.gitignore` (исключить data/, logs/, target/)
- [ ] Инициализировать git репозиторий

## Критерии готовности
- [ ] Проект собирается через `mvn clean compile`
- [ ] Структура папок создана
- [ ] Git репозиторий инициализирован
- [ ] Java 25+ настроена
- [ ] Spring Boot 4.x настроен
- [ ] Lombok НЕ используется
- [ ] Современные фичи Java доступны для использования

## Важные замечания по коду
- Использовать `record` для DTO, `class` для JPA-сущностей
- Внедрение зависимостей через конструктор (final поля)
- Логирование: `private static final Logger logger = LoggerFactory.getLogger(ClassName.class);`
- НЕ использовать System.out.println()
- @Autowired только в тестах, в основном коде — внедрение через конструктор
- Каждый класс должен иметь одну ответственность
- Код должен быть качественно комментирован (язык русский), Javadoc 100%
