# API Документация

## Обзор

API построено на Spring Boot MVC архитектуре с использованием следующих компонентов:
- **DTO (Data Transfer Objects)** - Java records для передачи данных
- **Services** - бизнес-логика с транзакциями
- **Controllers** - REST API эндпоинты
- **Mappers** - преобразование между Entity и DTO
- **Repositories** - доступ к данным через JPA

## Базовый URL

```
http://localhost:8082/api
```

## Общие принципы

### HTTP Методы
- `GET` - получение данных (read-only операции)
- `POST` - создание новых ресурсов
- `PUT` - полное обновление ресурса
- `PATCH` - частичное обновление ресурса
- `DELETE` - удаление ресурса (soft delete)

### Коды ответов
- `200 OK` - успешный запрос
- `201 Created` - ресурс создан
- `204 No Content` - успешное удаление
- `400 Bad Request` - ошибка валидации или некорректные данные
- `404 Not Found` - ресурс не найден
- `500 Internal Server Error` - внутренняя ошибка сервера

### Формат данных
Все запросы и ответы используют JSON формат.

---

## User API

### Базовый путь: `/api/users`

#### 1. Создание пользователя
```http
POST /api/users
Content-Type: application/json

{
  "email": "user@example.com",
  "phone": "+420123456789",
  "password": "password123",
  "firstName": "Иван",
  "lastName": "Иванов",
  "role": "TENANT",
  "authProvider": "LOCAL",
  "preferredLang": "CS"
}
```

**Валидация:**
- `email` - обязательное, валидный email, максимум 255 символов
- `password` - обязательное, минимум 6 символов, максимум 100
- `phone` - опциональное, максимум 32 символа
- `firstName`, `lastName` - опциональные, максимум 64 символа
- `role` - обязательное: `TENANT`, `OWNER`, `ADMIN`
- `authProvider` - обязательное: `LOCAL`, `BANKID`, `NEOID`
- `preferredLang` - опциональное: `CS`, `UA`, `EN`, `RU`

**Ответ:** `200 OK` с `UserResponse`

#### 2. Получение всех пользователей
```http
GET /api/users
```

**Ответ:** `200 OK` со списком `UserResponse[]`

#### 3. Получение пользователя по ID
```http
GET /api/users/{id}
```

**Ответ:** `200 OK` с `UserResponse` или `404 Not Found`

#### 4. Полное обновление пользователя (PUT)
```http
PUT /api/users/{id}
Content-Type: application/json

{
  "email": "newemail@example.com",
  "phone": "+420987654321",
  "firstName": "Петр",
  "lastName": "Петров",
  "preferredLang": "EN",
  "avatarUrl": "https://example.com/avatar.jpg"
}
```

**Валидация:**
- Все поля опциональные
- `email` - валидный email, если указан
- `phone` - максимум 32 символа
- `firstName`, `lastName` - максимум 64 символа

**Ответ:** `200 OK` с обновленным `UserResponse` или `404 Not Found`

#### 5. Частичное обновление пользователя (PATCH)
```http
PATCH /api/users/{id}
Content-Type: application/json

{
  "firstName": "Новое имя"
}
```

**Ответ:** `200 OK` с обновленным `UserResponse` или `404 Not Found`

#### 6. Удаление пользователя (Soft Delete)
```http
DELETE /api/users/{id}
```

**Ответ:** `204 No Content` или `404 Not Found`

#### 7. Активация пользователя
```http
POST /api/users/{id}/activate
```

**Ответ:** `200 OK` с `UserResponse` (статус изменен на `ACTIVE`)

#### 8. Блокировка пользователя
```http
POST /api/users/{id}/block
```

**Ответ:** `200 OK` с `UserResponse` (статус изменен на `BLOCKED`)

#### 9. Смена пароля
```http
POST /api/users/{id}/change-password
Content-Type: application/json

{
  "oldPassword": "старый_пароль",
  "newPassword": "новый_пароль123"
}
```

**Валидация:**
- `oldPassword` - обязательное
- `newPassword` - обязательное, минимум 6 символов, максимум 100

**Ответ:** `200 OK` или `400 Bad Request`

#### 10. Поиск по email
```http
GET /api/users/search/email?email=user@example.com
```

**Ответ:** `200 OK` с `UserResponse` или `404 Not Found`

#### 11. Поиск по телефону
```http
GET /api/users/search/phone?phone=+420123456789
```

**Ответ:** `200 OK` с `UserResponse` или `404 Not Found`

