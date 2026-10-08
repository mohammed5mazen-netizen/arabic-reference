# AI privacy

The assistant is anonymous. There is no visitor account and no stored conversation.

## What is not stored

`ai_usage` records a request id, time, provider, model identifier, status, latency, token counts when the provider returns them, and an evidence count. It has no question column and no answer column. Public questions are not written to the admin audit log.

Logs for an assistant request record the request id, status, evidence count, and duration. They do not record the question, the prompt, the provider body, or `AI_API_KEY`. The question is not a Micrometer tag.

## What the visitor sends

The browser may send the current question and up to two previous turns so a short follow-up can be phrased. Those turns live in the request only. They are not written to PostgreSQL or Redis as text. The optional answer cache stores the structured answer under a hash key, not under the question string, and it expires.

## What is not done

Questions are not used to train or fine-tune a model from this application. The server does not browse the web on the visitor's behalf. Provider calls, when enabled, send the system prompt, the selected excerpts, and the question to the configured endpoint. Disable the assistant, or leave the API key unset, and that call does not happen.

## Admin view

`ai.admin.view` shows aggregates: enabled, provider, model, prompt version, request counts, grounded and insufficient counts, errors, and latency. It does not show secrets or individual questions.
