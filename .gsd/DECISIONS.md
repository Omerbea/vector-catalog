# Decisions Register

<!-- Append-only. Never edit or remove existing rows.
     To reverse a decision, add a new row that supersedes it.
     Read this file at the start of any planning or research phase. -->

| # | When | Scope | Decision | Choice | Rationale | Revisable? | Made By |
|---|------|-------|----------|--------|-----------|------------|---------|
| D001 | M001 | arch | Embedding generation location | Database trigger via `ai.gemini_embed(...)` | "Zero glue code" — the DB is the brain; no embedding SDK in application write path; sync guarantee means no row exists without an embedding | Yes — if `ai.gemini_embed` confirmed absent in container, revisit | human |
| D002 | M001 | library | Vector column Hibernate mapping | `@JdbcTypeCode(SqlTypes.VECTOR)` + `@Array(length = 768)` (Hibernate 6.4+ native) | Hibernate 6.4+ has native vector module; no need for `pgvector-java` library; cleaner integration with Spring Boot 3.4+ | No | agent |
| D003 | M001 | arch | Fallback if `ai.gemini_embed` absent | Fail fast with documented error; do not silently switch to application-layer embeddings | Preserving the architectural intent is more valuable for a reference project than silently degrading | Yes — if user changes priorities | human |
| D004 | M001 | api | Search similarity metric | Cosine similarity (`<=>` operator) | Standard for text embeddings; pgvector supports it natively; matches spec | No | human |
| D005 | M001 | convention | Search query embedding | Generated DB-side inline in native SQL via `ai.gemini_embed(...)` | Maintains zero-glue-code pattern for both write and read paths | Yes — if DB-side latency becomes a concern | agent |
| D006 | M001/S01 | arch | Gemini embedding function (supersedes D001 and D003) | `ai.litellm_embed('gemini/gemini-embedding-2-preview', text, api_key => ...)` | `ai.gemini_embed` does not exist in pgai 0.11.2. LiteLLM proxy function exists and supports Gemini via `gemini/` model prefix. Preserves DB-trigger zero-glue-code pattern exactly. API key passed as `GOOGLE_API_KEY` session param. | Yes — if pgai adds native gemini_embed in a future version | human |
| D007 | M001/S01 | convention | Gemini embedding dimensions | `extra_options => '{"dimensions": 768}'::jsonb` on every `ai.litellm_embed` call | `gemini-embedding-2-preview` defaults to 3072 dims; `dimensions` in extra_options truncates to 768 matching the column type and spec intent | No — changing dims would require recreating the vector column and re-embedding all rows | agent |
