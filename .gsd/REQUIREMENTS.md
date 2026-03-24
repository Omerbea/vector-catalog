# Requirements

This file is the explicit capability and coverage contract for the project.

## Active

### R001 — Product ingestion via REST API
- Class: primary-user-loop
- Status: active
- Description: `POST /api/products` accepts `{name, description, price}` and persists the product. Response includes the created product with its ID.
- Why it matters: The primary write path. Everything downstream (embedding, search) depends on products existing.
- Source: user
- Primary owning slice: M001/S02
- Supporting slices: M001/S01
- Validation: unmapped
- Notes: Payload does not include embedding — that is the DB's job.

### R002 — DB-side automatic embedding generation via trigger
- Class: core-capability
- Status: active
- Description: Every `INSERT` or `UPDATE` on the `products` table fires a `BEFORE` trigger that calls `ai.gemini_embed('gemini-embedding-2-preview', name || ': ' || description)` and populates the `embedding VECTOR(768)` column. No application code handles embedding generation for the write path.
- Why it matters: The "zero glue code" contract. If this doesn't work, the project's core premise is invalid.
- Source: user
- Primary owning slice: M001/S01
- Supporting slices: none
- Validation: unmapped
- Notes: `ai.gemini_embed` existence in the Timescale container is the #1 risk. S01 verifies before any app code is written. If unverified, project documents gap clearly and stops.

### R003 — Semantic search via cosine similarity
- Class: primary-user-loop
- Status: active
- Description: `GET /api/products/search?q=<text>` returns products ordered by cosine similarity to the query. The query embedding is generated inline in SQL via `ai.gemini_embed(...)`. Returns top N results (default 5).
- Why it matters: The user-facing value. "Find by vibe" — searching "tropical weather" returns "Eversummer Jacket" because Gemini understands semantic links.
- Source: user
- Primary owning slice: M001/S03
- Supporting slices: M001/S01, M001/S02
- Validation: unmapped
- Notes: Cosine similarity operator `<=>`. Query embedding generated DB-side to maintain zero-glue-code pattern.

### R004 — Zero application-layer embedding code for ingestion
- Class: differentiator
- Status: active
- Description: The Spring Boot application has no dependency on any Gemini/OpenAI SDK for the write path. It issues a plain `INSERT` via JPA. The DB trigger handles all embedding logic.
- Why it matters: The architectural differentiator of this project. Losing this loses the point.
- Source: user
- Primary owning slice: M001/S01
- Supporting slices: M001/S02
- Validation: unmapped
- Notes: Spring AI may still be on the classpath for the search query path — that's acceptable. It must not be used on the ingestion path.

### R005 — Docker Compose local dev environment
- Class: operability
- Status: active
- Description: `docker compose up` starts `database` (Timescale pg16 with pgai/pgvector) and `app` (Spring Boot). `GEMINI_API_KEY` is plumbed through to the DB container. No manual setup steps beyond copying `.env`.
- Why it matters: The project must be reproducible from a `git clone`. This is a GitHub reference project.
- Source: user
- Primary owning slice: M001/S01
- Supporting slices: M001/S04
- Validation: unmapped
- Notes: `GEMINI_API_KEY` passed as env var to the Timescale container. Exact parameter name for Gemini (vs `ai.openai_api_key` for OpenAI) must be confirmed in S01.

### R006 — HNSW vector index for query performance
- Class: quality-attribute
- Status: active
- Description: An HNSW index is created on `products(embedding)` using `vector_cosine_ops` to make similarity queries efficient.
- Why it matters: Without an index, search is a sequential scan. Fine for a demo with 10 products, slow at any real scale.
- Source: inferred
- Primary owning slice: M001/S03
- Supporting slices: none
- Validation: unmapped
- Notes: Applied in the SQL init script, not managed by Hibernate schema generation.

## Deferred

### R007 — Multimodal search (image URL embeddings)
- Class: differentiator
- Status: deferred
- Description: `ai.gemini_embed` accepting image URLs to enable "search by photo" functionality.
- Why it matters: Mentioned in the spec as a natural evolution once text embeddings work.
- Source: user
- Primary owning slice: none
- Supporting slices: none
- Validation: unmapped
- Notes: Deferred until text embedding is proven to work. Depends on Gemini multimodal support in pgai.

## Out of Scope

### R008 — Background embedding workers
- Class: anti-feature
- Status: out-of-scope
- Description: No Celery, RabbitMQ, or any async worker infrastructure for embedding generation.
- Why it matters: Explicitly excluded by the "zero glue code" philosophy. Adding workers defeats the purpose.
- Source: user
- Primary owning slice: none
- Supporting slices: none
- Validation: n/a
- Notes: The synchronous trigger approach is the intentional design. Insert latency increase is acknowledged and acceptable.

### R009 — Spring AI VectorStore abstraction for ingestion
- Class: anti-feature
- Status: out-of-scope
- Description: Spring AI's `VectorStore` / `PgVectorStore` abstraction is not used for the write path. Plain JPA `save()` is the ingestion mechanism.
- Why it matters: Using Spring AI's VectorStore would move embedding generation back into the application layer, violating R004.
- Source: user
- Primary owning slice: none
- Supporting slices: none
- Validation: n/a
- Notes: Spring AI VectorStore may be referenced for comparison/documentation purposes only.

## Traceability

| ID | Class | Status | Primary owner | Supporting | Proof |
|---|---|---|---|---|---|
| R001 | primary-user-loop | active | M001/S02 | M001/S01 | unmapped |
| R002 | core-capability | active | M001/S01 | none | unmapped |
| R003 | primary-user-loop | active | M001/S03 | M001/S01, M001/S02 | unmapped |
| R004 | differentiator | active | M001/S01 | M001/S02 | unmapped |
| R005 | operability | active | M001/S01 | M001/S04 | unmapped |
| R006 | quality-attribute | active | M001/S03 | none | unmapped |
| R007 | differentiator | deferred | none | none | unmapped |
| R008 | anti-feature | out-of-scope | none | none | n/a |
| R009 | anti-feature | out-of-scope | none | none | n/a |

## Coverage Summary

- Active requirements: 6
- Mapped to slices: 6
- Validated: 0
- Unmapped active requirements: 0
