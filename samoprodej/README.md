# Samoprodej Backend

Backend для чешской платформы аренды недвижимости. REST API на Spring Boot с JWT-аутентификацией.

## Технологии

- **Java 21**
- **Spring Boot 4.0.2**
- **Spring Security** — JWT (RS256) + refresh-токены
- **Spring Data JPA** — Hibernate, PostgreSQL
- **Maven** (Maven Wrapper включён)
- **Lombok**
- **Thumbnailator** — генерация превью фото
- **Docker Compose** — PostgreSQL + Adminer

## Быстрый старт

```bash
# 1. Запустить PostgreSQL
cd samoprodej
docker-compose up -d db

# 2. Настроить JWT-ключи (см. SETUP.md)

# 3. Запустить приложение
./mvnw spring-boot:run
```

Приложение: `http://localhost:8082`
CORS разрешён для `http://localhost:3000` (фронтенд).

## Документация

| Файл | Описание |
|------|----------|
| [SETUP.md](SETUP.md) | Настройка окружения, БД, JWT-ключи, запуск |
| [API_DOCUMENTATION.md](API_DOCUMENTATION.md) | Полная документация API (Auth, Users, Properties, Media, Listings) |
| [ARCHITECTURE.md](ARCHITECTURE.md) | Архитектура, сущности, паттерны, руководство по разработке |

## API

| Модуль | Базовый путь | Эндпоинтов | Описание |
|--------|--------------|------------|----------|
| Auth | `/api/auth` | 5 | Регистрация, логин, refresh, logout, текущий пользователь |
| Users | `/api/users` | 11 | CRUD, поиск, смена пароля, активация/блокировка |
| Properties | `/api/properties` | 22 | CRUD, поиск по городу, управление медиа (загрузка, URL, сортировка) |
| Listings | `/api/listings` | 13 | CRUD, публикация/снятие/архивирование, поиск |

**Публичные эндпоинты**: `/api/auth/**`, `/api/public/**`. Все остальные требуют JWT в заголовке `Authorization: Bearer <token>`.

## Аутентификация

- **Access token** — JWT (RS256), короткоживущий, передаётся в `Authorization: Bearer`
- **Refresh token** — случайный токен, хранится как SHA-256 хэш в БД, передаётся через httpOnly cookie `refresh_token`
- **Ротация токенов** — при refresh старый токен отзывается, выдаётся новый. Повторное использование отозванного токена отзывает все токены пользователя

## Сборка и тесты

```bash
cd samoprodej

./mvnw clean package                    # сборка + тесты + JAR
./mvnw test                             # все тесты
./mvnw test -Dtest=AuthControllerTest   # один тест-класс
```

## Требования

- Java 21
- PostgreSQL 14+ (или Docker)
- RSA ключи для JWT (см. [SETUP.md](SETUP.md))
