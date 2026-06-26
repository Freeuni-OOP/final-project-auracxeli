# Kartuli Games

Two browser-based games in Georgian (Wordle and Connections) plus a shared platform - auth, profiles, stats, social features, admin, achievements.
Built with Java 21, Spring Boot 3.4.5, MySQL 8, Thymeleaf, and Spring Security.

## Prerequisites

| Tool | Minimum version |
|------|----------------|
| Docker Desktop | current |
| Java | 21 |
| Maven | 3.9+ |

Java and Maven are only required if you want to run without Docker or run the test suite.

## Quick start (Docker)

1. Clone the repo:

   ```bash
   git clone https://github.com/Freeuni-OOP/final-project-auracxeli.git
   cd final-project-auracxeli
   ```

2. Create your local env file and fill in the four variables:

   ```bash
   cp .env.example .env
   # open .env and set MYSQL_ROOT_PASSWORD, MYSQL_DATABASE, MYSQL_USER, MYSQL_PASSWORD
   ```

3. Build and start:

   ```bash
   docker-compose up --build
   ```

App is available at **http://localhost:8080**.

To tail logs: `docker-compose logs -f app`  
To stop: `docker-compose down`

## Run without Docker

Requires a local MySQL 8 instance. The app reads connection details from
`src/main/resources/application.properties`; override any property via the
matching env var (e.g. `SPRING_DATASOURCE_PASSWORD=secret`).

```bash
mvn spring-boot:run
```

## Project structure

```
src/main/java/com/auracxeli/
├── MainApplication.java
├── config/          # SecurityConfig
├── user/            # auth: entity, repository, service, controller, dto
├── wordle/          # entity/repository/service (controller + UI in progress)
├── connections/     # not yet built
├── social/          # not yet built
├── achievement/     # not yet built
└── admin/           # not yet built

src/main/resources/
├── db/migration/    # Flyway SQL migrations (V{N}__{description}.sql)
├── templates/       # Thymeleaf HTML templates
└── static/          # CSS, JS, images
```

## Logs

Application logs go to **stdout/console** (a single console appender - no log files).

- **Locally** (`mvn spring-boot:run`): logs print to the terminal you launched it from.
- **Docker** (`docker-compose up`): logs go to the container's stdout - view with
  `docker-compose logs -f app`.

### Formats (selected by Spring profile)

The format is defined in `src/main/resources/logback-spring.xml` and switches on the
active profile (`SPRING_PROFILES_ACTIVE`, default `dev`):

- **dev** (default): human-readable, colored, e.g.
  `12:00:00.123 INFO  c.a.wordle.WordleSessionService : Started Wordle game for user 42 on 2026-06-25`
- **prod** (`SPRING_PROFILES_ACTIVE=prod`): structured single-line `key=value`, easy to
  grep / ship to a log store, e.g.
  `ts=2026-06-25T12:00:00.123Z level=INFO logger=com.auracxeli.wordle.WordleSessionService thread=http-nio-8080-exec-1 msg="Started Wordle game for user 42 on 2026-06-25"`

### Changing the log level

Levels live in `src/main/resources/application.properties`:

```properties
logging.level.root=INFO
logging.level.com.auracxeli=DEBUG
```

Set `logging.level.com.auracxeli` to `DEBUG` (the default) to see guess evaluation,
daily-word lookups, and computed stats; raise it to `INFO` to quiet those down, or drop
to `TRACE` for everything. You can override without editing the file:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--logging.level.com.auracxeli=TRACE
# or via env var:
LOGGING_LEVEL_COM_AURACXELI=TRACE
```

### Level conventions used in this codebase

- **ERROR** - unexpected exceptions caught at a controller boundary (logged with stack trace).
- **WARN** - rejected input (invalid guess, failed validation), auth/lookup failures, missing daily puzzle.
- **INFO** - significant state changes: game started, game finished (win/loss), admin scheduled a word.
- **DEBUG** - guess evaluation inputs/outputs, daily-word lookups, computed stats.

We never log full session/user objects, passwords, request bodies, or the day's answer -
only scalar IDs, dates, and outcomes.

## Running tests

```bash
# Full build + test (what CI runs)
mvn verify

# Tests only, no package phase
mvn test

# Single test class
mvn test -Dtest=ClassName

# Single test method
mvn test -Dtest=ClassName#methodName
```

Integration tests spin up MySQL via Testcontainers - Docker must be running locally.
