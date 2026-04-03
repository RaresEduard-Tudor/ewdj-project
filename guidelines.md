# FIFA World Cup 2026 — Team Prediction App

Spring Boot + Thymeleaf university assignment. **Missing any of the 9 sub-requirements = herexamen (re-examination).**

---

## Assignment Requirements Checklist

| # | Requirement | Implementation |
|---|-------------|----------------|
| 1 | Spring Boot + Thymeleaf | All views in `src/main/resources/templates/` |
| 2 | Spring Security (admin/user roles) | `SecurityConfig`, login/logout in `layout.html` |
| 3 | JPA + MySQL | `domain/` entities, `application.properties` |
| 4 | Jakarta Validation (1+ custom annotation) | `validation/` — `@ValidEmail`, `@ValidPasswords`, `@ValidChecksum`, `@ValidCountries`, `@ValidMatchDate` |
| 5 | REST API + Reactive WebClient | `rest/MatchRestController`, `client/StadiumCapacityClient` |
| 6 | Unit tests | `src/test/` — MVC, REST, security, validation |
| 7 | Resource bundles | `messages.properties` — full register screen + all validation messages |
| 8 | Error handling | `GlobalExceptionHandler` (@ControllerAdvice), `error/404.html`, `error/error.html` |
| 9 | Security nav | `layout.html` — login/logout always visible, role-gated sections |

---

## Functionalities

### 1. Home Page

The home page shows:

- Overview of all matches (sorted by date): teams (A vs B), date/time, location (city + stadium)
- Links to: Login / Register, Public Top-10 Teams Ranking
- If logged in: link to own team(s) and predictions
- Admins see an extra "Manage Matches" button

### 2. Team Management

#### 2.1 Create Team

A team contains:

- Team name (unique within the application)
- Auto-generated invite code (min. 8 characters)
- Owner (creator of the team)

#### 2.2 Team Page

Available only to team members. Shows:

- Team name and member list
- Owner (marked)
- Personal score per member
- Total team score
- Button to remove members (owner only)

#### 2.3 Invite Members

The owner can:

- Regenerate the invite code
- Share the code (no mail service required — only display it)

Any user can join via the invite code.

### 3. Match Screen

When selecting a match, the user sees:

- Country A – Country B
- Date and time
- Stadium and location
- Official final score (if known)
- Own prediction (if logged in)
- Ability to change prediction until 1 hour before kick-off
- Admins see an Edit button

### 4. Match Management (Admin)

Admins can add matches via a form.

**Fields:** Country A, Country B, Date/time, City + Stadium, Stadium code (4 digits), Checksum (stadiumCode % 97)

**Validation:**

- Country A ≠ Country B
- Date must fall within the official World Cup period
- Checksum must be correct
- No two matches at the same location at the same time

Admins can also edit matches and enter official final scores after the match is played.

### 5. Predictions (User)

Each user can predict for every match:

- Goals Team A
- Goals Team B

Once an admin enters the final result, the user receives points and the team receives points.

#### 5.1 Scoring

Per user:

- Correct exact score → **X points**
- Correct winner / draw → **Y points**

Bonus points:

- Only person in team with exact score → **+B points**
- Only person in team with correct winner → **+C points**

X, Y, B, C are defined in `application.properties` (loaded via `@Value`).

### 6. Team Scoreboard (Private)

Team members can see:

- Total score per member
- Ranking within the team
- Per-match detail (optional)

Accessible only to team members.

### 7. Public Top-10 Teams

Visible to everyone (including guests):

- Top 10 teams sorted by total score
- Team name, total score, number of members

---

## Project Structure

```text
src/main/java/com/worldcup/
├── config/          SecurityConfig, WebConfig, PasswordEncoderConfig
├── controller/      HomeController, AuthController, MatchController, TeamController,
│                    PredictionController, ScoreboardController, GroupsController
│   └── admin/       AdminMatchController
├── rest/            MatchRestController
├── client/          StadiumCapacityClient (WebClient — calls own app)
├── domain/          User, Team, Match, Prediction
├── dto/             RegistrationDto, MatchDto, MatchResponseDto, PredictionDto, TeamStandingDto
├── exception/       GlobalExceptionHandler, PredictionDeadlineException,
│                    TeamNotFoundException, DuplicateTeamNameException, DuplicateMatchException
├── interceptor/     AdminAuditInterceptor (logs all /admin/** requests)
├── repository/      UserRepository, TeamRepository, MatchRepository, PredictionRepository
├── service/         UserService, TeamService, MatchService, PredictionService, ScoringService
├── util/            FlagUtil
├── validation/      ValidEmail/EmailValidator, ValidPasswords/PasswordsValidator,
│                    ValidChecksum/ChecksumValidator, ValidCountries/CountriesValidator,
│                    ValidMatchDate/MatchDateValidator
├── DataSeeder.java  (seeds admin/admin123 + WC 2026 group stage fixtures on startup)
└── WorldcupApplication.java
```

---

## Rules — Do Not Break

- **`@Valid` + `BindingResult`** must always appear together; `BindingResult` must be the parameter immediately after the DTO.
- **Never expose JPA entities from REST endpoints** — always use `MatchResponseDto.from(match)`.
- **`@ControllerAdvice` must have multiple `@ExceptionHandler` methods** — `GlobalExceptionHandler` has 5.
- **`AdminAuditInterceptor`** must remain functional and registered in `WebConfig` — separate requirement from Security.
- **Scoring bonuses B/C are per-team**, not global — `ScoringService.isSoleExactInAnyTeam()`.
- **`@ValidMatchDate` instead of `@Future`** on `matchDate` — validates WC 2026 range (June 11–July 19).
- **Scoring constants** (`scoring.exact`, etc.) must stay in `application.properties`, loaded via `@Value` — NOT `messages.properties`.
- **All user-visible strings** must use `#{key}` from `messages.properties`, never hardcoded.
- **`@Slf4j` logging** must be present in all controllers and service classes.
- **`server.error.whitelabel.enabled=false`** must stay in `application.properties` for 404 to work.

---

## Running Locally

1. Start MySQL via Docker: `docker run -d --name worldcup-mysql -e MYSQL_ROOT_PASSWORD=admin123 -e MYSQL_DATABASE=worldcup -p 3306:3306 mysql:8`
2. `mvn spring-boot:run`
3. App at [http://localhost:9001](http://localhost:9001)
4. Admin seeded automatically: `admin` / `admin123`

## WebClient Strategy

`StadiumCapacityClient` calls `http://localhost:9001/api/matches/stadiums/{code}/capacity` — calls the app itself. No external API needed.

---

## Submission

- Change `spring.jpa.hibernate.ddl-auto=create` → `none` before submitting.
- Zip as: `Klasgroep_Naam_Voornaam.zip`
- Deadline: **Saturday 23 May, 23:59**
