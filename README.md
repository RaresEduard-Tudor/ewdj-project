# FIFA World Cup 2026 - Prediction App

A Spring Boot + Thymeleaf web app where users predict match scores for the FIFA World Cup 2026, compete in teams, and climb the public leaderboard.

---

## Features

- **Match predictions**: predict scores for all 24 group stage matches; edit until 1 hour before kick-off
- **Automatic scoring**: exact score, correct outcome, and bonus points for being the sole predictor in your team
- **Team management**: create teams, join via invite code, manage members and settings (invite toggle, max members)
- **Group standings**: live group table with W/D/L/GD/Pts for all 12 WC 2026 groups
- **Public Top-10**: leaderboard of the best teams by total score, visible to everyone (including guests)
- **Admin panel**: add/edit matches, enter final results via modal, audit log for all admin actions
- **Match filtering**: search by country, filter by date or status (upcoming/final)
- **Spring Security**: role-based access (USER / ADMIN), login/logout always in nav
- **Responsive UI**: hamburger nav on mobile, CSS animations, medal indicators on scoreboard

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 3.4, Java 21 |
| Views | Thymeleaf + Thymeleaf Security Dialect |
| Security | Spring Security 6 |
| Persistence | Spring Data JPA + MySQL 8 (H2 for tests) |
| Validation | Jakarta Validation + 5 custom annotations |
| HTTP Client | Spring WebFlux WebClient (reactive) |
| i18n | Resource bundles (`messages.properties`) |
| Build | Maven |

---

## Design Patterns

| Pattern | Where in the project |
|---|---|
| **MVC** | Core architecture: `@Controller` handles requests, delegates to `@Service`, returns Thymeleaf view names. Model populated via Spring's `Model` object |
| **Singleton** | All Spring beans (`@Service`, `@Controller`, `@Component`, `@Repository`) are singletons managed by the IoC container. Dependencies injected via constructor injection |
| **Factory** | `@Bean` methods act as factory methods: `PasswordEncoderConfig.passwordEncoder()`, `SecurityConfig.filterChain()`. Also `MatchResponseDto.from(Match)` is a static factory converting entities to DTOs |
| **Strategy** | `ConstraintValidator` interface with 5 implementations (`EmailValidator`, `PasswordsValidator`, `ChecksumValidator`, `CountriesValidator`, `MatchDateValidator`), each providing a different validation strategy. Also `UserDetailsService` with custom `UserService` implementation |
| **Template Method** | `CommandLineRunner.run()` in `DataSeeder`: Spring defines the startup lifecycle template, we fill in the `run()` step. `HandlerInterceptor.preHandle()` in `AdminAuditInterceptor` follows the same idea |
| **Observer** | When admin saves a match result, `ScoringService` recalculates all prediction scores for that match, including per-team bonus logic. Spring's event-driven lifecycle (DataSeeder on startup) also fits |
| **Proxy** | `StadiumCapacityClient` wraps WebClient calls behind a simple `fetchCapacity()` method, acting as a proxy to the REST API. Spring also generates JPA repository proxies at runtime for all `JpaRepository` interfaces |
| **Facade** | `SecurityConfig.filterChain()` configures auth rules, login, logout, and URL permissions through one fluent API instead of dozens of separate security components |
| **Builder** | `WebClient.builder().baseUrl(baseUrl).build()` in `StadiumCapacityClient`. `HttpSecurity` also uses builder-style fluent API in `SecurityConfig` |
| **Adapter** | `UserService implements UserDetailsService`: adapts our `User` entity and `UserRepository` to Spring Security's `UserDetails` interface |
| **Iterator & Composite** | Thymeleaf `th:each` iterates over collections in every list view. Group standings use `Map<String, List<TeamStandingDto>>`: a composite structure of groups containing team standings |
| **Decorator** | Spring Security's filter chain decorates requests with authentication context. `@Transactional` decorates service methods with transaction management (AOP-based) |
| **Command** | `DataSeeder implements CommandLineRunner`: encapsulates startup seeding actions as a command object executed by the framework |
| **State** | Not explicitly used |

---

## Setup

### Prerequisites

- Java 21
- Maven
- Docker

### 1. Start MySQL

```bash
docker run -d \
  --name worldcup-mysql \
  -e MYSQL_ROOT_PASSWORD=admin123 \
  -e MYSQL_DATABASE=worldcup \
  -p 3306:3306 \
  mysql:8
```

