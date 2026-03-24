---
id: T01
parent: S03
milestone: M001
provides:
  - "ProductRepository.findSemanticMatches(String query, int limit) — native @Query with full SELECT including embedding column"
  - "ProductService.search(String query, int limit)"
  - "GET /api/products/search?q={text}&limit={n} — default limit 5"
  - "Semantic search verified: 'tropical weather' → Tropical Sun Hat #1, Eversummer Jacket #2"
  - "Semantic search verified: 'winter protection' → Arctic Shield Parka #1"
key_files:
  - src/main/java/com/example/vectorcatalog/product/ProductRepository.java
  - src/main/java/com/example/vectorcatalog/product/ProductService.java
  - src/main/java/com/example/vectorcatalog/product/ProductController.java
key_decisions:
  - "Native query must SELECT embedding column even though it's not in the response DTO — Hibernate entity mapping requires all columns to be present in the ResultSet"
patterns_established:
  - "Native @Query pattern for vector similarity search with ai.litellm_embed inline"
drill_down_paths:
  - .gsd/milestones/M001/slices/S03/tasks/T01-PLAN.md
duration: 20min
verification_result: pass
completed_at: 2026-03-24T17:35:00Z
---

# T01: Search repository method, service, and endpoint

**Semantic search live: 'tropical weather' → Tropical Sun Hat, Eversummer Jacket. 'winter protection' → Arctic Shield Parka. DB-side query embedding confirmed.**

## What Happened

Added `findSemanticMatches` to `ProductRepository` with a native `@Query`. First attempt selected only `id, name, description, price` — Hibernate threw `embedding column not found in ResultSet` because it tries to map the full entity. Fixed by adding `embedding` to the SELECT list.

Added `search()` to `ProductService` and `GET /api/products/search?q=...&limit=...` to `ProductController`.

Verified both test queries against a live 5-product dataset. Query embeddings are generated DB-side — the Spring layer issues a plain parameterized SQL call with no embedding SDK.

## Deviations

- Must SELECT `embedding` in native query even though it's excluded from response DTO — Hibernate entity mapping requirement.

## Files Created/Modified

- `ProductRepository.java` — findSemanticMatches native query
- `ProductService.java` — search() method
- `ProductController.java` — GET /api/products/search endpoint
