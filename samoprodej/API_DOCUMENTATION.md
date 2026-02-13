# API Documentation

## Общие сведения

- **Базовый URL**: `http://localhost:8082/api`
- **Формат**: JSON
- **Аутентификация**: JWT (RS256) в заголовке `Authorization: Bearer <token>`
- **Публичные эндпоинты**: `/api/auth/**`, `/api/public/**`

### HTTP-коды ответов

| Код | Описание |
|-----|----------|
| 200 | Успешный запрос |
| 201 | Ресурс создан |
| 204 | Успешное удаление (нет тела ответа) |
| 400 | Ошибка валидации / некорректные данные |
| 401 | Не аутентифицирован / неверные учётные данные |
| 404 | Ресурс не найден |
| 500 | Внутренняя ошибка сервера |

---

## Auth API

Базовый путь: `/api/auth`

Все эндпоинты Auth API являются публичными (не требуют JWT), кроме `GET /me`.

### POST /api/auth/register

Регистрация нового пользователя. Пароль хэшируется через BCrypt.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "secret123",
  "firstName": "Jan",
  "lastName": "Novak",
  "role": "TENANT",
  "preferredLang": "CS"
}
```

| Поле | Тип | Обязательное | Описание |
|------|-----|-------------|----------|
| email | string | да | Email (уникальный) |
| password | string | да | Пароль |
| firstName | string | нет | Имя |
| lastName | string | нет | Фамилия |
| role | enum | нет | `TENANT`, `OWNER`, `ADMIN` |
| preferredLang | string | нет | `CS`, `UA`, `EN`, `RU` (по умолчанию `CS`) |

**Response:** `200 OK`
```json
{
  "user": { "...UserResponse..." },
  "accessToken": "eyJhbG...",
  "expiresInSec": 900
}
```

Также устанавливает httpOnly cookie `refresh_token`.

### POST /api/auth/login

Аутентификация по email/паролю.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "secret123"
}
```

**Response:** `200 OK` — структура аналогична register. `401` при неверных данных.

### POST /api/auth/refresh

Обновление access-токена через refresh-токен из cookie.

**Cookie:** `refresh_token` — обязательный.

**Response:** `200 OK` с новым `AuthResponse`. Старый refresh-токен отзывается, в cookie записывается новый. При повторном использовании отозванного токена — все токены пользователя отзываются (защита от replay-атак).

### POST /api/auth/logout

Отзыв refresh-токена и очистка cookie.

**Cookie:** `refresh_token` — опциональный.

**Response:** `200 OK`

### GET /api/auth/me

Получение профиля текущего аутентифицированного пользователя.

**Заголовок:** `Authorization: Bearer <access_token>`

**Response:** `200 OK` с `UserResponse`

---

## User API

Базовый путь: `/api/users`

Все эндпоинты требуют аутентификации.

### POST /api/users

Создание пользователя.

**Request Body:**
```json
{
  "email": "user@example.com",
  "phone": "+420123456789",
  "password": "password123",
  "firstName": "Jan",
  "lastName": "Novak",
  "role": "TENANT",
  "authProvider": "LOCAL",
  "preferredLang": "CS"
}
```

| Поле | Тип | Обязательное | Валидация |
|------|-----|-------------|-----------|
| email | string | да | `@Email`, max 255 |
| phone | string | нет | max 32 |
| password | string | да | min 6, max 100 |
| firstName | string | нет | max 64 |
| lastName | string | нет | max 64 |
| role | enum | да | `TENANT`, `OWNER`, `ADMIN` |
| authProvider | enum | да | `LOCAL`, `BANKID`, `NEOID` |
| preferredLang | enum | нет | `CS`, `UA`, `EN`, `RU` |

**Response:** `200 OK` с `UserResponse`

### GET /api/users

Получение всех пользователей.

**Response:** `200 OK` с `UserResponse[]`

### GET /api/users/{id}

