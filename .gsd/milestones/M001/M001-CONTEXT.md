# M001: vector-catalog MVP — Context

**Gathered:** 2026-03-24
**Status:** Ready for planning

## Project Description

A Spring Boot + PostgreSQL semantic product catalog. The core innovation is "zero glue code": the database, not the application, is responsible for generating vector embeddings. Every product insert fires a `BEFORE` trigger that calls `ai.gemini_embed(...)` directly from inside PostgreSQL via the `pgai` extension. The Spring Boot layer is intentionally thin — it does plain JPA inserts and issues native SQL for search.

## Why This Milestone

This is the only milestone. The full project vision is: working DB-triggered embeddings, a product CRUD REST API, semantic search with cosine similarity, and a reproducible Docker Compose setup — all in one deliverable that can be pushed to GitHub as a reference project.

## User-Visible Outcome

### When this milestone is complete, the user can:

- Run `docker compose up` and have both services start cleanly
- `POST /api/products` with `{name, description, price}` and have the embedding auto-populated in the DB with no application-side embedding code
- `GET /api/products/search?q=tropical+weather` and get back "Eversummer Jacket" because the DB understands semantic meaning
- Read a README that explains the architecture and the "zero glue code" philosophy

### Entry point / environment

- Entry point: HTTP REST API at `localhost:8080`
- Environment: local dev via Docker Compose
- Live dependencies involved: Google Gemini API (via `GEMINI_API_KEY`)

## Completion Class

- Contract complete means: all REST endpoints return correct shapes; trigger populates embedding column on insert; search returns semantically relevant results
- Integration complete means: `docker compose up` wires DB + app; trigger fires against live Gemini API with a real key
- Operational complete means: `GEMINI_API_KEY` plumbed through to DB container; app connects to DB on startup; init SQL runs on first boot

## Final Integrated Acceptance

To call this milestone complete, we must prove:

- `POST /api/products` with "Eversummer Jacket / Lightweight breathable windbreaker for humid climates" results in a non-null 768-dim vector in the DB
- `GET /api/products/search?q=tropical+weather` returns that product in the top result
- `docker compose up` from a clean state starts both services and passes both checks above

## Risks and Unknowns

- `ai.gemini_embed` function existence in `timescale/timescaledb-ha:pg16-latest` — unverified. S01 must confirm before any application code is written. If it doesn't exist, project documents the gap and stops rather than silently falling back to application-layer embeddings.
- Exact environment variable name for passing Gemini API key to the pgai extension (vs `PGOPTIONS="-c ai.openai_api_key=..."` which is the documented OpenAI path) — must be confirmed in S01.
- Insert latency increase from synchronous trigger — acknowledged and acceptable for a catalog demo. Worth noting in the README.

## Existing Codebase / Prior Art

- No existing codebase. Greenfield project in `/Users/omer/Projects/OpenSource/vector-catalog/`.

> See `.gsd/DECISIONS.md` for all architectural and pattern decisions — it is an append-only register; read it during planning, append to it during execution.

## Relevant Requirements

- R002 — core capability; this milestone is entirely built around proving it
- R004 — the "zero glue code" constraint shapes every architectural decision
- R001, R003 — the two user-facing endpoints this milestone delivers
- R005 — Docker Compose is how the whole thing is exercised

## Scope

### In Scope

- `compose.yaml` with Timescale DB + Spring Boot app services
- SQL init script: extensions, `products` table, embedding trigger, HNSW index
- Spring Boot 3.4+ project: entity, repository, service, controller, `application.yml`
- `pom.xml` with all necessary dependencies (Spring Boot, Spring Data JPA, Hibernate Vector, PostgreSQL driver, Spring AI if needed for search query path)
- `POST /api/products` and `GET /api/products/search?q=...` endpoints
- Sample seed data that runs on startup
- README with architecture explanation and quickstart

### Out of Scope / Non-Goals

- Authentication or authorization
- Pagination on search results (fixed LIMIT is fine)
- Product update/delete endpoints (not in the spec)
- Any non-Gemini embedding model
- Deployment beyond local Docker Compose

## Technical Constraints

- Hibernate 6.4+ required for native `@JdbcTypeCode(SqlTypes.VECTOR)` support
- Spring Boot 3.4+ (ships with Hibernate 6.6)
- The embedding column must be mapped with `@JdbcTypeCode(SqlTypes.VECTOR)` + `@Array(length = 768)` — do not use the `pgvector-java` library, Hibernate handles it natively
- Native `@Query` required for the `<=>` cosine distance operator — Spring Data method name derivation cannot express this
- `ai.gemini_embed` function name and signature must be verified against the actual container before hardcoding in the trigger

## Integration Points

- Google Gemini API — called from inside PostgreSQL via `ai.gemini_embed(...)` in the trigger, and from inside SQL in the search query
- `timescale/timescaledb-ha:pg16-latest` Docker image — must have `pgai` and `pgvector` extensions available

## Open Questions

- Does `timescale/timescaledb-ha:pg16-latest` include `ai.gemini_embed` for Gemini? — S01 verifies this first thing. Fallback decision: fail fast with a documented error, do not silently switch to application-layer embeddings.
- What is the correct `PGOPTIONS` or env var name for the Gemini API key in pgai? — Research in S01 before writing the trigger.
