# Инструкция по локальной настройке проекта

## Требования

- **Java 21** (JDK 21)
- **Maven 3.6+** (или используйте Maven Wrapper из проекта)
- **PostgreSQL 14+** (локально или через Docker)
- **Docker** (опционально, для запуска БД через docker-compose)

---

## Вариант 1: Запуск с локальным PostgreSQL (рекомендуется)

### Шаг 1: Установка Java 21

#### macOS (через Homebrew)
```bash
brew install openjdk@21

# Добавьте в ~/.zshrc или ~/.bash_profile
echo 'export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"' >> ~/.zshrc
echo 'export JAVA_HOME="/opt/homebrew/opt/openjdk@21"' >> ~/.zshrc
source ~/.zshrc

# Проверьте версию
java -version
```

#### Альтернативные способы установки
- **SDKMAN**: `sdk install java 21-open`
- **Oracle/OpenJDK**: Скачайте с официального сайта

### Шаг 2: Установка PostgreSQL

#### macOS (через Homebrew)
```bash
brew install postgresql@14

# Запустите PostgreSQL вручную (если brew services не работает)
/opt/homebrew/opt/postgresql@14/bin/postgres -D /opt/homebrew/var/postgresql@14 > /tmp/postgres.log 2>&1 &
```

#### Проверка запуска PostgreSQL
```bash
pg_isready -h localhost -p 5432
# Должно показать: localhost:5432 - accepting connections
```

### Шаг 3: Настройка базы данных

```bash
# Подключитесь к PostgreSQL
psql postgres

# В psql выполните:
CREATE USER devdb WITH PASSWORD 'devPass' CREATEDB;
CREATE DATABASE samoprodej OWNER devdb;
GRANT ALL PRIVILEGES ON DATABASE samoprodej TO devdb;

# Подключитесь к базе данных и дайте права на схему
\c samoprodej
GRANT ALL ON SCHEMA public TO devdb;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO devdb;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO devdb;

# Выйдите
\q
```

### Шаг 4: Проверка подключения

```bash
# Проверьте подключение с паролем
PGPASSWORD=devPass psql -h localhost -U devdb -d samoprodej -c "SELECT 1;"
```

Если команда выполнилась успешно, база данных настроена правильно.

### Шаг 5: Настройка application.properties

Убедитесь, что файл `src/main/resources/application.properties` содержит:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/samoprodej
spring.datasource.username=devdb
spring.datasource.password=devPass

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

server.port=8082
spring.application.name=samoprodej
```

### Шаг 6: Запуск проекта

```bash
# Перейдите в директорию проекта
cd /path/to/samoprodej

# Запустите проект
./mvnw clean spring-boot:run

# Или если установлен Maven глобально
mvn clean spring-boot:run
```

### Шаг 7: Проверка работы

После запуска приложение будет доступно по адресу:
```
http://localhost:8082
```

Проверьте API:
```bash
curl http://localhost:8082/api/users
```

---

## Вариант 2: Запуск с Docker Compose

### Шаг 1: Установка Docker

Установите Docker Desktop для вашей ОС:
- macOS: https://www.docker.com/products/docker-desktop
- Linux: `sudo apt-get install docker.io docker-compose`
- Windows: Docker Desktop

### Шаг 2: Запуск базы данных через Docker

```bash
# Перейдите в директорию проекта
cd /path/to/samoprodej

# Запустите PostgreSQL через Docker Compose
docker-compose up -d db

# Проверьте статус контейнера
docker ps
```

### Шаг 3: Настройка application.properties для Docker

Измените `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/samoprodej
spring.datasource.username=devDB
spring.datasource.password=devPass

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

server.port=8082
spring.application.name=samoprodej
```

**Важно:** Docker использует порт `5433`, а локальный PostgreSQL - `5432`.

### Шаг 4: Запуск проекта

```bash
./mvnw clean spring-boot:run
```

### Шаг 5: Остановка базы данных

```bash
# Остановить контейнер
docker-compose down

# Или остановить только БД
docker-compose stop db
```

---

## Быстрый старт (одной командой)

### Для локального PostgreSQL:

```bash
# Убедитесь, что PostgreSQL запущен
pg_isready -h localhost -p 5432 || /opt/homebrew/opt/postgresql@14/bin/postgres -D /opt/homebrew/var/postgresql@14 > /tmp/postgres.log 2>&1 &