Получение пользователя по ID.

**Response:** `200 OK` с `UserResponse` или `404`

### PUT /api/users/{id}

Полное обновление пользователя. Все поля опциональные — обновляются только переданные.

**Request Body:**
```json
{
  "email": "new@example.com",
  "phone": "+420987654321",
  "firstName": "Petr",
  "lastName": "Svoboda",
  "preferredLang": "EN",
  "avatarUrl": "https://example.com/avatar.jpg"
}
```

| Поле | Тип | Валидация |
|------|-----|-----------|
| email | string | `@Email`, max 255 |
| phone | string | max 32 |
| firstName | string | max 64 |
| lastName | string | max 64 |
| preferredLang | enum | `CS`, `UA`, `EN`, `RU` |
| avatarUrl | string | — |

**Response:** `200 OK` с `UserResponse` или `404`

### PATCH /api/users/{id}

Частичное обновление. Тело запроса идентично PUT.

**Response:** `200 OK` с `UserResponse` или `404`

### DELETE /api/users/{id}

Soft delete — устанавливает `deletedAt`.

**Response:** `204 No Content` или `404`

### POST /api/users/{id}/activate

Активация пользователя (статус → `ACTIVE`).

**Response:** `200 OK` с `UserResponse`

### POST /api/users/{id}/block

Блокировка пользователя (статус → `BLOCKED`).

**Response:** `200 OK` с `UserResponse`

### POST /api/users/{id}/change-password

Смена пароля.

**Request Body:**
```json
{
  "oldPassword": "old_password",
  "newPassword": "new_password123"
}
```

| Поле | Тип | Валидация |
|------|-----|-----------|
| oldPassword | string | `@NotBlank` |
| newPassword | string | `@NotBlank`, min 6, max 100 |

**Response:** `200 OK` или `400`

### GET /api/users/search/email?email={email}

Поиск пользователя по email.

**Response:** `200 OK` с `UserResponse` или `404`

### GET /api/users/search/phone?phone={phone}

Поиск пользователя по телефону.

**Response:** `200 OK` с `UserResponse` или `404`

### UserResponse