### UserResponse структура
```json
{
  "id": "uuid",
  "email": "user@example.com",
  "phone": "+420123456789",
  "firstName": "Иван",
  "lastName": "Иванов",
  "role": "TENANT",
  "status": "ACTIVE",
  "authProvider": "LOCAL",
  "preferredLang": "CS",
  "avatarUrl": "https://example.com/avatar.jpg",
  "createdAt": "2026-02-04T10:00:00",
  "updatedAt": "2026-02-04T10:00:00",
  "lastLoginAt": "2026-02-04T10:00:00"
}
```

---

## Property API

### Базовый путь: `/api/properties`

#### 1. Создание недвижимости
```http
POST /api/properties
Content-Type: application/json

{
  "ownerUserId": "uuid",
  "type": "APARTMENT",
  "country": "CZ",
  "city": "Прага",
  "district": "Прага 1",
  "street": "Вацлавская площадь",
  "houseNumber": "1",
  "addressText": "Вацлавская площадь 1, Прага 1",
  "lat": 50.0833,
  "lng": 14.4167,
  "dispozice": "2+kk",
  "roomsCount": 2,
  "floor": 3,
  "totalFloors": 5,
  "areaM2": 45.5,
  "balconyAreaM2": 5.0,
  "cellarAreaM2": 3.0,
  "hasBalcony": true,
  "hasTerrace": false,
  "hasLoggia": false,
  "hasGarden": false,
  "hasCellar": true,
  "hasElevator": true,
  "parkingType": "GARAGE"
}
```

**Валидация:**
- `ownerUserId` - обязательное, UUID существующего пользователя
- `type` - обязательное: `APARTMENT`, `HOUSE`, `ROOM`, `COMMERCIAL`, `LAND`, `OTHER`
- `city` - обязательное, максимум 128 символов
- `areaM2` - обязательное, положительное число
- `country` - опциональное, по умолчанию "CZ"
- `lat` - опциональное, диапазон -90.0 до 90.0
- `lng` - опциональное, диапазон -180.0 до 180.0
- `roomsCount`, `totalFloors` - опциональные, положительные числа
- `balconyAreaM2`, `cellarAreaM2` - опциональные, положительные числа
- `parkingType` - опциональное: `NONE`, `STREET`, `GARAGE`, `GARAGE_SPACE`, `PRIVATE_SPOT`

**Ответ:** `200 OK` с `PropertyResponse` или `400 Bad Request`

#### 2. Получение всех недвижимостей
```http
GET /api/properties
```

**Ответ:** `200 OK` со списком `PropertyResponse[]`

#### 3. Получение недвижимости по ID
```http
GET /api/properties/{id}
```

**Ответ:** `200 OK` с `PropertyResponse` или `404 Not Found`

#### 4. Поиск по городу
```http
GET /api/properties/search?city=Прага
```

**Ответ:** `200 OK` со списком `PropertyResponse[]`

#### 5. Обновление недвижимости (PUT)
```http
PUT /api/properties/{id}
Content-Type: application/json

{
  "city": "Брно",
  "areaM2": 50.0,
  "hasBalcony": false
}
```

**Валидация:** Все поля опциональные, но с валидацией если указаны

**Ответ:** `200 OK` с обновленным `PropertyResponse` или `404 Not Found`

#### 6. Частичное обновление (PATCH)
```http
PATCH /api/properties/{id}
Content-Type: application/json

{
  "city": "Острава"
}
```

**Ответ:** `200 OK` с обновленным `PropertyResponse` или `404 Not Found`

#### 7. Удаление недвижимости (Soft Delete)
```http
DELETE /api/properties/{id}
```

**Ответ:** `204 No Content` или `404 Not Found`

---

## Property Media API

Медиа (фото и видео) привязаны к недвижимости. Поддерживается загрузка файлов и добавление по URL.

### Базовый путь: `/api/properties/{id}/media`

#### 1. Загрузка файла (Multipart)
```http
POST /api/properties/{id}/media/upload
Content-Type: multipart/form-data

file: (binary)
type: PHOTO | VIDEO
```

**Параметры:**
- `file` - файл (обязательный). Фото: до 10MB, видео: до 100MB (настраивается в application.properties)
- `type` - обязательный: `PHOTO`, `VIDEO`

**Ответ:** `200 OK` с `PropertyMediaResponse` или `404 Not Found`

**Примечание:** Для фото автоматически генерируется превью (max 800x600).

