# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Samoprodej — backend for a Czech real estate rental platform. Spring Boot 4.0.2, Java 21, PostgreSQL, Maven. All source code lives inside the `samoprodej/` subdirectory (a Maven project with `mvnw` wrapper).

## Build & Run

All commands run from `samoprodej/`:

```bash
./mvnw clean package            # compile + run tests + build JAR
./mvnw spring-boot:run          # start the app (port 8082)
./mvnw test                     # run all tests
./mvnw test -Dtest=AuthControllerTest                  # single test class
./mvnw test -Dtest=AuthControllerTest#login_success_returns200AndTokens  # single method
```

App runs at `http://localhost:8082`. CORS is configured for `http://localhost:3000` (frontend).

## Database

PostgreSQL via Docker Compose (`samoprodej/docker-compose.yml`):

```bash
docker-compose up -d db         # start PostgreSQL on port 5433
docker-compose up -d adminer    # Adminer DB UI on port 8080
```

Credentials: `devDB` / `devPass`, database `samoprodej`. The `application.properties` points to `localhost:5433` (Docker). For local PostgreSQL change port to `5432`.

Schema is managed by `hibernate.ddl-auto=update` — no Flyway/Liquibase migrations. Hibernate auto-creates/updates tables on startup.

## Architecture

Standard layered Spring Boot app:

```
Controller → Service → Repository → Entity
     ↕            ↕
    DTO ←→ Mapper (manual, no MapStruct)
```

Base package: `samoprodej.samoprodej`

| Package | Role |
|---------|------|
| `entity/` | JPA entities (table mapping) |
| `repository/` | `JpaRepository<Entity, UUID>` interfaces |
| `service/` | Business logic, `@Transactional` |
| `controller/` | REST endpoints (`/api/...`) |
| `dto/{domain}/` | Java records per domain: `user/`, `property/`, `listing/`, `propertymedia/`, `auth/` |
| `mapper/` | Manual Entity↔DTO conversion (`@Component`) |
| `enums/` | All enumerations (14 enums) |
| `config/` | Security, JWT filter, FileStorage, ApplicationConfig |
| `config/initializers/` | Mock data seeders (`CommandLineRunner`) |

### DTO Conventions

DTOs are Java records in domain subpackages (`dto/user/`, `dto/property/`, etc.):

- `Create*Request` — creation with validation (`@NotNull`, `@NotBlank`, `@Size`)
- `Update*Request` — full update, all fields optional
- `Patch*Request` — partial update (identical structure to Update)
- `*Response` — API response

Legacy `@Data` DTO classes (`PropertyDTO`, `PropertyMediaDTO`) exist alongside records — marked `@Deprecated`.

### Mapper Methods

Each mapper (`@Component`) follows this interface:

```java
Response toResponse(Entity e);
Entity toEntity(CreateRequest r);                    // or (CreateRequest r, ParentEntity p)
void updateEntityFromDto(UpdateRequest r, Entity e); // null-safe partial update
```

The `updateEntityFromDto` methods only set fields that are non-null in the request — this is the partial update pattern used everywhere.

## Authentication & Security

**JWT (RS256)** with access + refresh token flow:

- **Access token**: Short-lived JWT signed with RSA private key. Contains `userId`, `role`, `sub` (email). Sent in `Authorization: Bearer <token>` header.
- **Refresh token**: 64-byte random token, only SHA-256 hash stored in DB (`refresh_tokens` table). Sent via httpOnly cookie named `refresh_token`.
- **Token rotation**: On refresh, old token is revoked, new one issued. If a revoked token is reused, ALL user tokens are revoked (replay attack detection).

Key classes:
- `JwtService` — token generation/validation (RS256)
- `RefreshTokenService` — refresh token lifecycle with rotation and reuse detection
- `JwtAuthenticationFilter` — extracts JWT from Authorization header, sets SecurityContext
- `SecurityConfig` — filter chain: CSRF off, stateless sessions, JWT filter before `UsernamePasswordAuthenticationFilter`
- `SecurityUser` — `UserDetails` adapter wrapping the `User` entity
- `ApplicationConfig` — defines `PasswordEncoder` (BCrypt), `AuthenticationProvider`, `UserDetailsService`

**Public endpoints**: `/api/auth/**`, `/api/public/**`. Everything else requires authentication.

**Important**: JWT key properties (`jwt.private-key`, `jwt.public-key`, `jwt.access-token-expiration`, `jwt.refresh-token-expiration`) are read via `@Value` but are NOT in `application.properties` — they must be supplied via environment variables or a separate profile.

### Auth Endpoints (`/api/auth`)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/auth/register` | Register (BCrypt password, returns JWT + cookie) |
| POST | `/api/auth/login` | Login (email/password, returns JWT + cookie) |
| POST | `/api/auth/refresh` | Rotate refresh token (from cookie) |
| POST | `/api/auth/logout` | Revoke refresh token, clear cookie |
| GET | `/api/auth/me` | Current user profile |

## Domain Entities

### Active Entities

| Entity | Table | Soft Delete | ID Strategy | Relations |
|--------|-------|-------------|-------------|-----------|
| `User` | `users` | Manual (`deletedAt` field, no query filter) | `@GeneratedValue(UUID)` | — |
| `Property` | `properties` | `@SQLDelete` + `@SQLRestriction("deleted_at IS NULL")` | `@UuidGenerator` | `ownerUserId` (raw UUID, not `@ManyToOne`) |
| `PropertyMedia` | `property_media` | None | `@GeneratedValue` | `@ManyToOne(LAZY)` → Property |
| `Listing` | `listings` | None (hard delete) | `@GeneratedValue` | `@ManyToOne(LAZY)` → Property, `@ManyToOne(LAZY)` → User |
| `RefreshToken` | `refresh_tokens` | Revocation via `revokedAt` | `@UuidGenerator` | `@ManyToOne(LAZY)` → User |

