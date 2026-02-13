# Архитектура проекта

## 1. Общая архитектура

Классическая слоистая архитектура Spring Boot:

```
Client (HTTP/JSON)
       |
  Controllers  — REST API, валидация DTO, обработка ошибок
       |
   Services    — бизнес-логика, @Transactional
       |
 Repositories  — JPA (Spring Data), доступ к БД
       |
   Entities    — JPA-сущности (таблицы PostgreSQL)
```

Данные между слоями передаются через **DTO** (Java records). Преобразование Entity <-> DTO выполняется в **Mapper** (`@Component`, ручной маппинг без MapStruct).

## 2. Структура пакетов

Базовый пакет: `samoprodej.samoprodej`

```
samoprodej.samoprodej/
├── config/                 — SecurityConfig, JwtAuthenticationFilter, FileStorageConfig, ApplicationConfig
│   └── initializers/       — DataInitializer, UserInitializer, PropertyInitializer и др.
├── controller/             — REST-контроллеры (AuthController, UserController, PropertyController, ListingController)
├── dto/                    — DTO, разделены по доменам
│   ├── auth/               — AuthResponse, LoginRequest, RegisterRequest
│   ├── user/               — UserResponse, CreateUserRequest, UpdateUserRequest, ChangePasswordRequest
│   ├── property/           — PropertyResponse, CreatePropertyRequest, UpdatePropertyRequest, PropertyDTO (deprecated)
│   ├── propertymedia/      — PropertyMediaResponse, CreatePropertyMediaRequest, UpdatePropertyMediaRequest, ReorderMediaRequest, PropertyMediaDTO (deprecated)
│   └── listing/            — ListingResponse, CreateListingRequest, UpdateListingRequest, PatchListingRequest
├── entity/                 — JPA-сущности
├── enums/                  — 14 перечислений
├── mapper/                 — Entity <-> DTO маппинг (@Component)
├── repository/             — JpaRepository интерфейсы
└── service/                — Бизнес-логика (@Service)
```

## 3. Домены

| Домен | Entity | Контроллер | Сервис | Таблица |
|-------|--------|-----------|--------|---------|
| Auth | — | AuthController | JwtService, RefreshTokenService | — |
| User | User | UserController | UserService | `users` |
| Property | Property | PropertyController | PropertyService | `properties` |
| PropertyMedia | PropertyMedia | PropertyController | PropertyMediaService, FileStorageService | `property_media` |
| Listing | Listing | ListingController | ListingService | `listings` |
| RefreshToken | RefreshToken | — | RefreshTokenService | `refresh_tokens` |

### Связи между сущностями

```
User ←── Property (ownerUserId: raw UUID, не @ManyToOne)
User ←── Listing (@ManyToOne LAZY)
Property ←── Listing (@ManyToOne LAZY)
Property ←── PropertyMedia (@ManyToOne LAZY)
User ←── RefreshToken (@ManyToOne LAZY)
```

## 4. Аутентификация и безопасность

### JWT (RS256)

- **JwtService** — генерация и валидация access-токенов (RS256, RSA ключи)
- **RefreshTokenService** — жизненный цикл refresh-токенов с ротацией и обнаружением replay-атак
- **JwtAuthenticationFilter** — извлекает JWT из `Authorization: Bearer`, устанавливает `SecurityContext`
- **SecurityConfig** — фильтр-цепочка: CSRF выключен, stateless сессии, JWT-фильтр перед `UsernamePasswordAuthenticationFilter`
- **SecurityUser** — адаптер `UserDetails`, оборачивает `User` entity
- **ApplicationConfig** — `PasswordEncoder` (BCrypt), `AuthenticationProvider`, `UserDetailsService`

### Конфигурация ключей

Свойства JWT (**не** в `application.properties` — передаются через env-переменные или отдельный профиль):

```
jwt.private-key       — RSA приватный ключ (PEM, base64)
jwt.public-key        — RSA публичный ключ (PEM, base64)
jwt.access-token-expiration    — время жизни access-токена (мс)
jwt.refresh-token-expiration   — время жизни refresh-токена (мс)
```

### Токены

| Тип | Хранение | Передача | Содержимое |
|-----|----------|----------|------------|
| Access | — | `Authorization: Bearer <token>` | `userId`, `role`, `sub` (email) |
| Refresh | SHA-256 хэш в БД (`refresh_tokens`) | httpOnly cookie `refresh_token` | 64 байта random |