#### 2. Добавление медиа по URL
```http
POST /api/properties/{id}/media
Content-Type: application/json

{
  "type": "PHOTO",
  "url": "https://example.com/photo.jpg",
  "previewUrl": "https://example.com/preview.jpg",
  "sortOrder": 0
}
```

**Валидация:**
- `type` - обязательное: `PHOTO`, `VIDEO`
- `url` - обязательное
- `previewUrl` - опциональное
- `sortOrder` - опциональное, >= 0 (если не указан - следующий по порядку)

**Ответ:** `200 OK` с `PropertyMediaResponse` или `404 Not Found`

#### 3. Получение медиа недвижимости
```http
GET /api/properties/{id}/media
```

**Ответ:** `200 OK` со списком `PropertyMediaResponse[]` (отсортированы по sortOrder)

#### 4. Изменение порядка медиа
```http
PUT /api/properties/{id}/media/reorder
Content-Type: application/json

{
  "mediaIds": ["uuid1", "uuid2", "uuid3"]
}
```

**Валидация:**
- `mediaIds` - непустой список UUID всех медиа данной недвижимости в желаемом порядке

**Ответ:** `200 OK` со списком `PropertyMediaResponse[]` или `400 Bad Request`

#### 5. Обновление медиа (previewUrl, sortOrder)
```http
PATCH /api/properties/{id}/media/{mediaId}
Content-Type: application/json

{
  "previewUrl": "https://example.com/new-preview.jpg",
  "sortOrder": 1
}
```

**Валидация:** Оба поля опциональные. `sortOrder` >= 0.

**Ответ:** `200 OK` с `PropertyMediaResponse` или `404 Not Found`

#### 6. Удаление медиа
```http
DELETE /api/properties/{id}/media/{mediaId}
```

**Действие:** Удаляет запись в БД и физические файлы (если были загружены).

**Ответ:** `204 No Content` или `404 Not Found`

### PropertyMediaResponse структура
```json
{
  "id": "uuid",
  "propertyId": "uuid",
  "type": "PHOTO",
  "url": "/uploads/properties/.../original.jpg",
  "previewUrl": "/uploads/properties/.../preview.jpg",
  "sortOrder": 0,
  "createdAt": "2026-02-04T10:00:00Z"
}
```

### Конфигурация хранения файлов (application.properties)
```
file.storage.path=uploads/properties
file.storage.max-size-photo=10485760
file.storage.max-size-video=104857600
file.storage.preview.max-width=800
file.storage.preview.max-height=600
file.storage.max-media-per-property=50
```

### PropertyResponse структура
```json
{
  "id": "uuid",
  "ownerUserId": "uuid",
  "type": "APARTMENT",
  "country": "CZ",
  "city": "Прага",
  "district": "Прага 1",
  "street": "Вацлавская площадь",
  "houseNumber": "1",
  "addressText": "Вацлавская площадь 1, Прага 1",
  "lat": 50.0833,
  "lng": 14.4167,
  "dispozice": "2+kk",
  "roomsCount": 2,
  "floor": 3,
  "totalFloors": 5,
  "areaM2": 45.5,
  "balconyAreaM2": 5.0,
  "cellarAreaM2": 3.0,
  "hasBalcony": true,
  "hasTerrace": false,
  "hasLoggia": false,
  "hasGarden": false,
  "hasCellar": true,
  "hasElevator": true,
  "parkingType": "GARAGE",
  "createdAt": "2026-02-04T10:00:00",
  "updatedAt": "2026-02-04T10:00:00"
}
```

---

## Listing API

### Базовый путь: `/api/listings`

#### 1. Создание объявления
```http
POST /api/listings
X-User-Id: {owner-uuid}
Content-Type: application/json

{
  "propertyId": "uuid",
  "rentMonthly": 15000,
  "depositKauce": 30000,
  "utilitiesMonthly": 2000,
  "petsAllowed": true,
  "smokingAllowed": false,
  "childrenAllowed": true,
  "maxTenants": 2
}
```

**Валидация:**
- `propertyId` - обязательное, UUID существующего property
- `rentMonthly` - обязательное, положительное число
- `depositKauce`, `utilitiesMonthly` - опциональные, положительные числа
- `maxTenants` - опциональное, положительное число (Short)
- `petsAllowed`, `smokingAllowed`, `childrenAllowed` - опциональные boolean

**Заголовок:** `X-User-Id` - UUID владельца (временно, позже будет из JWT)

**Ответ:** `200 OK` с `ListingResponse` или `400 Bad Request`

#### 2. Получение всех объявлений
```http
GET /api/listings
```

