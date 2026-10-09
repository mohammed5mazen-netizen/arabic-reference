# Observability

## Logs

The backend writes one JSON object per line to stdout: `timestamp`, `level`, `logger`, `traceId`, `message`. Newlines and control characters in the message are replaced. Production level is INFO. Hibernate SQL and Lettuce stay at WARN.

Do not log passwords, JWT or refresh tokens, the AI API key, raw assistant questions, quiz attempt tokens, or `Authorization` headers. Client addresses used for rate limits are stripped of control characters first.

Unexpected API failures return `INTERNAL_ERROR` and a trace id. The stack stays in the log.

## Health

| Endpoint | Meaning |
| --- | --- |
| `GET /actuator/health/liveness` | The process is up. A down database does not fail this. |
| `GET /actuator/health/readiness` | Flyway has finished, the process is ready, and the database answers. |
| `GET /actuator/health` | Aggregate. Details are hidden (`show-details: never`). |

Other actuator endpoints are denied. The body does not include the database URL, Redis password, or other secrets.

Startup accepts traffic only after Flyway and the production configuration guard succeed. Shutdown is graceful, with 20 seconds for in-flight requests. The container entrypoint is `java`, so SIGTERM reaches Spring Boot.

## Metrics

Micrometer is on the classpath through Actuator. HTTP, JVM, and the Hikari pool are registered in process. The public web exposes health only, so those series are not scraped and cannot leak query text or user ids. Do not add tags for raw search text, a word, a lesson title, or a user id if a private scrape is enabled later.

To scrape later, put a management port on a private interface and allow `health` and `metrics` there. Do not publish that port to the internet.

## Recommended alerts

- Liveness or the public homepage failing.
- Readiness down for more than a minute.
- 5xx rate above the recent baseline.
- Hikari pool timeouts or connections waiting.
- p95 latency far above the local baseline in `PRODUCTION_RUNBOOK.md`.
- Disk free space low on the database volume.
- Backup job failed or the newest dump is older than 26 hours.
- Redis unavailable, because staff login, the assistant, and quizzes fail closed.

No hosted monitoring vendor is wired in. The platform's own checks are enough until one is chosen.
