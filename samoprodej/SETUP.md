# Настройка и запуск проекта

## Требования

- **Java 21** (JDK 21)
- **Maven 3.6+** (или Maven Wrapper из проекта)
- **PostgreSQL 14+** (локально или через Docker)
- **Docker** (опционально, для запуска БД)
- **RSA ключи** для JWT-аутентификации

---

## Вариант 1: Docker Compose (рекомендуется)

### Шаг 1: Запуск PostgreSQL

```bash
cd samoprodej
docker-compose up -d db
```

PostgreSQL будет доступен на порту `5433` (проброс с контейнера).
Credentials: `devDB` / `devPass`, БД: `samoprodej`.

Опционально — Adminer (веб-интерфейс для БД):

```bash
docker-compose up -d adminer
# Открыть http://localhost:8080
# Система: PostgreSQL, Сервер: db, Пользователь: devDB, Пароль: devPass, БД: samoprodej
```

### Шаг 2: Генерация RSA ключей для JWT

JWT требует RSA ключевую пару. Сгенерируйте ключи:

```bash
# Генерация приватного ключа (PKCS#8)
openssl genpkey -algorithm RSA -out private.pem -pkeyopt rsa_keygen_bits:2048

# Извлечение публичного ключа
openssl rsa -pubout -in private.pem -out public.pem

# Кодирование в base64 (для передачи через env)
export JWT_PRIVATE_KEY=$(cat private.pem | base64)
export JWT_PUBLIC_KEY=$(cat public.pem | base64)
```

### Шаг 3: Настройка переменных окружения

JWT-свойства **не** прописаны в `application.properties` и должны быть переданы через переменные окружения:

```bash
export JWT_PRIVATE_KEY="<base64-encoded PEM>"
export JWT_PUBLIC_KEY="<base64-encoded PEM>"
export JWT_ACCESS_TOKEN_EXPIRATION=900000        # 15 минут (в мс)
export JWT_REFRESH_TOKEN_EXPIRATION=604800000    # 7 дней (в мс)
```

Или через аргументы запуска:

```bash
./mvnw spring-boot:run \
  -Dspring-boot.run.arguments="--jwt.private-key=<value> --jwt.public-key=<value> --jwt.access-token-expiration=900000 --jwt.refresh-token-expiration=604800000"
```

### Шаг 4: Запуск приложения

```bash
cd samoprodej
./mvnw spring-boot:run
```

Приложение будет доступно на `http://localhost:8082`.

### Шаг 5: Проверка

```bash
# Проверка что приложение запущено
curl http://localhost:8082/api/auth/me
# Ожидается 401 (не аутентифицирован)

# Регистрация пользователя
curl -X POST http://localhost:8082/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123","role":"TENANT"}'
```

---

## Вариант 2: Локальный PostgreSQL

### Шаг 1: Установка Java 21

#### macOS (Homebrew)
```bash
brew install openjdk@21
echo 'export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"' >> ~/.zshrc
echo 'export JAVA_HOME="/opt/homebrew/opt/openjdk@21"' >> ~/.zshrc
source ~/.zshrc
java -version
```

#### Альтернативы
- **SDKMAN**: `sdk install java 21-open`
- **Скачать**: OpenJDK или Oracle JDK с официального сайта

### Шаг 2: Настройка PostgreSQL

```bash
# macOS
brew install postgresql@14

# Запуск
pg_isready -h localhost -p 5432 || \
  /opt/homebrew/opt/postgresql@14/bin/postgres -D /opt/homebrew/var/postgresql@14 &
```

Создание пользователя и БД:

```sql
-- Подключитесь: psql postgres
CREATE USER devdb WITH PASSWORD 'devPass' CREATEDB;
CREATE DATABASE samoprodej OWNER devdb;
GRANT ALL PRIVILEGES ON DATABASE samoprodej TO devdb;

\c samoprodej
GRANT ALL ON SCHEMA public TO devdb;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO devdb;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO devdb;
\q
```

Проверка подключения:

```bash
PGPASSWORD=devPass psql -h localhost -U devdb -d samoprodej -c "SELECT 1;"
```

### Шаг 3: Изменение порта в application.properties

Для локального PostgreSQL (порт 5432 вместо Docker 5433):

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/samoprodej
spring.datasource.username=devdb
spring.datasource.password=devPass
```

### Шаг 4: Генерация JWT ключей и запуск

См. шаги 2-4 из варианта 1.

---

## Конфигурация

### application.properties (текущие значения)

```properties
# База данных (Docker Compose)
spring.datasource.url=jdbc:postgresql://localhost:5433/samoprodej
spring.datasource.username=devDB
spring.datasource.password=devPass

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Сервер
server.port=8082

