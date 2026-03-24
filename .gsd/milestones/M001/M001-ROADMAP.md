# M001: vector-catalog MVP

**Vision:** A Spring Boot + PostgreSQL product catalog where the database — not the application — handles all embedding generation via a `pgai` trigger calling Gemini directly. A plain `INSERT` produces a searchable 768-dim vector. No background workers, no embedding SDK in the application write path.

## Success Criteria

- `docker compose up` starts database and app cleanly with no manual steps beyond providing `GEMINI_API_KEY`
- `POST /api/products` triggers automatic embedding generation — the `embedding` column is non-null after insert
- `GET /api/products/search?q=tropical+weather` returns "Eversummer Jacket" in top results
- The Spring Boot service has zero embedding-generation code in the write path
- `ai.gemini_embed` function is confirmed to exist in the Timescale container, or the gap is explicitly documented

## Key Risks / Unknowns

- `ai.gemini_embed` function existence in `timescale/timescaledb-ha:pg16-latest` — the entire architecture depends on this. Unverified before S01.
- Correct mechanism for passing `GEMINI_API_KEY` to the pgai extension inside the DB container — OpenAI uses `PGOPTIONS="-c ai.openai_api_key=..."`, Gemini equivalent is unknown.

## Proof Strategy

- `ai.gemini_embed` unverified → retire in S01 by pulling the container, running `CREATE EXTENSION ai CASCADE`, and calling `SELECT ai.gemini_embed(...)` directly in psql with a real key
- Gemini API key mechanism unknown → retire in S01 by testing both env var and session-parameter approaches in the container

## Verification Classes

- Contract verification: REST endpoint response shapes verified via curl; DB column state verified via psql query
- Integration verification: trigger fires against live Gemini API with real key; `docker compose up` wires both services
- Operational verification: init SQL runs on first container boot; app connects to DB on startup with retry
- UAT / human verification: semantic search relevance ("tropical weather" → "Eversummer Jacket") requires human judgment

## Milestone Definition of Done

This milestone is complete only when all are true:

- All four slices are complete (S01 infrastructure proof, S02 CRUD API, S03 semantic search, S04 polish)
- `docker compose up` from clean state starts both services without error
- `POST /api/products` populates the `embedding` column via trigger (verified with `SELECT id, embedding IS NOT NULL FROM products`)
- `GET /api/products/search?q=tropical+weather` returns semantically relevant results
- README explains the architecture and has a working quickstart
- `ai.gemini_embed` assumption is verified or gap is documented

## Requirement Coverage

- Covers: R001, R002, R003, R004, R005, R006
- Partially covers: none
- Leaves for later: R007 (multimodal)
- Orphan risks: none

## Slices

- [x] **S01: Infrastructure + DB embedding proof** `risk:high` `depends:[]`
  > After this: The Timescale container is running locally with `pgvector` and `pgai` extensions; `ai.gemini_embed` is confirmed to exist and fire via trigger; a test product row has a real 768-dim vector in the `embedding` column. Verified via psql, not via the application.

- [x] **S02: Product CRUD API** `risk:medium` `depends:[S01]`
  > After this: Spring Boot app builds and starts; `POST /api/products` saves a product; the DB trigger auto-populates the embedding; `GET /api/products/{id}` returns the product. Verified via curl against the running app.

- [x] **S03: Semantic search endpoint + HNSW index** `risk:medium` `depends:[S02]`
  > After this: `GET /api/products/search?q=tropical+weather` returns semantically relevant results using cosine similarity; HNSW index is in place; query embedding is generated DB-side in the SQL.

- [x] **S04: Polish — README, seed data, error handling** `risk:low` `depends:[S03]`
  > After this: Sample products seed on startup; all error paths return clean JSON; README has architecture diagram, quickstart, and the "zero glue code" explanation. Project is ready to push to GitHub.

<!--
  Format rules (parsers depend on this exact structure):
  - Checkbox line: - [ ] **S01: Title** `risk:high|medium|low` `depends:[S01,S02]`
  - Demo line:     >  After this: one sentence showing what's demoable
  - Mark done:     change [ ] to [x]
-->

## Boundary Map

### S01 → S02

Produces:
- `compose.yaml` — database service definition with Timescale image, `GEMINI_API_KEY` env var, port 5432
- `init.sql` — `CREATE EXTENSION vector`, `CREATE EXTENSION ai CASCADE`, `products` table, `auto_generate_gemini_embedding()` function, `trg_products_ai_embed` trigger
- Confirmed `ai.gemini_embed('gemini-embedding-2-preview', text)` function signature and API key mechanism
- A running PostgreSQL instance with extensions active and trigger in place

Consumes:
- nothing (first slice)

### S02 → S03

Produces:
- `pom.xml` — full dependency set (Spring Boot 3.4+, Spring Data JPA, Hibernate Vector 6.4+, PostgreSQL JDBC driver, Spring Web)
- `Product.java` — JPA entity with `@JdbcTypeCode(SqlTypes.VECTOR)` + `@Array(length = 768)` on `embedding` field
- `ProductRepository.java` — `JpaRepository<Product, Long>` with stub for search method
- `ProductService.java` — `save(ProductRequest)` method
- `ProductController.java` — `POST /api/products`, `GET /api/products/{id}`
- `application.yml` — datasource config, JPA dialect, Hibernate DDL validation
- `POST /api/products` → `201 Created` with product JSON (no embedding in response body)
- `GET /api/products/{id}` → `200 OK` with product JSON

Consumes from S01:
- Running DB with trigger in place (S02 app connects to it)
- Confirmed `VECTOR(768)` column type and extension names

### S03 → S04

Produces:
- `ProductRepository.findSemanticMatches(String userQuery, int limit)` — native `@Query` using `ORDER BY embedding <=> ai.gemini_embed('gemini-embedding-2-preview', :userQuery) LIMIT :limit`
- HNSW index on `products(embedding vector_cosine_ops)` in `init.sql`
- `GET /api/products/search?q={text}&limit={n}` endpoint (default limit 5)
- Verified semantic search: "tropical weather" → "Eversummer Jacket" in top results

Consumes from S02:
- `Product` entity, `ProductRepository`, `ProductService`, `ProductController` base
- Running app with `POST /api/products` working

### S04 (terminal)

Produces:
- `DataSeeder.java` — `@PostConstruct` or `CommandLineRunner` that inserts sample products if table is empty
- Error handling: `@ControllerAdvice` returning `{"error": "message"}` JSON for 400/404/500
- `README.md` — architecture section, quickstart (`docker compose up`, curl examples), "zero glue code" explanation, known limitations (insert latency, `ai.gemini_embed` assumption note)
- Final `compose.yaml` with `app` service including `Dockerfile` reference and health check

Consumes from S03:
- All prior application code; fully working search endpoint
