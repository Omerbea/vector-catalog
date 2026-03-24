---
id: S03
milestone: M001
provides:
  - "GET /api/products/search?q={text}&limit={n} — semantic similarity search, default limit 5"
  - "ProductRepository.findSemanticMatches — native query with ai.litellm_embed inline, HNSW-indexed"
  - "Verified: 'tropical weather' → Tropical Sun Hat #1, Eversummer Jacket #2"
  - "Verified: 'winter protection' → Arctic Shield Parka #1"
requires:
  - slice: S02
    provides: Product entity, ProductRepository, ProductService, ProductController, running compose stack
affects: [S04]
key_files:
  - src/main/java/com/example/vectorcatalog/product/ProductRepository.java
  - src/main/java/com/example/vectorcatalog/product/ProductService.java
  - src/main/java/com/example/vectorcatalog/product/ProductController.java
key_decisions:
  - "Native query must SELECT embedding — Hibernate maps full entity from ResultSet"
patterns_established:
  - "Search native query: SELECT id, name, description, price, embedding FROM products ORDER BY embedding <=> ai.litellm_embed(...) LIMIT :limit"
drill_down_paths:
  - .gsd/milestones/M001/slices/S03/tasks/T01-SUMMARY.md
verification_result: pass
completed_at: 2026-03-24T17:40:00Z
---

# S03: Semantic search endpoint + HNSW index

**Semantic search live end-to-end: GET /api/products/search?q=tropical+weather returns Tropical Sun Hat #1, Eversummer Jacket #2. HNSW index in place. Zero application-layer embedding code.**

## What Was Built

Added `findSemanticMatches` to `ProductRepository` using a native `@Query` that calls `ai.litellm_embed` inline — the query vector is generated DB-side. One fix: Hibernate requires all entity columns in the ResultSet even for native queries, so `embedding` must appear in the SELECT even though it's excluded from the response DTO.

Both test scenarios from the spec verified.

## Files Created/Modified

- `ProductRepository.java` — findSemanticMatches native query added
- `ProductService.java` — search() method added
- `ProductController.java` — GET /api/products/search endpoint added
