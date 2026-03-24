# T01: Container verification — confirm `ai.gemini_embed` exists

**Slice:** S01
**Milestone:** M001

## Goal

Pull `timescale/timescaledb-ha:pg16-latest`, start a container, install the `ai` extension, and confirm whether `ai.gemini_embed` exists in the function catalog. No API key required — purely a schema inspection.

## Must-Haves

### Truths
- `timescale/timescaledb-ha:pg16-latest` can be pulled and started
- `CREATE EXTENSION IF NOT EXISTS vector` succeeds
- `CREATE EXTENSION IF NOT EXISTS ai CASCADE` succeeds
- A query against `pg_proc` or `\df ai.*embed*` reveals whether `ai.gemini_embed` (or similar) exists
- The confirmed function name and signature are documented in the task summary

### Artifacts
- No files created — this is a verification-only task. Findings documented in T01-SUMMARY.md.

## Steps
1. Pull the image: `docker pull timescale/timescaledb-ha:pg16-latest`
2. Start a throwaway container: `docker run --rm -d --name pgai-probe -e POSTGRES_PASSWORD=test -p 5433:5432 timescale/timescaledb-ha:pg16-latest`
3. Wait for it to be ready
4. Connect and install extensions: `psql -h localhost -p 5433 -U postgres -c "CREATE EXTENSION IF NOT EXISTS vector; CREATE EXTENSION IF NOT EXISTS ai CASCADE;"`
5. List all functions in the `ai` schema containing "embed": `psql ... -c "SELECT proname, pronargs FROM pg_proc JOIN pg_namespace ON pg_proc.pronamespace = pg_namespace.oid WHERE nspname = 'ai' AND proname LIKE '%embed%';"`
6. If `gemini_embed` found: note the exact signature. If not found: list all `ai.*` functions to understand what embedding providers are available.
7. Check the pgai version installed: `psql ... -c "SELECT extversion FROM pg_extension WHERE extname = 'ai';"`
8. Stop and remove the probe container.
9. Document findings in T01-SUMMARY.md.

## Context
- This is the highest-risk task in the milestone. Everything else depends on its outcome.
- If `ai.gemini_embed` doesn't exist, per D003 we document the gap and stop — do not silently fall back.
- OpenAI's equivalent is `ai.openai_embed` — if only that exists, document it.
- The pgai repo's documented API key mechanism for OpenAI is `PGOPTIONS="-c ai.openai_api_key=..."` — check if a Gemini equivalent is visible in the function list or extension config.
