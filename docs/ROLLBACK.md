# Rollback

Flyway does not undo a migration. Roll back the image first. Restore the database only when the data or schema cannot be fixed forward.

## Frontend

Deploy the previous frontend image. It is a static production server and does not own schema. Confirm the homepage, one published page, and that `SITE_URL` still matches the live origin.

## Backend

Deploy the previous backend image that matches the current schema. If the new image already applied a migration, the previous image must still start against that schema (`ddl-auto=validate`). If it cannot, fix forward with a new migration. Do not run Flyway `clean`.

## Database

Restore from `BACKUP_RESTORE.md` only when rows are corrupt or a migration cannot be fixed forward. Restoring discards writes made after the dump. Stop writes, restore into a new database, point the backend at it, and verify a known record before sending traffic back.

## Configuration

Keep the previous environment revision. Rolling back an image while leaving a new `SITE_URL`, CORS origin, or JWT secret in place can look like an application failure. Change one layer at a time and record which one moved.

## After rollback

Check liveness, readiness, a public read, and an admin login. Leave `SEO_INDEXING_ENABLED` as it was unless the canonical origin changed.
