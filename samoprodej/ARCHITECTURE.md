# Архитектура проекта и руководство по разработке

## 1. Как сейчас устроено приложение

### 1.1. Общая схема

Приложение построено по классической трёхслойной архитектуре:

```
Client (HTTP/JSON)
       ↓
  Controllers  — REST API, валидация входящих DTO
       ↓
   Services   — бизнес-логика, транзакции
       ↓
 Repositories — доступ к БД через JPA
       ↓
   Entity     — JPA-сущности
```

Данные между слоями передаются через **DTO (Data Transfer Objects)**, преобразование Entity ↔ DTO выполняется в **Mapper**.

### 1.2. Структура пакетов

```
samoprodej.samoprodej/
├── config/           — конфигурация (Security, FileStorage, DataInitializer)
├── controller/       — REST-контроллеры
├── dto/              — DTO, разбиты по доменам
│   ├── user/
│   ├── property/
│   ├── propertymedia/
│   └── listing/
├── Entity/           — JPA-сущности
├── enums/            — перечисления
├── mapper/           — Entity ↔ DTO
├── Repository/       — JPA-репозитории
└── service/          — сервисы
```

### 1.3. Домены и их связи

| Домен       | Entity       | Контроллер       | Сервис              | Описание                    |
|------------|--------------|------------------|---------------------|-----------------------------|
| User       | User         | UserController   | UserService         | Пользователи                |
| Property   | Property     | PropertyController| PropertyService     | Недвижимость                |
| PropertyMedia | PropertyMedia | (в PropertyController) | PropertyMediaService | Фото/видео недвижимости |
| Listing    | Listing      | ListingController| ListingService      | Объявления об аренде        |

Связи: `Property` → `User` (owner), `PropertyMedia` → `Property`, `Listing` → `Property`, `Listing` → `User` (owner).

### 1.4. Организация DTO

DTO лежат в подпапках по доменам. Package: `samoprodej.samoprodej.dto.{domain}`.

Типы DTO:
- **Create*Request** — создание (обязательные поля, валидация)
- **Update*Request** — полное обновление (все поля опциональные)
- **Patch*Request** — частичное обновление (при необходимости)
- **Response** — ответ API

Пример для propertymedia:
- `CreatePropertyMediaRequest`, `UpdatePropertyMediaRequest`, `ReorderMediaRequest`, `PropertyMediaResponse`

### 1.5. Property Media — как это работает

- **PropertyMediaService** — загрузка файлов (`uploadMedia`), добавление по URL (`addMediaFromUrl`), переупорядочивание, обновление, удаление.
- **FileStorageService** — сохранение файлов на диск, генерация превью, удаление.
- **FileStorageConfig** — настройки из `application.properties` (путь, размеры, лимиты).
- Файлы хранятся в `uploads/properties/{propertyId}/{mediaId}/`.

---

## 2. Как создавать новые сущности, сервисы, контроллеры и DTO

### Шаг 1: Entity

1. Создайте класс в `Entity/` с JPA-аннотациями.
2. Добавьте `@Entity`, `@Table`, `@Id`, `@GeneratedValue(UUID)`.
3. Используйте `@ManyToOne`, `@OneToMany` для связей.
4. При необходимости — `@SQLDelete`, `@SQLRestriction` для soft delete.

Пример структуры:
```java
@Entity
@Table(name = "example_entity")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ExampleEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    // поля, связи, createdAt/updatedAt при необходимости
}
```

### Шаг 2: Enum (если нужен)

Создайте enum в `enums/`:
```java
public enum ExampleStatus { DRAFT, ACTIVE, ARCHIVED }
```

### Шаг 3: Repository

1. Создайте интерфейс в `Repository/`.
2. Наследуйте `JpaRepository<Entity, UUID>`.
3. Добавьте кастомные методы при необходимости.

```java
public interface ExampleRepository extends JpaRepository<ExampleEntity, UUID> {
    List<ExampleEntity> findByStatus(ExampleStatus status);
}
```

### Шаг 4: DTO

