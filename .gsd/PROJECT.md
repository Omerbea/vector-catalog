# vector-catalog

## What This Is

A Spring Boot + PostgreSQL product catalog with semantic search. The distinguishing design choice is that embedding generation happens entirely inside the database via a `pgai` trigger that calls the Gemini API directly — the application layer inserts a row and the vector column populates automatically. Search also delegates to the DB: query embedding is generated inline in SQL via `ai.gemini_embed(...)`. The Spring Boot layer is intentionally thin.

## Core Value

<!-- This is the primary value anchor for prioritization and tradeoffs.
     If scope must shrink, this should survive. -->

The "zero glue code" contract: embedding generation is the database's job, not the application's. A plain `INSERT` produces a searchable vector. No background workers, no embedding clients in the application layer.

## Current State

Nothing built yet. Project directory created, GSD planning artifacts being written.

## Architecture / Key Patterns

- **Language / Framework:** Java 21, Spring Boot 3.4+
- **AI Integration:** Spring AI 1.1+ (used only for the search query embedding path if needed)
- **Database:** PostgreSQL 16 via `timescale/timescaledb-ha:pg16-latest` Docker image
- **Extensions:** `pgvector` (vector storage + cosine similarity operator `<=>`), `pgai` (in-DB Gemini API calls)
- **Embedding model:** `gemini-embedding-2-preview` (768 dimensions)
- **Trigger pattern:** `BEFORE INSERT OR UPDATE` trigger calls `ai.gemini_embed(...)` to populate `embedding VECTOR(768)` before the row commits
- **Entity mapping:** Hibernate 6.4+ `@JdbcTypeCode(SqlTypes.VECTOR)` for the vector column
- **Search:** Native `@Query` using `<=>` cosine distance operator, `ORDER BY embedding <=> ai.gemini_embed(...) LIMIT :limit`
- **Infrastructure:** Docker Compose with two services — `database` (Timescale) and `app` (Spring Boot). `GEMINI_API_KEY` passed through to DB container.
- **Key risk:** `ai.gemini_embed` function existence in the Timescale container is unverified. S01 proves or disproves this before any application code is written.

## Capability Contract

See `.gsd/REQUIREMENTS.md` for the explicit capability contract, requirement status, and coverage mapping.

## Milestone Sequence

- [ ] M001: vector-catalog MVP — DB-triggered Gemini embeddings, product CRUD, semantic search, working Docker Compose