```json
{
  "id": "uuid",
  "email": "user@example.com",
  "phone": "+420123456789",
  "firstName": "Jan",
  "lastName": "Novak",
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

Базовый путь: `/api/properties`

Все эндпоинты требуют аутентификации.

### POST /api/properties

Создание объекта недвижимости.

**Request Body:**
```json
{
  "ownerUserId": "uuid",
  "type": "APARTMENT",
  "country": "CZ",
  "city": "Praha",
  "district": "Praha 1",
  "street": "Vaclavske namesti",
  "houseNumber": "1",
  "addressText": "Vaclavske namesti 1, Praha 1",
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

| Поле | Тип | Обязательное | Валидация |
|------|-----|-------------|-----------|
| ownerUserId | UUID | да | — |
| type | enum | да | `APARTMENT`, `HOUSE`, `ROOM`, `COMMERCIAL`, `LAND`, `OTHER` |
| country | string | нет | max 2, по умолчанию `CZ` |
| city | string | да | max 128 |
| district | string | нет | max 128 |
| street | string | нет | max 255 |
| houseNumber | string | нет | max 32 |
| addressText | string | нет | max 512 |
| lat | BigDecimal | нет | -90.0 ... 90.0 |
| lng | BigDecimal | нет | -180.0 ... 180.0 |
| dispozice | string | нет | max 16 |
| roomsCount | int | нет | положительное |
| floor | int | нет | — |
| totalFloors | int | нет | положительное |
| areaM2 | BigDecimal | да | положительное |
| balconyAreaM2 | BigDecimal | нет | положительное |
| cellarAreaM2 | BigDecimal | нет | положительное |
| hasBalcony..hasElevator | boolean | нет | по умолчанию `false` |
| parkingType | enum | нет | `NONE`, `STREET`, `GARAGE`, `GARAGE_SPACE`, `PRIVATE_SPOT` |

**Response:** `200 OK` с `PropertyResponse`

### GET /api/properties

Получение всех объектов.

**Response:** `200 OK` с `PropertyResponse[]`

### GET /api/properties/{id}

Получение по ID.

**Response:** `200 OK` с `PropertyResponse` или `404`

### GET /api/properties/search?city={city}

Поиск по городу (нормализованный, без учёта диакритики).

**Response:** `200 OK` с `PropertyResponse[]`

### PUT /api/properties/{id}

Полное обновление (все поля опциональные, обновляются только переданные).

**Request Body:** поля из `UpdatePropertyRequest` (все поля `CreatePropertyRequest` кроме `ownerUserId` и `type`).

**Response:** `200 OK` с `PropertyResponse` или `404`

### PATCH /api/properties/{id}

Частичное обновление. Тело запроса идентично PUT.

**Response:** `200 OK` с `PropertyResponse` или `404`

### DELETE /api/properties/{id}

Soft delete (Hibernate `@SQLDelete` — выполняет `UPDATE SET deleted_at`).

**Response:** `204 No Content` или `404`

### PropertyResponse

```json
{
  "id": "uuid",
  "ownerUserId": "uuid",
  "type": "APARTMENT",
  "country": "CZ",
  "city": "Praha",
  "district": "Praha 1",
  "street": "Vaclavske namesti",
  "houseNumber": "1",
  "addressText": "Vaclavske namesti 1, Praha 1",
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

### Legacy-эндпоинты (deprecated)

| Метод | Путь | Описание |
|-------|------|----------|
| GET | `/api/properties/legacy` | Получение всех (возвращает `PropertyDTO`) |
| GET | `/api/properties/legacy/{id}` | Получение по ID |
| POST | `/api/properties/legacy` | Создание |
| PUT | `/api/properties/legacy/{id}` | Обновление |
| DELETE | `/api/properties/legacy/{id}` | Удаление |

---

## Property Media API

Базовый путь: `/api/properties/{propertyId}/media`

Управление фото, видео и 3D-турами для объектов недвижимости.

### POST /api/properties/{id}/media/upload

Загрузка файла (multipart).

**Content-Type:** `multipart/form-data`

| Параметр | Тип | Обязательное | Описание |
|----------|-----|-------------|----------|
| file | file | да | Файл (фото до 10MB, видео до 100MB) |
| type | enum | да | `PHOTO`, `VIDEO`, `TOUR3D` |

Для фото автоматически генерируется превью (max 800x600). Максимум 50 медиа на объект.

Допустимые MIME-типы: `image/jpeg`, `image/png`, `image/webp`, `video/mp4`, `video/webm`, `application/*`, `model/*`.

**Response:** `200 OK` с `PropertyMediaResponse`

### POST /api/properties/{id}/media

Добавление медиа по URL.

**Request Body:**
```json
{
  "type": "PHOTO",
  "url": "https://example.com/photo.jpg",
  "previewUrl": "https://example.com/preview.jpg",
  "sortOrder": 0
}
```

| Поле | Тип | Обязательное | Валидация |
|------|-----|-------------|-----------|
| type | enum | да | `PHOTO`, `VIDEO`, `TOUR3D` |
| url | string | да | `@NotBlank` |
| previewUrl | string | нет | — |
| sortOrder | int | нет | >= 0 |

**Response:** `200 OK` с `PropertyMediaResponse`

### GET /api/properties/{id}/media

Получение всех медиа объекта (отсортированы по `sortOrder`).

**Response:** `200 OK` с `PropertyMediaResponse[]`

### PUT /api/properties/{id}/media/reorder

Изменение порядка медиа.

**Request Body:**
```json
{
  "mediaIds": ["uuid1", "uuid2", "uuid3"]
}
```

| Поле | Тип | Валидация |
|------|-----|-----------|
| mediaIds | UUID[] | `@NotEmpty`, все UUID должны принадлежать данному объекту |

**Response:** `200 OK` с `PropertyMediaResponse[]`

### PATCH /api/properties/{id}/media/{mediaId}

Обновление медиа (previewUrl и/или sortOrder).

**Request Body:**
```json
{
  "previewUrl": "https://example.com/new-preview.jpg",
  "sortOrder": 1
}
```

**Response:** `200 OK` с `PropertyMediaResponse`

### DELETE /api/properties/{id}/media/{mediaId}

Удаление медиа (запись из БД + физические файлы).

**Response:** `204 No Content`

### PropertyMediaResponse

```json
{
  "id": "uuid",
  "propertyId": "uuid",
  "type": "PHOTO",
  "url": "/uploads/properties/.../media.jpg",
  "previewUrl": "/uploads/properties/.../preview.jpg",
  "sortOrder": 0,
  "createdAt": "2026-02-04T10:00:00Z"
}
```

### Legacy-эндпоинты медиа (deprecated)

| Метод | Путь | Описание |
|-------|------|----------|
| POST | `/api/properties/{id}/media/legacy` | Добавление (возвращает `PropertyMediaDTO`) |
| GET | `/api/properties/{id}/media/legacy` | Получение |
| DELETE | `/api/properties/media/{mediaId}` | Удаление |

---

## Listing API

Базовый путь: `/api/listings`

Управление объявлениями об аренде.

### POST /api/listings

Создание объявления.

**Заголовок:** `X-User-Id: {uuid}` — UUID владельца (временное решение, будет заменено на извлечение из JWT).

**Request Body:**
```json
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

| Поле | Тип | Обязательное | Валидация |
|------|-----|-------------|-----------|
| propertyId | UUID | да | — |
| rentMonthly | int | да | положительное |
| depositKauce | int | нет | положительное |
| utilitiesMonthly | int | нет | положительное |
| petsAllowed | boolean | нет | — |
| smokingAllowed | boolean | нет | — |
| childrenAllowed | boolean | нет | — |
| maxTenants | short | нет | положительное |

Объявление создаётся со статусом `DRAFT` и `paymentStatus = UNPAID`.

**Response:** `200 OK` с `ListingResponse`

### GET /api/listings

Получение всех объявлений.

**Response:** `200 OK` с `ListingResponse[]`

### GET /api/listings/{id}

Получение по ID.

**Response:** `200 OK` с `ListingResponse` или `404`

### PUT /api/listings/{id}

Полное обновление (все поля опциональные).

**Request Body:**
```json
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

| Поле | Тип | Валидация |
|------|-----|-----------|
| status | enum | `DRAFT`, `PUBLISHED`, `RENTED`, `ARCHIVED` |
| paymentStatus | enum | `UNPAID`, `CREATED`, `PENDING`, `SUCCEDED`, `FAILED`, `CANCELED`, `REFUNDED` |
| rentMonthly | int | положительное |
| depositKauce | int | положительное |
| utilitiesMonthly | int | положительное |
| petsAllowed | boolean | — |
| smokingAllowed | boolean | — |
| childrenAllowed | boolean | — |
| maxTenants | short | положительное |

**Response:** `200 OK` с `ListingResponse` или `404`

### PATCH /api/listings/{id}

Частичное обновление. Тело запроса идентично PUT.

**Response:** `200 OK` с `ListingResponse` или `404`

### DELETE /api/listings/{id}

Hard delete (удаление из БД).

**Response:** `204 No Content` или `404`

### POST /api/listings/{id}/publish

Публикация объявления. Устанавливает `status = PUBLISHED` и `publishedAt`.

**Response:** `200 OK`

### POST /api/listings/{id}/unpublish

Снятие с публикации. Возвращает статус в `DRAFT`, очищает `publishedAt`.

**Response:** `200 OK` с `ListingResponse` или `400` (если не было опубликовано)

### POST /api/listings/{id}/archive

Архивирование. Устанавливает `status = ARCHIVED`.

**Response:** `200 OK` с `ListingResponse`

### GET /api/listings/search/status?status={status}

Поиск по статусу.

| Параметр | Значения |
|----------|----------|
| status | `DRAFT`, `PUBLISHED`, `RENTED`, `ARCHIVED` |

**Response:** `200 OK` с `ListingResponse[]`

### GET /api/listings/search/owner?ownerId={uuid}

Поиск по владельцу.

**Response:** `200 OK` с `ListingResponse[]`

### GET /api/listings/search/property?propertyId={uuid}

Поиск по объекту недвижимости.

**Response:** `200 OK` с `ListingResponse[]`

### ListingResponse

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

## Перечисления (Enums)

| Enum | Значения |
|------|----------|
| Role | `TENANT`, `OWNER`, `ADMIN` |
| UserStatus | `ACTIVE`, `BLOCKED`, `DELETED` |
| AuthProvider | `LOCAL`, `BANKID`, `NEOID` |
| Language | `CS`, `UA`, `EN`, `RU` |
| PropertyType | `APARTMENT`, `HOUSE`, `ROOM`, `COMMERCIAL`, `LAND`, `OTHER` |
| ListingStatus | `DRAFT`, `PUBLISHED`, `RENTED`, `ARCHIVED` |
| MediaType | `PHOTO`, `VIDEO`, `TOUR3D` |
| ParkingType | `NONE`, `STREET`, `GARAGE`, `GARAGE_SPACE`, `PRIVATE_SPOT` |
| PaymentStatus | `UNPAID`, `CREATED`, `PENDING`, `SUCCEDED`, `FAILED`, `CANCELED`, `REFUNDED` |
| PaymentProvider | `STRIPE`, `GPWEBPAY`, `GOPAY`, `COMGATE` |
| PaymentPurpose | `OWNER_VERIFICATION`, `LISTING_PROMOTION`, `DEAL_COMMISSION` |
| Currency | `CZK`, `USD` |
| PromotionType | `TOP_7_DAYS`, `FEATURED_30_DAYS`, `URGENT`, `HIGHLIGHT` |
| PropertyStatus | `DRAFT`, `ACTIVE`, `ARCHIVED` (определён, но не используется) |

---

## Примеры использования (curl)

### Регистрация и получение токена

```bash
curl -X POST http://localhost:8082/api/auth/register \
  -H "Content-Type: application/json" \
  -c cookies.txt \
  -d '{
    "email": "owner@example.com",
    "password": "password123",
    "firstName": "Jan",
    "lastName": "Novak",
    "role": "OWNER"
  }'
```

### Создание объекта с JWT

```bash
curl -X POST http://localhost:8082/api/properties \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <access_token>" \
  -d '{
    "ownerUserId": "<user-uuid>",
    "type": "APARTMENT",
    "city": "Praha",
    "areaM2": 50.0
  }'
```

### Загрузка фото

```bash
curl -X POST http://localhost:8082/api/properties/<property-uuid>/media/upload \
  -H "Authorization: Bearer <access_token>" \
  -F "file=@photo.jpg" \
  -F "type=PHOTO"
```

### Создание и публикация объявления

```bash
# Создание (DRAFT)
curl -X POST http://localhost:8082/api/listings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <access_token>" \
  -H "X-User-Id: <owner-uuid>" \
  -d '{
    "propertyId": "<property-uuid>",
    "rentMonthly": 15000,
    "depositKauce": 30000
  }'

# Публикация
curl -X POST http://localhost:8082/api/listings/<listing-uuid>/publish \
  -H "Authorization: Bearer <access_token>"
```

### Обновление refresh-токена

```bash
curl -X POST http://localhost:8082/api/auth/refresh \
  -b cookies.txt \
  -c cookies.txt
```