# Запустите проект
cd /path/to/samoprodej && ./mvnw clean spring-boot:run
```

### Для Docker:

```bash
cd /path/to/samoprodej && docker-compose up -d db && ./mvnw clean spring-boot:run
```

---

## Решение проблем

### Проблема: "password authentication failed"

**Решение:**
1. Проверьте, что пользователь `devdb` существует и пароль правильный
2. Пересоздайте пользователя:
   ```bash
   psql postgres
   ALTER USER devdb WITH PASSWORD 'devPass';
   \q
   ```

### Проблема: "Connection refused" или "Port 5432 not accepting connections"

**Решение:**
1. Убедитесь, что PostgreSQL запущен:
   ```bash
   pg_isready -h localhost -p 5432
   ```
2. Если не запущен, запустите вручную:
   ```bash
   /opt/homebrew/opt/postgresql@14/bin/postgres -D /opt/homebrew/var/postgresql@14 &
   ```

### Проблема: "release version 21 not supported"

**Решение:**
1. Проверьте версию Java:
   ```bash
   java -version
   ```
2. Установите Java 21 (см. Шаг 1)

### Проблема: "brew services start postgresql@14" не работает

**Решение:**
Запустите PostgreSQL вручную:
```bash
/opt/homebrew/opt/postgresql@14/bin/postgres -D /opt/homebrew/var/postgresql@14 > /tmp/postgres.log 2>&1 &
```

### Проблема: Docker не запускается

**Решение:**
1. Убедитесь, что Docker Desktop запущен
2. Проверьте статус:
   ```bash
   docker ps
   ```
3. Если ошибка подключения к Docker daemon, перезапустите Docker Desktop

---

## Полезные команды

### PostgreSQL

```bash
# Подключиться к базе данных
psql -h localhost -U devdb -d samoprodej

# Показать все таблицы
\dt

# Показать структуру таблицы
\d users

# Выйти
\q
```

### Maven

```bash
# Очистить и собрать проект
./mvnw clean package

# Запустить тесты
./mvnw test

# Запустить приложение без очистки
./mvnw spring-boot:run
```

### Docker

```bash
# Просмотр логов PostgreSQL
docker-compose logs db

# Перезапуск контейнера БД
docker-compose restart db

# Остановка всех контейнеров
docker-compose down
```

---

## Структура проекта

```
samoprodej/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── samoprodej/samoprodej/
│   │   │       ├── controller/     # REST контроллеры
│   │   │       ├── service/        # Бизнес-логика
│   │   │       ├── dto/           # Data Transfer Objects
│   │   │       ├── mapper/        # Преобразование Entity ↔ DTO
│   │   │       ├── Entity/       # JPA сущности
│   │   │       ├── Repository/   # JPA репозитории
│   │   │       └── enums/         # Перечисления
│   │   └── resources/
│   │       └── application.properties  # Конфигурация
│   └── test/                      # Тесты
├── docker-compose.yml             # Конфигурация Docker
├── pom.xml                        # Maven конфигурация
└── mvnw                           # Maven Wrapper
```

---

## Настройка для разных сред

### Разработка (Development)

Используйте настройки из `application.properties`:
- `ddl-auto=update` - автоматическое обновление схемы БД
- `show-sql=true` - показывать SQL запросы в логах

### Продакшн (Production)

Создайте `application-prod.properties`:
```properties
spring.datasource.url=jdbc:postgresql://prod-host:5432/samoprodej
spring.datasource.username=prod_user
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

server.port=8080
```

Запуск с профилем:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

---

## Дополнительные инструменты

### Adminer (веб-интерфейс для БД)

Если используете Docker Compose, Adminer доступен по адресу:
```
http://localhost:8080
```

Параметры подключения:
- **Система**: PostgreSQL
- **Сервер**: db (для Docker) или localhost (для локального)
- **Пользователь**: devDB (Docker) или devdb (локальный)
- **Пароль**: devPass
- **База данных**: samoprodej

---

## Следующие шаги

После успешного запуска проекта:

1. Изучите API документацию: `API_DOCUMENTATION.md`
2. Протестируйте эндпоинты через Postman или curl
3. Проверьте созданные тестовые данные (пользователи создаются автоматически при старте)

---

## Контакты и поддержка

При возникновении проблем:
1. Проверьте логи приложения в консоли
2. Проверьте логи PostgreSQL: `/tmp/postgres.log` (для локального запуска)
3. Убедитесь, что все зависимости установлены и версии соответствуют требованиям
