# S03: Semantic search endpoint + HNSW index

**Goal:** Add `findSemanticMatches` to ProductRepository using a native `@Query` with the `<=>` cosine distance operator. Expose `GET /api/products/search?q=...`. HNSW index already exists in init.sql.

**Demo:** `GET /api/products/search?q=tropical+weather` returns Eversummer Jacket in top results. Verified via curl.

## Must-Haves

- `ProductRepository.findSemanticMatches(String query, int limit)` — native query using `ai.litellm_embed` inline
- `GET /api/products/search?q={text}` — default limit 5, optional `limit` param
- Response: array of `ProductResponse` ordered by cosine similarity
- "tropical weather" → Eversummer Jacket or Tropical Sun Hat in top results (verified)
- HNSW index already in init.sql — confirm it's used (no new DB changes needed)

## Tasks

- [ ] **T01: Search repository method, service, and endpoint**
  Add native @Query to ProductRepository, add search method to ProductService, add GET /api/products/search to ProductController. Verify with curl.

## Files Likely Touched

- `src/main/java/com/example/vectorcatalog/product/ProductRepository.java`
- `src/main/java/com/example/vectorcatalog/product/ProductService.java`
- `src/main/java/com/example/vectorcatalog/product/ProductController.java`