# Хранение файлов
file.storage.path=uploads/properties
file.storage.max-size-photo=10485760          # 10 MB
file.storage.max-size-video=104857600         # 100 MB
file.storage.preview.max-width=800
file.storage.preview.max-height=600
file.storage.max-media-per-property=50

# Загрузка файлов
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB
```

### JWT (через env-переменные или отдельный профиль)

```properties
jwt.private-key=<RSA приватный ключ в base64>
jwt.public-key=<RSA публичный ключ в base64>
jwt.access-token-expiration=900000
jwt.refresh-token-expiration=604800000
```

### Инициализация данных (опционально)

```properties
app.user.count=10          # количество тестовых пользователей
app.property.count=50      # количество объектов недвижимости
app.listing.count=50       # количество объявлений
```

Данные засеваются при старте только если таблицы пустые.

---

## Сборка и тесты

```bash
cd samoprodej

# Полная сборка (компиляция + тесты + JAR)
./mvnw clean package

# Запуск приложения
./mvnw spring-boot:run

# Все тесты
./mvnw test

# Один тест-класс
./mvnw test -Dtest=AuthControllerTest

# Один метод теста
./mvnw test -Dtest=AuthControllerTest#login_success_returns200AndTokens
```

---

## Docker Compose — полная конфигурация

`docker-compose.yml` содержит два сервиса:

| Сервис | Порт | Описание |
|--------|------|----------|
| db | 5433:5432 | PostgreSQL (devDB/devPass, БД samoprodej) |
| adminer | 8080:8080 | Веб-интерфейс для управления БД |

```bash
docker-compose up -d db          # только PostgreSQL
docker-compose up -d adminer     # Adminer UI
docker-compose up -d             # оба сервиса
docker-compose down              # остановка
docker-compose logs db           # логи PostgreSQL
```

---

## Решение проблем

### "password authentication failed"

Проверьте credentials в `application.properties`. Для Docker — `devDB`/`devPass`, для локального PostgreSQL — `devdb`/`devPass` (регистрозависимо).

### "Connection refused" на порту 5433

```bash
docker ps  # проверить что контейнер запущен
docker-compose up -d db  # перезапустить
```

### "Connection refused" на порту 5432

```bash
pg_isready -h localhost -p 5432  # проверить PostgreSQL
# Если не запущен:
/opt/homebrew/opt/postgresql@14/bin/postgres -D /opt/homebrew/var/postgresql@14 &
```

### "release version 21 not supported"

```bash
java -version  # Должна быть 21
# Установите Java 21 (см. раздел Установка)
```

### JWT ошибки при старте

Убедитесь что переменные `jwt.private-key` и `jwt.public-key` заданы. Без них приложение не сможет генерировать/валидировать токены.

### Схема БД не создаётся

`hibernate.ddl-auto=update` — Hibernate автоматически создаёт/обновляет таблицы. Миграции (Flyway/Liquibase) не используются. Если таблицы не появились, проверьте логи подключения к БД.

---

## Структура проекта

```
samoprodej/                         # корень Maven-проекта
├── src/
│   ├── main/
│   │   ├── java/samoprodej/samoprodej/
│   │   │   ├── config/             # Security, JWT-фильтр, FileStorage, ApplicationConfig
│   │   │   │   └── initializers/   # Засевка тестовых данных
│   │   │   ├── controller/         # REST-контроллеры
│   │   │   ├── dto/                # DTO (auth, user, property, propertymedia, listing)
│   │   │   ├── entity/             # JPA-сущности
│   │   │   ├── enums/              # 14 перечислений
│   │   │   ├── mapper/             # Entity <-> DTO
│   │   │   ├── repository/         # JPA-репозитории
│   │   │   └── service/            # Бизнес-логика
│   │   └── resources/
│   │       └── application.properties
│   └── test/                       # Тесты (AuthControllerTest и др.)
├── uploads/                        # Загруженные медиа (создаётся при первой загрузке)
├── docker-compose.yml
├── pom.xml
├── mvnw / mvnw.cmd                 # Maven Wrapper
├── API_DOCUMENTATION.md
├── ARCHITECTURE.md
└── SETUP.md                        # (этот файл)
```
