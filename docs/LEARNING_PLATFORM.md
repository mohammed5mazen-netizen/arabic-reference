# Learning platform

Published reference knowledge stays in its own modules. A learning path points at it and adds the teaching sequence: path, unit, lesson, activity, question.

The public pages are `/learn`, `/learn/{pathSlug}`, and `/learn/{pathSlug}/{lessonSlug}`. Reading them does not require an account. Drafts are not on those routes. Staff preview uses `GET /api/v1/admin/learning/paths/{id}` with `learning.lesson.view`.

## Public API

- `GET /api/v1/public/learning/paths`
- `GET /api/v1/public/learning/paths/{slug}`
- `GET /api/v1/public/learning/lessons/{slug}`
- `POST /api/v1/public/learning/quizzes/{id}/attempts`
- `POST /api/v1/public/learning/attempts/{token}/submit`

The lesson payload includes objectives, sections, resolved reference cards, activities, and a quiz summary (`id`, title, passing score, question count). It does not include options or which option is correct.

## Hierarchy

A path has ordered units. A unit has ordered lessons. A lesson has ordered objectives, sections, references, and activities. Display orders are contiguous from 1. A quiz belongs to one lesson or one unit, not both.

Difficulties are `BEGINNER`, `INTERMEDIATE`, and `ADVANCED`. Visitors see مبتدئ، متوسط، and متقدم.

Section types are introduction, explanation, example, note, warning, summary, reference, and activity. Activity types are read, multiple choice, true/false, matching, and classification. Matching and classification are prompts in the lesson. They are not a game engine.

## Knowledge references

A reference is a kind plus the slug of a published record: dictionary entry, grammar rule, concept, or topic, spelling rule, rhetoric device, article, or the morphology tool (`analyze`). `PublishedReferencePort` resolves it. An unknown slug is rejected at edit time. The public page resolves the link again, so the card follows the latest published title. If that record is no longer public, the card says the reference is no longer published. An optional note on the reference is editorial text and is snapshotted with the lesson.

## Editorial text

The path row keeps a working copy and a `published_snapshot`. Publishing copies the teaching text into that snapshot and indexes it. A later edit reopens the path as a draft and leaves the snapshot in place, so the public page and any open quiz stay on the published revision until the next publish. Revisions are rows in `learning_revision`.

The lifecycle is the shared one: draft, in review, changes requested, verified, published, archived. The creator cannot verify the path. The reviewer cannot publish it.

## Search

Publish indexes the path and each lesson from the title and summary. Archive removes those documents. `indexVersion` is 3. See [SEARCH_ARCHITECTURE.md](SEARCH_ARCHITECTURE.md).

## What this stage does not do

There is no learner account, no email verification, and no server-side progress table. Anonymous progress stays in the browser. See [LEARNING_PROGRESS.md](LEARNING_PROGRESS.md). Quizzes are scored in code, not by the assistant. See [QUIZ_ENGINE.md](QUIZ_ENGINE.md).