### Публичные эндпоинты

`/api/auth/**`, `/api/public/**` — не требуют JWT. Все остальные пути требуют аутентификации.

## 5. Сущности (Entities)

### Активные сущности

| Entity | Таблица | ID | Soft Delete | Особенности |
|--------|---------|----|-------------|-------------|
| User | `users` | `@GeneratedValue(UUID)` | Ручной (`deletedAt`, без фильтра в запросах) | `@CreationTimestamp`, `@UpdateTimestamp` |
| Property | `properties` | `@UuidGenerator` | `@SQLDelete` + `@SQLRestriction("deleted_at IS NULL")` | `@PrePersist/@PreUpdate`: нормализация города (cityNorm) |
| PropertyMedia | `property_media` | `@GeneratedValue` | Нет (hard delete) | Unique constraint `(property_id, sort_order)` |
| Listing | `listings` | `@GeneratedValue` | Нет (hard delete) | `@PrePersist` → `DRAFT` + `UNPAID`, метод `publish()` |
| RefreshToken | `refresh_tokens` | `@UuidGenerator` | Отзыв через `revokedAt` | Уникальный индекс по `token_hash` |

### Stub-сущности (скелеты)

| Entity | Таблица | Статус |
|--------|---------|--------|
| Payment | `payments` | Абстрактный, `@Inheritance(JOINED)` |
| DealPayment | `payments_deal` | **Нет `@Entity`** — не работает |
| ListingPayment | `payments_listing` | **Нет `@Entity`** — не работает |
| VerificationPayment | `payments_listing` | **Нет `@Entity`**, дублирует имя таблицы с ListingPayment |
| Deal | `deal` | Только ID, пустой |
| VerificationRequest | `verification_request` | Только ID, пустой |

## 6. Soft Delete — два подхода

### Property (полный Hibernate soft delete)

```java
@SQLDelete(sql = "UPDATE properties SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
```

- `repository.delete(p)` → `UPDATE ... SET deleted_at = NOW()` (не физическое удаление)
- Все JPA-запросы автоматически исключают удалённые записи

### User (неполный soft delete)

- `UserService.delete()` вручную устанавливает `deletedAt`
- **Нет `@SQLRestriction`** — удалённые пользователи видны в `findAll()`, `findByEmail()` и т.д.

## 7. DTO-конвенции

DTO — Java records в подпакетах по доменам (`dto/user/`, `dto/property/` и т.д.):

| Тип | Назначение | Валидация |
|-----|-----------|-----------|
| `Create*Request` | Создание | `@NotNull`, `@NotBlank`, `@Size`, `@Positive` |
| `Update*Request` | Полное обновление | Все поля опциональные |
| `Patch*Request` | Частичное обновление | Идентичная структура с Update |
| `*Response` | Ответ API | Только для чтения |

Legacy DTO-классы (`PropertyDTO`, `PropertyMediaDTO`) — `@Data` + `@Deprecated`, существуют параллельно с records.

## 8. Mapper-паттерн

Каждый маппер — `@Component` с методами:

```java
Response toResponse(Entity e);                       // Entity → Response DTO
Entity toEntity(CreateRequest r);                    // или (CreateRequest r, ParentEntity p)
void updateEntityFromDto(UpdateRequest r, Entity e); // null-safe частичное обновление
```

`updateEntityFromDto` обновляет только non-null поля из запроса — это паттерн partial update, используемый повсеместно.

## 9. Хранение файлов

| Компонент | Роль |
|-----------|------|
| FileStorageConfig | Настройки из `file.storage.*` properties |
| FileStorageService | Сохранение/удаление файлов, генерация превью |
| PropertyMediaService | Бизнес-логика загрузки, переупорядочивания, удаления |

- Путь: `uploads/properties/{propertyId}/{mediaId}/media.{ext}`
- Превью фото: Thumbnailator (max 800x600)
- Лимиты: 10MB фото, 100MB видео, 50 медиа на объект
- MIME-типы: `image/jpeg`, `image/png`, `image/webp`, `video/mp4`, `video/webm`, `application/*`, `model/*`

## 10. Инициализация данных

При запуске `DataInitializer` (`CommandLineRunner`) засевает пустую БД:

