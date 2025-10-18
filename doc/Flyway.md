Flyway usage (project)

This project uses Flyway for database migrations. Basic notes:

- Flyway migrations live under `src/main/resources/db/migration/` and must be named using Flyway's convention, e.g. `V2__add_store_table.sql`.
- The project currently includes a safe baseline `V1__baseline.sql` so Flyway will treat existing databases as version 1.

Run Flyway via Maven plugin (preferred in CI):

Windows (cmd.exe):
```
set FLYWAY_URL=jdbc:mysql://127.0.0.1:3306/clapp
set FLYWAY_USER=root
set FLYWAY_PASSWORD=admin123
mvn flyway:info
mvn flyway:migrate
```

Linux/macOS:
```
export FLYWAY_URL=jdbc:mysql://127.0.0.1:3306/clapp
export FLYWAY_USER=root
export FLYWAY_PASSWORD=admin123
mvn flyway:info
mvn flyway:migrate
```

Run Flyway through Spring Boot startup (when Flyway is on the classpath):
- Start the app normally; Flyway runs on application startup and applies pending migrations from `classpath:db/migration`.

Recommendations:
- Keep using `baseline-on-migrate=true` only when adopting Flyway into an existing DB for the first time. After migrations are applied and history is stable, prefer explicit migrations and remove baseline settings if appropriate.
- For production, provide DB credentials via environment variables or a secrets manager. Use `application-prod.properties` (this repo uses `${SPRING_DATASOURCE_*}` placeholders).
- Prefer small, focused SQL migrations with explicit statements and no destructive operations without review.

Converting existing DB dumps into Flyway migrations:
- Identify the authoritative dump file (e.g., from `db/Dump20250706/`).
- Clean the dump: remove `SET` statements that conflict with target DB, remove `USE` statements, split into logical migrations (tables, constraints, indexes, seed data).
- Create `V2__initial_schema.sql` containing non-destructive DDL and `V3__seed_data.sql` for initial seeds.

If you want, I can:
- Convert a chosen SQL dump into one or more Flyway migrations (I will preview the generated migration before adding it), or
- Add the Flyway Maven plugin configuration to CI scripts.

