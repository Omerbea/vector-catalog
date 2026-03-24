---
id: T03
parent: S01
milestone: M001
provides:
  - "Live trigger test PASSED: INSERT fires trigger → Gemini API called via LiteLLM → 768-dim vector stored"
  - "Semantic search VERIFIED: 'tropical weather' → Tropical Sun Hat #1, Eversummer Jacket #2"
  - "Fixed: gemini-embedding-2-preview defaults to 3072 dims; extra_options '{\"dimensions\": 768}' truncates to 768"
  - "Confirmed: init.sql runs cleanly on fresh container boot; all 5 test products embedded"
key_files:
  - docker/init.sql
  - compose.yaml
key_decisions:
  - "extra_options => '{\"dimensions\": 768}'::jsonb required in both trigger and search query to get 768-dim vectors from gemini-embedding-2-preview"
patterns_established:
  - "Full ai.litellm_embed call pattern: ai.litellm_embed('gemini/gemini-embedding-2-preview', text, api_key_name => 'GOOGLE_API_KEY', extra_options => '{\"dimensions\": 768}'::jsonb)"
drill_down_paths:
  - .gsd/milestones/M001/slices/S01/tasks/T03-PLAN.md
duration: 20min
verification_result: pass
completed_at: 2026-03-24T17:00:00Z
---

# T03: Live trigger test with real Gemini API key

**Trigger fires live: INSERT auto-embeds via Gemini/LiteLLM; 768-dim vector stored; semantic search returns correct results.**

## What Happened

Started the compose database service. Init SQL ran on first boot — all extensions, table, trigger, and HNSW index created cleanly.

**First INSERT failed:** `gemini-embedding-2-preview` defaults to 3072 dimensions, not 768. Fixed by adding `extra_options => '{"dimensions": 768}'::jsonb` to the `ai.litellm_embed` call in the trigger function (confirmed that LiteLLM passes this through to the Gemini API's `output_dimensionality` parameter).

**After fix:** Restarted with fresh volume. All 5 test products inserted successfully — each triggering a live Gemini API call from inside PostgreSQL, storing a 768-dim vector. Zero application code.

**Semantic search verified in SQL:** `ORDER BY embedding <=> ai.litellm_embed(...)` with query "tropical weather" correctly ranked Tropical Sun Hat first, Eversummer Jacket second.

## Deviations

`extra_options => '{"dimensions": 768}'::jsonb` required on both the trigger and the search query — not mentioned in the original spec. Init.sql updated accordingly.

## Files Created/Modified

- `docker/init.sql` — updated trigger to include `extra_options => '{"dimensions": 768}'::jsonb`