**Ответ:** `200 OK` со списком `ListingResponse[]`

#### 3. Получение объявления по ID
```http
GET /api/listings/{id}
```

**Ответ:** `200 OK` с `ListingResponse` или `404 Not Found`

#### 4. Полное обновление объявления (PUT)
```http
PUT /api/listings/{id}
Content-Type: application/json

{
  "status": "PUBLISHED",
  "paymentStatus": "SUCCEDED",
  "rentMonthly": 16000,
  "depositKauce": 32000,
  "utilitiesMonthly": 2500,
  "petsAllowed": false,
  "smokingAllowed": false,
  "childrenAllowed": true,
  "maxTenants": 3
}
```

**Валидация:**
- Все поля опциональные
- `status` - опциональное: `DRAFT`, `PUBLISHED`, `RENTED`, `ARCHIVED`
- `paymentStatus` - опциональное: `UNPAID`, `CREATED`, `PENDING`, `SUCCEDED`, `FAILED`, `CANCELED`, `REFUNDED`
- Числовые поля - положительные, если указаны

**Ответ:** `200 OK` с обновленным `ListingResponse` или `404 Not Found`

#### 5. Частичное обновление (PATCH)
```http
PATCH /api/listings/{id}
Content-Type: application/json

{
  "status": "PUBLISHED",
  "rentMonthly": 17000
}
```

**Ответ:** `200 OK` с обновленным `ListingResponse` или `404 Not Found`

#### 6. Удаление объявления
```http
DELETE /api/listings/{id}
```

**Ответ:** `204 No Content` или `404 Not Found`

#### 7. Публикация объявления
```http
POST /api/listings/{id}/publish
```

**Действие:** Изменяет статус на `PUBLISHED` и устанавливает `publishedAt`

**Ответ:** `200 OK` или `404 Not Found`

#### 8. Снятие с публикации
```http
POST /api/listings/{id}/unpublish
```

**Действие:** Изменяет статус на `DRAFT` и очищает `publishedAt`

**Ответ:** `200 OK` с `ListingResponse` или `400 Bad Request` (если не опубликовано)

#### 9. Архивирование объявления
```http
POST /api/listings/{id}/archive
```

**Действие:** Изменяет статус на `ARCHIVED`

**Ответ:** `200 OK` с `ListingResponse` или `404 Not Found`

#### 10. Поиск по статусу
```http
GET /api/listings/search/status?status=PUBLISHED
```

**Параметры:**
- `status` - обязательный: `DRAFT`, `PUBLISHED`, `RENTED`, `ARCHIVED`

**Ответ:** `200 OK` со списком `ListingResponse[]`

#### 11. Поиск по владельцу
```http
GET /api/listings/search/owner?ownerId={uuid}
```

**Ответ:** `200 OK` со списком `ListingResponse[]`

#### 12. Поиск по недвижимости
```http
GET /api/listings/search/property?propertyId={uuid}
```

**Ответ:** `200 OK` со списком `ListingResponse[]`

### ListingResponse структура
```json
{
  "id": "uuid",
  "propertyId": "uuid",
  "ownerId": "uuid",
  "status": "PUBLISHED",
  "paymentStatus": "UNPAID",
  "publishedAt": "2026-02-04T10:00:00Z",
  "rentMonthly": 15000,
  "depositKauce": 30000,
  "utilitiesMonthly": 2000,
  "petsAllowed": true,
  "smokingAllowed": false,
  "childrenAllowed": true,
  "maxTenants": 2,
  "createdAt": "2026-02-04T10:00:00Z",
  "updatedAt": "2026-02-04T10:00:00Z"
}
```

---

## Архитектура и организация

### Структура компонентов

#### DTO (Data Transfer Objects)
DTO организованы по доменам в подпапках `dto/`:

```
dto/
├── user/           — UserResponse, CreateUserRequest, UpdateUserRequest, ChangePasswordRequest
├── property/       — PropertyResponse, PropertyDTO, CreatePropertyRequest, UpdatePropertyRequest
├── propertymedia/  — PropertyMediaResponse, PropertyMediaDTO, CreatePropertyMediaRequest, 
│                     UpdatePropertyMediaRequest, ReorderMediaRequest
└── listing/        — ListingResponse, CreateListingRequest, UpdateListingRequest, PatchListingRequest
```

Используются Java records для передачи данных между слоями:

- **Create*Request** - для создания новых ресурсов (обязательные поля с валидацией)
- **Update*Request** - для полного обновления (все поля опциональные)
- **Patch*Request** - для частичного обновления (все поля опциональные)
- **Response** - для ответов API (все поля только для чтения)

