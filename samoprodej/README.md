# Samoprodej Backend

Spring Boot приложение для управления недвижимостью и объявлениями об аренде.

## Технологии

- **Java 21**
- **Spring Boot 4.0.2**
- **Spring Data JPA**
- **PostgreSQL 14+**
- **Maven**
- **Lombok**

## Быстрый старт

1. Установите Java 21 и PostgreSQL
2. Настройте базу данных (см. [SETUP.md](SETUP.md))
3. Запустите проект:
   ```bash
   ./mvnw clean spring-boot:run
   ```

Приложение будет доступно по адресу: `http://localhost:8082`

## Документация

- **[SETUP.md](SETUP.md)** - Подробная инструкция по настройке и запуску проекта локально
- **[API_DOCUMENTATION.md](API_DOCUMENTATION.md)** - Полная документация по API (User, Property, Listing)

## Основные компоненты

### API Endpoints

- **User API**: `/api/users` - управление пользователями
- **Property API**: `/api/properties` - управление недвижимостью
- **Listing API**: `/api/listings` - управление объявлениями об аренде

### Архитектура

- **Controllers** - REST API эндпоинты
- **Services** - бизнес-логика с транзакциями
- **DTOs** - Java records для передачи данных
- **Mappers** - преобразование Entity ↔ DTO
- **Repositories** - доступ к данным через JPA

## Требования

- Java 21
- PostgreSQL 14+ (или Docker)
- Maven 3.6+ (или используйте Maven Wrapper)

## Лицензия

[Укажите лицензию проекта]
