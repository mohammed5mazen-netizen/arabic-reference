# Backup and restore

PostgreSQL is the source of truth. Redis holds rate limits and short-lived caches and is not backed up as business data.

## What to back up

- The application database, with `pg_dump` custom format.
- The production environment file, stored in the host's secret manager, not in Git.
- There is no user-upload store.

## Command

Run from a host that can reach PostgreSQL. Do not print the password into logs or tickets.

```text
pg_dump --format=custom --no-owner --file=arabic-reference-YYYYMMDD.dump --dbname="$DATABASE_URL"
```

On the local Docker database the equivalent is `docker exec` of `pg_dump -Fc` against the database name. Keep the dump off the application servers' public disks.

## Retention and encryption

Keep daily dumps for 14 days and one weekly dump for 8 weeks. Encrypt the dump at rest with the platform disk encryption or an age/gpg key held outside the database host. A backup that is not encrypted on a shared disk is not acceptable for production.

## Restore

Restore into an empty database. Do not restore over a live production database until the incident owner says so.

```text
createdb arabic_reference_restore
pg_restore --no-owner --dbname=arabic_reference_restore arabic-reference-YYYYMMDD.dump
```

Point a stopped backend at that database with the same Flyway history. Flyway should validate and not apply a new migration if the dump already contains the schema. Start the application and read one known published record through the public API.

## Verification

A backup is not a recovery plan until a restore has been read back. After each production backup job, record the dump size and the exit code. Once a month, restore the newest dump into a scratch database and select one published row. The S13 local drill is recorded in the validation report.

## If backup fails

Treat a failed backup as an incident. Do not deploy schema changes until the next successful dump. Alert on a missing daily dump or a non-zero `pg_dump` exit. Keep the previous good dump. Do not delete it to "make space" during the incident.

## What restore does not do

Flyway migrations do not run backward. Restoring an older dump discards data written after that dump. Prefer a forward schema fix when the database is healthy and only the application image is bad.