1. `UserInitializer` — admin + tenant + owner (настраивается `app.user.count`, по умолчанию 10)
2. `PropertyInitializer` — объекты в чешских городах (`app.property.count`, по умолчанию 50)
3. `PropertyMediaInitializer` — 3-6 placeholder фото на объект (picsum.photos URLs)
4. `ListingInitializer` — объявления, 70% published / 30% drafts (`app.listing.count`, по умолчанию 50)

Засевка выполняется только если таблицы пустые.

## 11. Тестирование

Два тест-файла:

| Файл | Тип | Покрытие |
|------|-----|----------|
| `SamoprodejApplicationTests` | `@SpringBootTest` | Загрузка контекста |
| `AuthControllerTest` | `@WebMvcTest` | 6 тестов: register (успех, дубль email, неизвестный язык), login (успех, неверный пароль, несуществующий пользователь) |

Подход к тестированию:
- `@WebMvcTest` + `@AutoConfigureMockMvc(addFilters=false)` — без Security-фильтров
- `@MockitoBean` для мокирования зависимостей
- `MockMvc` для HTTP-симуляции
- Проверки: HTTP статус, JSON body (`jsonPath`), headers (`Set-Cookie`)

## 12. Известные проблемы

1. **Двойное хэширование паролей**: `UserService.hashPassword()` использует `String.hashCode()` (небезопасно), а `AuthController` — BCrypt. Пользователи, созданные через `UserService.create()` или `DataInitializer`, не могут логиниться через `/api/auth/login`.
2. **Отсутствует `@Entity`** на `DealPayment`, `ListingPayment`, `VerificationPayment`.
3. **Дублирование имени таблицы** `payments_listing` у `VerificationPayment` и `ListingPayment`.
4. **Неполный soft delete User** — нет `@SQLRestriction`, удалённые пользователи видны в запросах.
5. **JWT-свойства не в `application.properties`** — нужны env-переменные.
6. **Опечатка `PaymentStatus.SUCCEDED`** — должно быть `SUCCEEDED`.
7. **`PropertyStatus` enum** — определён, но не используется ни одной сущностью.
8. **Дублирование зависимостей в `pom.xml`** — `spring-boot-starter-security` и `spring-boot-starter-validation` указаны дважды.

---

## Руководство по добавлению новых сущностей

### Чек-лист

1. **Entity** в `entity/` — `@Entity`, `@Table`, `@Id`, `@GeneratedValue(UUID)`, связи через `@ManyToOne`/`@OneToMany`
2. **Enum** в `enums/` (если нужен)
3. **Repository** в `repository/` — `extends JpaRepository<Entity, UUID>`
4. **DTO** в `dto/{domain}/` — `Create*Request`, `Update*Request`, `*Response` (Java records)
5. **Mapper** в `mapper/` — `@Component`, методы `toResponse`, `toEntity`, `updateEntityFromDto`
6. **Service** в `service/` — `@Service`, `@Transactional`, инжектировать Repository + Mapper
7. **Controller** в `controller/` — `@RestController`, `@RequestMapping("/api/...")`, `@Valid` для DTO
8. Обновить `SecurityConfig` если нужны публичные эндпоинты
9. Обновить `API_DOCUMENTATION.md`

### Паттерн контроллера

```java
@RestController
@RequestMapping("/api/examples")
@RequiredArgsConstructor
public class ExampleController {
    private final ExampleService service;

    @PostMapping
    public ResponseEntity<ExampleResponse> create(@Valid @RequestBody CreateExampleRequest request) {
        try {
            return ResponseEntity.ok(service.create(request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
```

### Паттерн сервиса

```java
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ExampleService {
    private final ExampleRepository repository;
    private final ExampleMapper mapper;

    @Transactional(readOnly = true)
    public ExampleResponse getById(UUID id) {
        return repository.findById(id)
            .map(mapper::toResponse)
            .orElseThrow(() -> new RuntimeException("Not found: " + id));
    }
}
```

### Паттерн маппера

```java
@Component
public class ExampleMapper {
    public ExampleResponse toResponse(ExampleEntity e) { /* ... */ }
    public ExampleEntity toEntity(CreateExampleRequest r) { /* ... */ }
    public void updateEntityFromDto(UpdateExampleRequest r, ExampleEntity e) {
        if (r.name() != null) e.setName(r.name());
        // null-safe: обновляются только переданные поля
    }
}
```
