# Learning progress

Opening a lesson does not mark it complete. The visitor presses **أكملت هذا الدرس**, or the quiz result is saved after submit.

## Anonymous

The browser keeps one record in `localStorage` under `ar.learning.v1`:

- `schema`: `1`. A different schema is ignored.
- `completedLessonSlugs`
- `quizScores`: quiz id, score, and whether it passed
- `lastLesson`: path slug, lesson slug, and title

The server does not receive this record. There is no `learning_progress` table. Path percentage is `completed / required lessons` in the browser, not a stored column.

## Accounts

Learner accounts are deferred. There is no public registration and no reuse of `AdminUser`. When an account exists later, the server can store path, unit, and lesson state (`NOT_STARTED`, `IN_PROGRESS`, `COMPLETED`) plus quiz attempts. The percentage stays derived from required lessons.
