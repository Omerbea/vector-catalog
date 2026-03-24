---
id: M001
provides:
  - "DB-triggered Gemini embeddings via ai.litellm_embed — zero application-layer embedding code"
  - "products table: BIGSERIAL id, VARCHAR(255) name, TEXT description, DECIMAL(10,2) price, VECTOR(768) embedding"
  - "Trigger: trg_products_embed BEFORE INSERT OR UPDATE → auto_generate_embedding() → ai.litellm_embed('gemini/gemini-embedding-2-preview', ..., api_key_name => 'GOOGLE_API_KEY', extra_options => '{\"dimensions\": 768}')"
  - "HNSW index on products(embedding vector_cosine_ops)"
  - "Spring Boot 3.4 + Hibernate 6.6 + hibernate-vector:6.6.13.Final"
  - "Product entity: @JdbcTypeCode(SqlTypes.VECTOR) @Array(length=768) float[] embedding"
  - "POST /api/products → 201 Created (embedding auto-generated)"
  - "GET /api/products/{id} → 200/404"
  - "GET /api/products/search?q=...&limit=N → semantic similarity results, query embedding DB-side"
  - "docker compose up --build — both services start; 8 products auto-seeded on first boot"
  - "README with architecture explanation, quickstart, known limitations"
key_files:
  - docker/init.sql
  - compose.yaml
  - Dockerfile
  - .env.example
  - .gitignore
  - README.md
  - pom.xml
  - src/main/java/com/example/vectorcatalog/VectorCatalogApplication.java
  - src/main/java/com/example/vectorcatalog/DataSeeder.java
  - src/main/java/com/example/vectorcatalog/GlobalExceptionHandler.java
  - src/main/java/com/example/vectorcatalog/product/Product.java
  - src/main/java/com/example/vectorcatalog/product/ProductRepository.java
  - src/main/java/com/example/vectorcatalog/product/ProductService.java
  - src/main/java/com/example/vectorcatalog/product/ProductController.java
  - src/main/resources/application.yml
key_decisions:
  - "ai.gemini_embed absent → ai.litellm_embed('gemini/...') used instead (D006)"
  - "extra_options dimensions:768 required — model defaults to 3072 (D007)"
  - "GOOGLE_API_KEY env var resolved by pgai secrets.py automatically"
  - "BIGSERIAL not SERIAL — Hibernate Long/bigint type constraint"
  - "price: BigDecimal not Double — Hibernate scale constraint"
  - "Native query must SELECT embedding column even when excluded from response DTO"
  - "eclipse-temurin:21-jre-jammy as Dockerfile base"
  - "DataSeeder uses ApplicationRunner + count() to skip on restart"
verification_result: pass
completed_at: 2026-03-24T18:00:00Z
---

# M001: vector-catalog MVP

**Complete: DB-triggered Gemini embeddings, product CRUD, semantic search, Docker Compose stack — all working end-to-end. Ready for GitHub.**

## What Was Built

Four slices delivered the full milestone:

**S01** proved the infrastructure works. `ai.gemini_embed` doesn't exist in pgai 0.11.2 — `ai.litellm_embed('gemini/gemini-embedding-2-preview', ...)` is the correct path. The `GOOGLE_API_KEY` env var in the DB container is resolved automatically by pgai. A live INSERT with a real Gemini API key produces a 768-dim vector. Semantic search in SQL verified DB-side.

**S02** built the Spring Boot application layer. Two fixes emerged from integration: `price` must be `BigDecimal` (Hibernate rejects `@Column(scale)` on `Double`), and the table needs `BIGSERIAL` not `SERIAL` (Hibernate maps `Long` to `bigint`). After fixes: `POST /api/products` returns 201, embedding appears in DB, `GET /api/products/{id}` returns 200/404.

**S03** added semantic search. The native `@Query` calls `ai.litellm_embed` inline — query embedding generated DB-side. One quirk: Hibernate requires `embedding` in the `SELECT` list even when excluded from the response DTO. Fixed and verified: "tropical weather" → Tropical Sun Hat #1, Eversummer Jacket #2.

**S04** added error handling (`GlobalExceptionHandler`), auto-seeding (`DataSeeder` — 8 products on first boot), README with full architecture explanation, and `.gitignore`.

## Milestone Definition of Done — Verified

- ✓ `docker compose up --build` starts both services cleanly
- ✓ First boot seeds 8 products; each triggers a live Gemini API call
- ✓ `POST /api/products` returns 201, embedding column populated (768-dim)
- ✓ `GET /api/products/search?q=tropical+weather` returns Tropical Sun Hat #1
- ✓ `ai.gemini_embed` assumption documented — `ai.litellm_embed` used instead
- ✓ README explains the architecture and zero-glue-code philosophy
- ✓ Error paths return structured JSON
