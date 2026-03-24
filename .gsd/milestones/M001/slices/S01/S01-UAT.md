## After this slice, the user can:
- Run `docker compose up database` and get a running Timescale DB with pgai + pgvector installed
- Connect via psql and run `INSERT INTO products (name, description, price) VALUES (...)` — the trigger fires automatically, calls Gemini via LiteLLM, and the `embedding` column is populated with a real 768-dim vector
- Run a cosine similarity query in psql and see semantically relevant results (verified: "tropical weather" → Tropical Sun Hat, Eversummer Jacket)

## Test

1. `docker compose up -d database && docker compose ps` — both healthy
2. `docker exec vector-catalog-database-1 psql -U postgres -d vectorcatalog -c "INSERT INTO products (name, description, price) VALUES ('Test Coat', 'A warm winter coat', 99.99);"`
3. `docker exec vector-catalog-database-1 psql -U postgres -d vectorcatalog -c "SELECT id, embedding IS NOT NULL AS has_embedding, vector_dims(embedding) AS dims FROM products WHERE name = 'Test Coat';"`
   Expected: `has_embedding = t`, `dims = 768`
