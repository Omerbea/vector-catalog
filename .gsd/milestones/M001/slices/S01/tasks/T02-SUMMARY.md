---
id: T02
parent: S01
milestone: M001
provides:
  - "docker/init.sql — extensions, products table, litellm_embed trigger, HNSW index"
  - "compose.yaml — database service with Timescale image, GOOGLE_API_KEY env var, health check, init.sql mount"
  - ".env.example — documents GOOGLE_API_KEY, POSTGRES_PASSWORD, SPRING_DATASOURCE_* vars"
  - "Confirmed: GOOGLE_API_KEY env var in container is read automatically by ai.secrets resolve chain (no PGOPTIONS needed)"
  - "Confirmed: init.sql DDL runs cleanly — table, trigger function, trigger, HNSW index all created without error"
key_files:
  - docker/init.sql
  - compose.yaml
  - .env.example
key_decisions:
  - "api_key_name='GOOGLE_API_KEY' in ai.litellm_embed — pgai reads env var GOOGLE_API_KEY from container automatically via secrets.py resolve chain"
  - "HNSW index placed in init.sql, not Hibernate DDL — Hibernate set to validate, not update"
  - "DB name: vectorcatalog (clean name, not default postgres)"
patterns_established:
  - "ai.litellm_embed('gemini/gemini-embedding-2-preview', text, api_key_name => 'GOOGLE_API_KEY') is the trigger call pattern"
drill_down_paths:
  - .gsd/milestones/M001/slices/S01/tasks/T02-PLAN.md
duration: 20min
verification_result: pass
completed_at: 2026-03-24T16:45:00Z
---

# T02: Write init.sql and compose.yaml database service

**init.sql + compose.yaml written; DDL verified clean; GOOGLE_API_KEY env var resolution confirmed — no PGOPTIONS needed.**

## What Happened

Inspected the pgai 0.11.2 `secrets.py` source directly in the container. The `reveal_secret` function checks env vars automatically when `api_key_name` is provided — the container's `GOOGLE_API_KEY` env var is read without any session parameter setup. This simplifies the compose config significantly.

Wrote `docker/init.sql` using `ai.litellm_embed('gemini/gemini-embedding-2-preview', ..., api_key_name => 'GOOGLE_API_KEY')`. Ran the full DDL against a live container — all objects created without error.

`compose.yaml` uses the correct `timescale/timescaledb-ha:pg16` tag, mounts `init.sql` into `docker-entrypoint-initdb.d/`, and passes `GOOGLE_API_KEY` as a container env var. App service will be added in S02.

## Deviations

None from the plan.

## Files Created/Modified

- `docker/init.sql` — extensions, products table, embedding trigger via litellm_embed, HNSW index
- `compose.yaml` — database service only (app service added in S02)
- `.env.example` — all required env vars documented
