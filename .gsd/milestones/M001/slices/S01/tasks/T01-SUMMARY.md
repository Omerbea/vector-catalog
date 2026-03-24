---
id: T01
parent: S01
milestone: M001
provides:
  - Confirmed: `ai.gemini_embed` does NOT exist in pgai 0.11.2 (`timescale/timescaledb-ha:pg16`)
  - Confirmed: pgai 0.11.2 embedding providers are openai_embed, cohere_embed, ollama_embed, litellm_embed, voyageai_embed
  - Confirmed: `ai.litellm_embed(model text, input_text text, api_key text, ...)` exists and LiteLLM supports Gemini as a backend via `gemini/` model prefix
  - Confirmed: correct Docker tag is `timescale/timescaledb-ha:pg16` (not `pg16-latest`)
  - Confirmed: extensions install successfully: `vector`, `plpython3u`, `ai CASCADE`
key_files:
  - none (verification-only task)
key_decisions:
  - "ai.gemini_embed does not exist in pgai 0.11.2 — spec assumption is false"
  - "Viable path: ai.litellm_embed('gemini/gemini-embedding-2-preview', ...) with GOOGLE_API_KEY — LiteLLM proxies to Gemini, preserving DB-side zero-glue-code pattern"
patterns_established: []
drill_down_paths:
  - .gsd/milestones/M001/slices/S01/tasks/T01-PLAN.md
duration: 15min
verification_result: pass
completed_at: 2026-03-24T16:30:00Z
---

# T01: Container verification — confirm `ai.gemini_embed` exists

**`ai.gemini_embed` does NOT exist in pgai 0.11.2. Viable alternative: `ai.litellm_embed('gemini/...', ...)` which proxies to Gemini via LiteLLM.**

## What Happened

Pulled `timescale/timescaledb-ha:pg16` (the correct tag — `pg16-latest` does not exist). Started probe container, installed `vector`, `plpython3u`, and `ai CASCADE` extensions. Queried all `ai.*embed*` functions.

pgai version is **0.11.2**. The function `ai.gemini_embed` does not exist. Available embedding providers:

| Function | Provider |
|---|---|
| `ai.openai_embed` | OpenAI |
| `ai.cohere_embed` | Cohere |
| `ai.ollama_embed` | Ollama (local) |
| `ai.litellm_embed` | LiteLLM (proxy — supports 100+ providers) |
| `ai.voyageai_embed` | Voyage AI |

**Key finding:** `ai.litellm_embed(model text, input_text text, api_key text DEFAULT NULL, ...)` exists. LiteLLM supports Gemini as a backend via the `gemini/` model prefix (e.g., `gemini/gemini-embedding-2-preview` with a `GOOGLE_API_KEY`). This is a viable path to get Gemini embeddings from inside the DB trigger, preserving the zero-glue-code contract.

No Google/Gemini/Vertex named functions exist in the schema.

## Deviations

The spec's `ai.gemini_embed` function does not exist. Per D003, this was the stop condition. However, `ai.litellm_embed` with a Gemini model name is a functionally equivalent path that preserves the architecture's core intent.

## Files Created/Modified

- None (verification-only task)