Импорты: `samoprodej.samoprodej.dto.{domain}.{ClassName}` (например, `samoprodej.samoprodej.dto.propertymedia.PropertyMediaResponse`)

#### Services
Содержат бизнес-логику:
- Используют `@Transactional` для управления транзакциями
- `@Transactional(readOnly = true)` для read-only операций
- Логирование через `@Slf4j`
- Обработка ошибок через `RuntimeException`

#### Controllers
REST API эндпоинты:
- Используют `@Valid` для валидации DTO
- Возвращают `ResponseEntity` для контроля HTTP статусов
- Обработка ошибок через try-catch

#### Mappers
Преобразование между Entity и DTO:
- `toResponse(Entity)` - Entity → Response DTO
- `toEntity(CreateRequest)` - CreateRequest → Entity
- `updateEntityFromDto(UpdateRequest, Entity)` - обновление Entity из DTO

### Особенности реализации

#### Soft Delete
- **User**: ручное обновление `deletedAt` через сервис
- **Property**: автоматический soft delete через `@SQLDelete` и `@SQLRestriction`

#### Валидация
Все DTO используют Jakarta Validation:
- `@NotNull`, `@NotBlank` - обязательные поля
- `@Email` - валидация email
- `@Size` - ограничение длины строк
- `@Positive` - положительные числа
- `@DecimalMin`, `@DecimalMax` - диапазоны для BigDecimal

#### Статусы и Enums
- **UserStatus**: `ACTIVE`, `BLOCKED`, `DELETED`
- **ListingStatus**: `DRAFT`, `PUBLISHED`, `RENTED`, `ARCHIVED`
- **PaymentStatus**: `UNPAID`, `CREATED`, `PENDING`, `SUCCEDED`, `FAILED`, `CANCELED`, `REFUNDED`
- **Role**: `TENANT`, `OWNER`, `ADMIN`
- **PropertyType**: `APARTMENT`, `HOUSE`, `ROOM`, `COMMERCIAL`, `LAND`, `OTHER`
- **MediaType**: `PHOTO`, `VIDEO`

---

## Примеры использования

### Пример 1: Создание пользователя и недвижимости

```bash
# 1. Создать пользователя-владельца
curl -X POST http://localhost:8082/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "owner@example.com",
    "password": "password123",
    "role": "OWNER",
    "authProvider": "LOCAL",
    "preferredLang": "CS"
  }'

# Ответ содержит UUID пользователя, используем его дальше
# ownerId = "123e4567-e89b-12d3-a456-426614174000"

# 2. Создать недвижимость
curl -X POST http://localhost:8082/api/properties \
  -H "Content-Type: application/json" \
  -d '{
    "ownerUserId": "123e4567-e89b-12d3-a456-426614174000",
    "type": "APARTMENT",
    "city": "Прага",
    "areaM2": 50.0
  }'

# Ответ содержит UUID недвижимости
# propertyId = "223e4567-e89b-12d3-a456-426614174000"

# 3. Создать объявление
curl -X POST http://localhost:8082/api/listings \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 123e4567-e89b-12d3-a456-426614174000" \
  -d '{
    "propertyId": "223e4567-e89b-12d3-a456-426614174000",
    "rentMonthly": 15000,
    "depositKauce": 30000
  }'
```

### Пример 2: Публикация объявления

```bash
# 1. Создать объявление (статус DRAFT по умолчанию)
# ... (см. пример 1)

# 2. Опубликовать объявление
curl -X POST http://localhost:8082/api/listings/{listing-id}/publish

# Статус изменен на PUBLISHED, установлен publishedAt
```

### Пример 3: Поиск опубликованных объявлений

```bash
curl http://localhost:8082/api/listings/search/status?status=PUBLISHED
```

---

## Обработка ошибок

### Валидация
При ошибках валидации возвращается `400 Bad Request` без деталей ошибки.

### Ресурс не найден
При отсутствии ресурса возвращается `404 Not Found`.

### Внутренние ошибки
При внутренних ошибках возвращается `500 Internal Server Error`.

---

## Безопасность

⚠️ **Важно:** В текущей версии Spring Security настроен с автоматически сгенерированным паролем для разработки. Все эндпоинты доступны без аутентификации (кроме заголовка `X-User-Id` для создания объявлений).

Для продакшена необходимо:
1. Настроить JWT аутентификацию
2. Добавить авторизацию по ролям
3. Защитить чувствительные эндпоинты