1. Создайте папку `dto/{domain}/` (например, `dto/example/`).
2. Создайте DTO с package `samoprodej.samoprodej.dto.example`:
   - `CreateExampleRequest` — record с `@NotNull`, `@NotBlank`, `@Size` и т.д.
   - `UpdateExampleRequest` — record, все поля опциональные.
   - `ExampleResponse` — record для ответа.

Пример:
```java
// dto/example/CreateExampleRequest.java
package samoprodej.samoprodej.dto.example;

public record CreateExampleRequest(
    @NotBlank @Size(max = 255) String name,
    @NotNull UUID parentId
) {}
```

### Шаг 5: Mapper

1. Создайте класс в `mapper/` с `@Component`.
2. Реализуйте:
   - `toResponse(Entity)` → Response
   - `toEntity(CreateRequest)` → Entity
   - `updateEntityFromDto(UpdateRequest, Entity)` — обновление entity.

```java
@Component
public class ExampleMapper {
    public ExampleResponse toResponse(ExampleEntity e) { ... }
    public ExampleEntity toEntity(CreateExampleRequest r) { ... }
    public void updateEntityFromDto(UpdateExampleRequest r, ExampleEntity e) { ... }
}
```

### Шаг 6: Service

1. Создайте класс в `service/` с `@Service`, `@RequiredArgsConstructor`, `@Transactional`.
2. Инжектируйте Repository и Mapper.
3. Используйте `@Transactional(readOnly = true)` для read-only методов.
4. Логируйте через `@Slf4j`.
5. Выбрасывайте `RuntimeException` при ошибках (например, «not found»).

```java
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ExampleService {
    private final ExampleRepository repository;
    private final ExampleMapper mapper;

    public ExampleResponse create(CreateExampleRequest request) { ... }
    @Transactional(readOnly = true)
    public ExampleResponse getById(UUID id) { ... }
    public ExampleResponse update(UUID id, UpdateExampleRequest request) { ... }
    public void delete(UUID id) { ... }
}
```

### Шаг 7: Controller

1. Создайте класс в `controller/` с `@RestController`, `@RequestMapping("/api/...")`, `@RequiredArgsConstructor`.
2. Инжектируйте Service.
3. Используйте `@Valid` для тела запросов.
4. Возвращайте `ResponseEntity` и обрабатывайте ошибки в try-catch.

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
    // GET, PUT, PATCH, DELETE...
}
```

### Шаг 8: Импорты

Все DTO импортируются с указанием домена:
```java
import samoprodej.samoprodej.dto.example.CreateExampleRequest;
import samoprodej.samoprodej.dto.example.ExampleResponse;
import samoprodej.samoprodej.dto.example.UpdateExampleRequest;
```

### Шаг 9: Безопасность (опционально)

Если нужна защита эндпоинтов, обновите `SecurityConfig`: добавьте правила для новых путей `/api/...`.

---

## 3. Чек-лист для новой сущности

- [ ] Entity в `Entity/`
- [ ] Enum в `enums/` (если нужен)
- [ ] Repository в `Repository/`
- [ ] Папка `dto/{domain}/` и DTO (Create, Update, Response)
- [ ] Mapper в `mapper/`
- [ ] Service в `service/`
- [ ] Controller в `controller/`
- [ ] Обновить `API_DOCUMENTATION.md`
- [ ] При необходимости — DataInitializer, миграции

---

## 4. Важные замечания

### Валидация

Используйте Jakarta Validation:
- `@NotNull`, `@NotBlank` — обязательные поля
- `@Email`, `@Size`, `@Positive`, `@DecimalMin`, `@DecimalMax`

### Переменные в lambda

Переменные в lambda должны быть effectively final. Если нужно переприсваивать значение, заведите отдельную переменную для использования в lambda.

### Область видимости в try-catch

Переменные, объявленные внутри `try`, недоступны в `catch`. Объявляйте их до блока `try`, если они нужны в `catch`.

### Дополнительные зависимости

Для загрузки файлов используйте `FileStorageService` и `FileStorageConfig`. Пример — PropertyMediaService.
