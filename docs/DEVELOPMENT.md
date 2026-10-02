# Development

## Windows PowerShell

```powershell
Copy-Item .env.example .env
docker compose up -d
cd backend
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run
```

In another shell:

```powershell
cd frontend
npm install
npm run lint
npm run typecheck
npm run build
npm test
npm run dev
```

## Unix shells

```bash
cp .env.example .env
docker compose up -d
cd backend && ./mvnw clean verify
```

## JDK

The compiler target is Java 25. Maven Wrapper downloads Maven itself; a system Maven install is unnecessary. `JAVA_HOME` must point at JDK 25 before `mvnw`. On this machine the default `java` on `PATH` may still be 21. Point the shell at Temurin 25 before building:

```powershell
$env:JAVA_HOME = "$env:USERPROFILE\.jdks\temurin-25\jdk-25.0.4.1+1"
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path
java -version
```

## Tests

- Unit tests cover Arabic normalization, ids, time, and error JSON.
- `@WebMvcTest` covers the error model without containers.
- `FoundationIntegrationTest` starts PostgreSQL and Redis with Testcontainers and checks Flyway, health, and anonymous public reads.
- `AdminIdentityIntegrationTest` checks staff login, RBAC, token rotation, lockout, the last owner, optimistic locking, and the audit trail.
- ArchUnit guards module boundaries. There is still no public visitor account type.

## Port conflicts

The Compose file publishes PostgreSQL on `5432` and Redis on `6379`. If a local service already owns one of those ports, set `DB_PORT` or `REDIS_PORT` before `docker compose up` and use the same values when starting the backend. Example:

```powershell
$env:DB_PORT = "5433"
$env:REDIS_PORT = "6380"
docker compose up -d
```

## Encoding

Source files are UTF-8. The database is created with UTF-8. HTTP responses force UTF-8. The frontend document is `lang="ar"` and `dir="rtl"`.
