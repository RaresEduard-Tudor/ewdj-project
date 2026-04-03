# FIFA World Cup 2026 — Prediction App

A Spring Boot + Thymeleaf web app where users predict match scores for the FIFA World Cup 2026, compete in teams, and climb the public leaderboard.

---

## Features

- **Match predictions** — predict scores for all 48 group stage matches; edit until 3 days before kick-off
- **Automatic scoring** — exact score, correct outcome, and bonus points for being the sole predictor in your team
- **Team management** — create teams, join via invite code, manage members and settings (invite toggle, max members)
- **Group standings** — live group table with W/D/L/GD/Pts for all 12 WC 2026 groups
- **Public Top-10** — leaderboard of the best teams by total score, visible to everyone
- **Admin panel** — add/edit matches, enter final results, audit log for all admin actions
- **Spring Security** — role-based access (USER / ADMIN), login/logout always in nav

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 3.4, Java 21 |
| Views | Thymeleaf + Thymeleaf Security Dialect |
| Security | Spring Security 6 |
| Persistence | Spring Data JPA + MySQL 8 |
| Validation | Jakarta Validation + 5 custom annotations |
| HTTP Client | Spring WebFlux WebClient (reactive) |
| Build | Maven |

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
├── validation/      5 custom Jakarta annotations
├── DataSeeder.java
└── WorldcupApplication.java
```

---

## Scoring

Configured in `application.properties` via `@Value`:

| Event | Points |
|---|---|
| Exact score | 10 |
| Correct outcome (win/draw) | 4 |
| Bonus: sole exact score in team | +5 |
| Bonus: sole correct outcome in team | +2 |

---

## Notes

- Before submitting, change `spring.jpa.hibernate.ddl-auto=update` → `none` in `application.properties`
- All user-visible strings are in `src/main/resources/messages.properties`
- The REST API (`/api/matches/**`) calls itself via WebClient — no external dependency needed
