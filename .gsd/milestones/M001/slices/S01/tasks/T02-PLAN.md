# T02: Write init.sql and compose.yaml database service

**Slice:** S01
**Milestone:** M001

## Goal

Write the SQL init script and Docker Compose database service definition using the confirmed `ai.litellm_embed` function signature from T01.

## Must-Haves

### Truths
- `init.sql` runs cleanly on a fresh Timescale container (no errors)
- `products` table created with `embedding VECTOR(768)` column
- Trigger fires on INSERT and calls `ai.litellm_embed('gemini/gemini-embedding-2-preview', ...)`
- `compose.yaml` starts database service; `GOOGLE_API_KEY` is plumbed through
- `.env.example` documents required env vars

### Artifacts
- `docker/init.sql` — extensions, table, trigger function, trigger (real implementation)
- `compose.yaml` — database service stanza (app service added in S02)
- `.env.example` — env var template

## Steps
1. Write `docker/init.sql`: extensions, products table, trigger function using `ai.litellm_embed`, trigger
2. Write `compose.yaml` with database service only (app service added in S02)
3. Write `.env.example`
4. Test the init SQL runs cleanly against a fresh container (without a real API key — just verify SQL parses and trigger installs)

## Context
- From T01: function is `ai.litellm_embed(model text, input_text text, api_key text DEFAULT NULL, api_key_name text DEFAULT NULL, ...)`
- API key passed via `api_key_name` referencing a GUC set in session or via `ai.set_secret` — need to figure out which mechanism to use in the trigger
- The trigger runs inside a DB transaction — the API key must be accessible from within the trigger function context
- `GOOGLE_API_KEY` env var in the container should be readable via `current_setting('ai.google_api_key', true)` if set via `PGOPTIONS`, or we pass it directly in the trigger via `api_key_name`
