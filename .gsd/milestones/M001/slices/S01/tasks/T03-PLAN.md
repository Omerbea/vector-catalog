# T03: Live trigger test with real Gemini API key

**Slice:** S01
**Milestone:** M001

## Goal

Start the compose database service with a real `GOOGLE_API_KEY`, run the init SQL, INSERT a test product, and verify the `embedding` column is non-null and 768-dimensional. This is the proof that the entire architecture works.

## Must-Haves

### Truths
- `docker compose up database` starts without error
- Init SQL runs on first boot (extensions, table, trigger, index created)
- `INSERT INTO products (name, description, price) VALUES (...)` succeeds
- After insert, `SELECT id, embedding IS NOT NULL, vector_dims(embedding) FROM products` shows `true` and `768`

### Artifacts
- No new files — this is an integration test

## Steps
1. Start the database service: `docker compose up -d database`
2. Wait for health check to pass
3. Verify extensions installed: `SELECT extname FROM pg_extension WHERE extname IN ('vector','ai','plpython3u');`
4. Insert a test product: `INSERT INTO products (name, description, price) VALUES ('Eversummer Jacket', 'Lightweight breathable windbreaker for humid climates', 89.99);`
5. Verify embedding: `SELECT id, embedding IS NOT NULL AS has_embedding, vector_dims(embedding) AS dims FROM products;`
6. Shut down: `docker compose down`

## Context
- GOOGLE_API_KEY is in .env — docker compose reads it automatically
- The trigger calls the live Gemini API synchronously — insert may take 1-3 seconds
- If the insert fails with an API error, check the key is valid and the LiteLLM Gemini path is correct
