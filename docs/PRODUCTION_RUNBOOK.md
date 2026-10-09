# Production runbook

## Deploy

1. Confirm a fresh backup exists.
2. Set secrets in the host store. Profile `prod`. Indexing off until the domain check passes.
3. Start PostgreSQL and Redis, then the backend. Wait until readiness is up. Flyway failure means the deploy failed.
4. Start or roll the frontend production server.
5. Run the checks below.

## Verify

- `GET /actuator/health/liveness` returns UP.
- `GET /actuator/health/readiness` returns UP and does not show a password or a database URL.
- `GET /` on the public origin returns the Arabic homepage.
- One published word, search, grammar, tools, and learn respond.
- `GET /robots.txt` disallows indexing while `SEO_INDEXING_ENABLED=false`.
- Admin login works. An anonymous admin API call returns 401.

## Restart

Restart the backend process. Flyway validates and does not create a second owner. The public site returns after readiness is up. Graceful shutdown allows in-flight requests up to 20 seconds. Send SIGTERM, not SIGKILL, unless the process is wedged.

## Logs

Read stdout JSON. Filter on `traceId` from the API error body. Do not ask a user to paste a token.

## Backup and restore

Follow `BACKUP_RESTORE.md`. A failed backup blocks the next schema deploy.

## Incidents

| Symptom | First checks |
| --- | --- |
| Site down | Frontend process, proxy, TLS certificate, `SITE_URL`. |
| API down | Readiness, backend logs, proxy route to the API origin. |
| Database down | Readiness fails, liveness stays up. Restore connectivity. Do not restart in a loop. Restore only if data is lost. |
| Redis down | Staff login, assistant, and quizzes return 503. Reading and tools stay up. Restore Redis. Do not flush it to "fix" rate limits unless the keys are corrupt. |
| AI provider down | The rest of the site stays up. The assistant returns a safe unavailable response. Leave `AI_ENABLED=false` if there is no key. |
| High 5xx | Trace ids in logs, pool timeouts, recent deploy. Roll back the image if the error starts with the deploy and the schema is still compatible. |

## Rollback

Follow `ROLLBACK.md`.
