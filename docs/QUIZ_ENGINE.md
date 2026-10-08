# Quiz engine

Editors write the correct options. The assistant is not asked which answer is right.

## Questions

S9 scores three types:

- `MULTIPLE_CHOICE`: at least two options and exactly one correct
- `TRUE_FALSE`: exactly two options and exactly one correct
- `MULTIPLE_SELECT`: at least two options and at least one correct

Multiple select is all-or-nothing. A missing or extra option makes the question wrong. There is no partial credit. The score is `correctCount * 100 / questionCount`, using integer division. A path passes when that score is at least the passing score, which must be from 0 to 100.

## Attempts

`POST /api/v1/public/learning/quizzes/{id}/attempts` copies the published quiz, including the correct flags, into `learning_attempt.snapshot`. The response returns a random URL-safe token (32 bytes) and the questions with option id and label only. The stored value is the SHA-256 hex of the token. The public id of the attempt is not a sequence.

The token lifetime is `LEARNING_ATTEMPT_TTL`, default 60 minutes, clamped to 30–120 minutes. Submit after expiry is rejected. A later edit of the quiz changes the working copy and, after republish, new attempts. It does not change an attempt that already started.

Submit sends `{ idempotencyKey, answers: [{ questionId, optionIds }] }`. The server checks that every question in the snapshot is answered, that option ids belong to that question, and that option ids are not repeated. The same idempotency key returns the stored result. A different key after submit is a conflict. The row is locked with `FOR UPDATE`, and the update matches `status = OPEN` and the row version, so two submits cannot both record a result.

The result includes the score, whether it passed, and for each question whether it was accepted, the editor's explanation, and the resolved knowledge link when the question has one. Those fields appear only after submit.

`maxAttempts` is stored for a later authenticated learner. It is not enforced for anonymous visitors, because there is no identity to count against.

## Limits and metrics

`LEARNING_RATE_LIMIT` defaults to 20 requests a minute for start and submit, clamped to 5–60. The bucket is the client address for that minute. It is not stored on the attempt. Counters are `learning.lesson.views`, `learning.quiz.started`, `learning.quiz.submitted`, and `learning.quiz.passed`. They have no lesson title tag.

Attempt rows are not written to the admin audit log.
