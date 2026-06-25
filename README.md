-## Run project with docker:

```bash
docker-compose up --build
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
mvn verify
```

Integration tests spin up MySQL via Testcontainers, and Docker must be running locally. If `mvn verify` fails with `Connection refused` / `CannotCreateTransactionException` errors on the Wordle test classes, it's Docker Desktop running low on memory while juggling multiple Testcontainers MySQL instances at once - not a code bug. Try, in order:

1. Free up Docker resources: `docker system prune` (removes stopped containers, unused networks/images), or bump Docker Desktop's memory allocation under Settings → Resources.
2. If it still fails, split the heavier MySQL-backed test classes from the rest of the suite into two runs, listed explicitly so the two batches together cover every test class (41 tests total as of writing):

   **Batch 1** - the two MySQL-Testcontainers-heavy classes (10 tests):
   ```
   mvn test -Dtest=WordleControllerTest,WordleSessionRepositoryTest
   ```

   **Batch 2** - every other test class (31 tests):
   ```
   mvn test -Dtest=MainApplicationIntegrationTest,UserDetailsImplTest,UserDetailsServiceImplTest,UserServiceTest,WordleDailyServiceTest,WordleGuessEvaluatorTest,WordleSessionServiceTest
   ```

   Both commands above work as-is in Windows `cmd.exe` (e.g. IntelliJ's default Maven runner) since `,` isn't special there. In bash/zsh/PowerShell, quote the whole argument instead:
   ```bash
   mvn test "-Dtest=WordleControllerTest,WordleSessionRepositoryTest"
   mvn test "-Dtest=MainApplicationIntegrationTest,UserDetailsImplTest,UserDetailsServiceImplTest,UserServiceTest,WordleDailyServiceTest,WordleGuessEvaluatorTest,WordleSessionServiceTest"
   ```

   If a new test class gets added later and isn't in either list above, just add it to Batch 2 (or run `mvn verify` once Docker has more headroom) - these batches are a manual workaround, not an enforced split.