If the container already exists from a previous run:

```bash
docker start worldcup-mysql
```

### 2. Run the app

```bash
mvn spring-boot:run
```

### 3. Open in browser

```
http://localhost:9001
```

### Default admin account

| Username | Password |
|---|---|
| `admin` | `admin123` |

The seeder runs on startup and creates the admin account + all 24 WC 2026 group stage fixtures automatically (only if the DB is empty).

---

## Project Structure

```
src/main/java/com/worldcup/
├── config/          SecurityConfig, WebConfig, PasswordEncoderConfig
├── controller/      HomeController, AuthController, MatchController,
│                    TeamController, PredictionController,
│                    ScoreboardController, GroupsController
│   └── admin/       AdminMatchController
├── rest/            MatchRestController (REST API)
├── client/          StadiumCapacityClient (WebClient)
├── domain/          User, Team, Match, Prediction
├── dto/             RegistrationDto, MatchDto, MatchResponseDto,
│                    PredictionDto, TeamStandingDto
├── exception/       GlobalExceptionHandler + custom exceptions
├── interceptor/     AdminAuditInterceptor
├── repository/      UserRepository, TeamRepository, MatchRepository,
│                    PredictionRepository
├── service/         UserService, TeamService, MatchService,
│                    PredictionService, ScoringService
├── util/            FlagUtil
├── validation/      5 custom Jakarta annotations + validators
├── DataSeeder.java
└── WorldcupApplication.java
```

---

## Scoring

Configured in `application.properties` and loaded via `@Value`:

| Event | Points |
|---|---|
| Exact score | 10 |
| Correct outcome (win/draw) | 5 |
| Bonus: sole exact score in team | +5 |
| Bonus: sole correct outcome in team | +3 |

---

## Custom Validators

| Annotation | Purpose |
|---|---|
| `@ValidEmail` | Validates email format |
| `@ValidPasswords` | Checks password and confirm password match (class-level) |
| `@ValidChecksum` | Verifies checksum equals stadiumCode % 97 |
| `@ValidCountries` | Ensures Country A and Country B are different |
| `@ValidMatchDate` | Date must fall within FIFA WC 2026 period (June 11 - July 19, 2026) |

---

## Testing

37 tests covering:

- **Unit tests**: `MatchDateValidatorTest`, `PasswordsValidatorTest` (custom validator logic)
- **Integration tests**: `ScoringServiceTest` (full scoring + bonus calculation with H2 DB)
- **MVC tests**: `MatchControllerTest`, `AdminMatchControllerTest` (MockMvc, form submission, redirects)
- **REST tests**: `MatchRestControllerTest` (JSON responses, status codes)
- **Security tests**: `SecurityConfigTest` (role-based access, login/logout, CSRF)

```bash
mvn test
```

---

## Security

- **Roles**: `ROLE_USER` (default on registration), `ROLE_ADMIN` (seeded)
- **Protected routes**: `/team/**`, `/predictions/**` require authentication; `/admin/**` requires `ROLE_ADMIN`
- **Public routes**: `/`, `/matches`, `/groups`, `/top10`, `/login`, `/register`, `/api/**`
- **CSRF**: enabled on all POST forms via hidden `_csrf` token
- **Password encoding**: BCrypt via `PasswordEncoder` bean
- **Nav**: login/logout always visible; role-gated links for admin and authenticated sections

---

## Error Handling

| Exception | HTTP | Handler |
|---|---|---|
| `PredictionDeadlineException` | 400 | Shows deadline message |
| `TeamNotFoundException` | 404 | Team not found page |
| `DuplicateTeamNameException` | 409 | Redirects with error |
| `MatchNotFoundException` | 404 | Match not found page |
| `DuplicateUsernameException` | 409 | Registration form error |
| `AccessDeniedException` | 403 | Access denied page |
| Generic `Exception` | 500 | Generic error page |

---

## Notes

- Before submitting, change `spring.jpa.hibernate.ddl-auto=create` to `none` in `application.properties`
- `spring.jpa.open-in-view=false`: lazy collections are initialized explicitly in controllers via `Hibernate.initialize()`
- All user-visible strings are in `src/main/resources/messages.properties` (i18n ready)
- The REST API (`/api/matches/**`) is consumed by the app's own `StadiumCapacityClient` via WebClient (no external dependency)
- The `DataSeeder` only seeds data when the database is empty (idempotent)
