# AI assistant

The linguistic assistant answers a public question from published reference knowledge. It is not a general chatbot and it does not browse the internet.

The public page is `/assistant` (المساعد اللغوي). The label is **إجابات موثقة من محتوى المرجع**. The API is anonymous:

- `GET /api/v1/public/ai/status`
- `POST /api/v1/public/ai/ask`

A question is optional prior turns (at most two). The server does not store the conversation. See [AI_PRIVACY.md](AI_PRIVACY.md).

## Disabled mode

`AI_ENABLED` defaults to `false`, and `AI_PROVIDER` defaults to `none`. The assistant is also unavailable when the provider is `openai-compatible` and `AI_API_KEY` is blank. The rest of the site keeps working. `/assistant` shows:

**المساعد الذكي غير متاح حاليًا. يمكنك استخدام البحث والأدوات اللغوية.**

`POST /api/v1/public/ai/ask` then returns `503`.

## Pipeline

1. Validate length (Unicode code points and lines).
2. Normalize Arabic and classify the intent in code.
3. Retrieve published evidence.
4. Rank, deduplicate, and fit a context budget.
5. If nothing usable remains, return the insufficient-evidence sentence and do not call a model.
6. Otherwise call the model with the evidence block only.
7. Accept only evidence ids that were retrieved. The backend attaches the real URLs.

Intents: `WORD_MEANING`, `ROOT`, `MORPHOLOGY`, `GRAMMAR`, `SPELLING`, `RHETORIC`, `LITERATURE`, `COMPARISON`, `LESSON_HELP`, and `GENERAL_LINGUISTIC`. An unclear question uses unified search. The backend chooses the route. There is no tool-calling loop.

A lesson or learning path found in search is capped at the search score for a reference question, below an exact dictionary hit and below a grammar-domain hit. `LESSON_HELP` (a question that names a درس) searches only published lessons and paths and may use that lesson as the main evidence, still without calling a second model pipeline. The assistant does not score quizzes.

## Configuration

| Variable | Default | Role |
| --- | --- | --- |
| `AI_ENABLED` | `false` | Master switch |
| `AI_PROVIDER` | `none` | `none`, `stub` (tests), or `openai-compatible` |
| `AI_MODEL` | empty | Model identifier sent to the provider |
| `AI_API_KEY` | empty | Environment only. Never committed, logged, or returned |
| `AI_BASE_URL` | OpenAI chat completions URL | Compatible endpoint |
| `AI_TIMEOUT` | `20s` | Provider call timeout |
| `AI_MAX_OUTPUT_TOKENS` | `400` | Output cap |
| `AI_MAX_CONTEXT_ITEMS` | `6` | Evidence items in the prompt |
| `AI_MAX_EXCERPT_CHARS` | `280` | Excerpt length |
| `AI_MAX_QUESTION_CODE_POINTS` | `400` | Question size |
| `AI_MAX_QUESTION_LINES` | `8` | Question lines |
| `AI_RATE_LIMIT` | `12` | Requests per minute per client address |
| `AI_CACHE_ENABLED` | `true` | Redis cache of a grounded or insufficient answer |
| `AI_CACHE_TTL` | `10m` | Cache lifetime |

The admin page `/admin/ai` requires `ai.admin.view`. It shows enabled state, provider, model identifier, prompt version `AR_ASSISTANT_V1`, and aggregate counts. It does not show the API key and cannot change it.

## Answers

The response carries `answer`, `citations`, `evidence`, `limitations`, `grounding`, `grounded`, `uncertain`, and `requestId`. Grounding is `GROUNDED`, `PARTIALLY_GROUNDED`, or `INSUFFICIENT_EVIDENCE`. `grounded` is true only after citation validation. A rule-derived morphology candidate stays labeled as a possible analysis. The page renders the answer as text. It does not inject HTML.

A timeout or provider failure returns:

**تعذر إكمال الإجابة الآن. جرّب مرة أخرى بعد قليل أو استخدم البحث.**