### Stub/Incomplete Entities

These exist in code but are skeletons or have known issues:

- `Payment` (abstract, `@Inheritance(JOINED)`) — base for payment hierarchy
- `DealPayment`, `ListingPayment`, `VerificationPayment` — extend Payment, **missing `@Entity` annotation**
- `Deal`, `VerificationRequest` — skeleton entities with only ID field

### Key Entity Behaviors

**Property**: Has `@PrePersist`/`@PreUpdate` hooks that normalize city names (lowercase + strip diacritics via `java.text.Normalizer`) into `cityNorm` field for search. Auto-generates `addressText` from components.

**PropertyMedia**: Unique constraint on `(property_id, sort_order)`. `sortOrder` is managed by the service layer. Constructor-based creation: `new PropertyMedia(Property, MediaType, String url)`.

**Listing**: `prePersist()` sets defaults (`DRAFT` status, `UNPAID` payment). Has `publish()` method that sets status and timestamp. Constructor: `new Listing(Property, User, Integer rentMonthly)`.

## Soft Delete — Two Different Approaches

**Property**: Full Hibernate soft delete. `repository.delete(p)` executes `UPDATE ... SET deleted_at = NOW()`. All JPA queries auto-exclude deleted records via `@SQLRestriction`.

**User**: Incomplete soft delete. `UserService.delete()` sets `deletedAt` manually, but there's no `@SQLRestriction` — deleted users still appear in query results like `findAll()`, `findByEmail()`, etc.

## File Storage

`FileStorageService` + `PropertyMediaService` handle media uploads:

- Files stored at: `uploads/properties/{propertyId}/{mediaId}/media.{ext}`
- Photo previews auto-generated via Thumbnailator (max 800x600)
- Allowed MIME types: `image/jpeg`, `image/png`, `image/webp`, `video/mp4`, `video/webm`, `application/*`/`model/*` (3D tours)
- Limits: 10MB photos, 100MB videos, 50 media per property
- Config: `FileStorageConfig` bound to `file.storage.*` properties

## Data Initialization

On startup, `DataInitializer` (a `CommandLineRunner`) seeds mock data into an empty database in order:

1. `UserInitializer` — admin + tenant + owner users (configurable count via `app.user.count`)
2. `PropertyInitializer` — random properties in Czech cities (`app.property.count`)
3. `PropertyMediaInitializer` — 3-6 placeholder photos per property (picsum.photos URLs)
4. `ListingInitializer` — listings with calculated rents, 70% published / 30% drafts

Seeding only runs if tables are empty. Configurable via `app.*` properties (defaults: 10 users, 50 properties, 50 listings).

## API Endpoints

### Users (`/api/users`)
CRUD + `POST /{id}/activate`, `POST /{id}/block`, `POST /{id}/change-password`, `GET /search/email`, `GET /search/phone`

### Properties (`/api/properties`)
CRUD + `GET /search?city=` + full media management sub-endpoints:
- `POST /{id}/media/upload` (multipart file upload)
- `POST /{id}/media` (add by URL)
- `GET /{id}/media`
- `PUT /{id}/media/reorder`
- `PATCH /{id}/media/{mediaId}`
- `DELETE /{id}/media/{mediaId}`

Legacy endpoints exist under `/api/properties/legacy/*` — all `@Deprecated`.

### Listings (`/api/listings`)
CRUD + `POST /{id}/publish`, `POST /{id}/unpublish`, `POST /{id}/archive`, `GET /search/status`, `GET /search/owner`, `GET /search/property`

**Note**: Listing creation uses `@RequestHeader("X-User-Id")` header for owner — intended to be replaced by JWT extraction later.

## Known Issues in Codebase

These are existing problems to be aware of:

1. **Dual password hashing**: `UserService.hashPassword()` uses `String.hashCode()` (insecure), while `AuthController` uses BCrypt. Users created via `UserService.create()` or `DataInitializer` cannot log in via `/api/auth/login`.
2. **Missing `@Entity`** on `DealPayment`, `ListingPayment`, `VerificationPayment` — will cause Hibernate errors if Payment hierarchy is used.
3. **`VerificationPayment` and `ListingPayment`** share table name `payments_listing` — copy-paste bug.
4. **User soft delete is incomplete** — `deletedAt` is set but queries don't filter by it.
5. **JWT config properties absent** — `jwt.private-key`, `jwt.public-key`, `jwt.access-token-expiration`, `jwt.refresh-token-expiration` are not in `application.properties`.
6. **`PaymentStatus.SUCCEDED`** — typo (should be `SUCCEEDED`). Changing this requires a database migration for existing data.
7. **`PropertyStatus` enum** — defined but not used by any entity.
8. **Duplicate dependencies in `pom.xml`** — `spring-boot-starter-security` and `spring-boot-starter-validation` are listed twice.

## Testing

Two test files exist:

- `SamoprodejApplicationTests` — context load test (`@SpringBootTest`)
- `AuthControllerTest` — `@WebMvcTest` with 6 tests covering register/login (success and error cases)

Test approach: `@WebMvcTest` with `@AutoConfigureMockMvc(addFilters=false)` to bypass security. Dependencies mocked via `@MockBean`. Tests use `MockMvc` for HTTP simulation.

When writing new controller tests, follow the `AuthControllerTest` pattern: mock all service/repository dependencies, disable security filters, test HTTP status codes and response body via `MockMvc` assertions.
