-## Run project with docker:

```bash
docker-compose up --build
```

## Running tests

```bash
mvn verify
```

Integration tests spin up MySQL via Testcontainers, and Docker must be running locally. If `mvn verify` fails with `Connection refused` / `CannotCreateTransactionException` errors on the Wordle test classes, it's Docker Desktop running low on memory while juggling multiple Testcontainers MySQL instances at once — not a code bug. Try, in order:

1. Free up Docker resources: `docker system prune` (removes stopped containers, unused networks/images), or bump Docker Desktop's memory allocation under Settings → Resources.
2. If it still fails, split the heavier MySQL-backed test classes from the rest of the suite into two runs, listed explicitly so the two batches together cover every test class (41 tests total as of writing):

   **Batch 1** — the two MySQL-Testcontainers-heavy classes (10 tests):
   ```
   mvn test -Dtest=WordleControllerTest,WordleSessionRepositoryTest
   ```

   **Batch 2** — every other test class (31 tests):
   ```
   mvn test -Dtest=MainApplicationIntegrationTest,UserDetailsImplTest,UserDetailsServiceImplTest,UserServiceTest,WordleDailyServiceTest,WordleGuessEvaluatorTest,WordleSessionServiceTest
   ```

   Both commands above work as-is in Windows `cmd.exe` (e.g. IntelliJ's default Maven runner) since `,` isn't special there. In bash/zsh/PowerShell, quote the whole argument instead:
   ```bash
   mvn test "-Dtest=WordleControllerTest,WordleSessionRepositoryTest"
   mvn test "-Dtest=MainApplicationIntegrationTest,UserDetailsImplTest,UserDetailsServiceImplTest,UserServiceTest,WordleDailyServiceTest,WordleGuessEvaluatorTest,WordleSessionServiceTest"
   ```

   If a new test class gets added later and isn't in either list above, just add it to Batch 2 (or run `mvn verify` once Docker has more headroom) — these batches are a manual workaround, not an enforced split.
