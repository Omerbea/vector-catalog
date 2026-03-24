# vector-catalog

> **Semantic product search powered by Google Gemini — where the database generates the embeddings, not your application.**

```bash
curl "http://localhost:8080/api/products/search?q=tropical+weather"
# → returns "Tropical Sun Hat", "Eversummer Jacket"
# No keyword match. Pure semantic understanding.
```

---

## The Problem with Typical Embedding Pipelines

Most embedding workflows look like this:

```
INSERT product
  → application calls Gemini API
    → stores vector in DB
      → hope they stay in sync
```

Four moving parts. Two failure surfaces. One more background worker to maintain.

## The Zero-Glue-Code Architecture

```
INSERT product → PostgreSQL trigger → Gemini API → vector stored atomically
```

This project demonstrates a single architectural idea: **let the database own the embeddings**.

A `BEFORE INSERT OR UPDATE` trigger calls Google Gemini from inside PostgreSQL via [pgai](https://github.com/timescale/pgai). The vector is stored in the same transaction as the row. If the API call fails, the insert fails — you can never have a product without an embedding.

The Spring Boot application has **zero embedding code**. It issues a plain `save()`. The 768-dimensional vector appears automatically.

Semantic search follows the same principle — the query embedding is generated inline in SQL:

```sql
ORDER BY embedding <=> ai.litellm_embed('gemini/gemini-embedding-2-preview', :query, ...)
```

No embedding SDK in the application. Not even for search.

---

## Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.4 |
| Database | PostgreSQL 16 via `timescale/timescaledb-ha:pg16` |
| Vector storage | [pgvector](https://github.com/pgvector/pgvector) — `VECTOR(768)` column + HNSW index |
| AI in the DB | [pgai](https://github.com/timescale/pgai) 0.11.2 — `ai.litellm_embed` function |
| Embedding model | Google Gemini `gemini-embedding-2-preview` (768-dim) |
| ORM | Hibernate 6.6 with native vector type support |

---

## Prerequisites

- Docker + Docker Compose
- A Google Gemini API key — [get one free](https://aistudio.google.com/app/apikey)

---

## Quickstart

```bash
# 1. Clone
git clone https://github.com/your-username/vector-catalog
cd vector-catalog

# 2. Configure
cp .env.example .env
# Open .env and set: GOOGLE_API_KEY=your_key_here

# 3. Run
docker compose up --build
```

On first boot, 8 sample products are seeded automatically. Each triggers a live Gemini API call from inside PostgreSQL. Expect ~20–40 seconds for the full startup + seeding cycle.

---

## API

### `POST /api/products` — Add a product

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Eversummer Jacket",
    "description": "Lightweight breathable windbreaker for humid climates",
    "price": 89.99
  }'
```

```json
{
  "id": 1,
  "name": "Eversummer Jacket",
  "description": "Lightweight breathable windbreaker for humid climates",
  "price": 89.99
}
```

The `embedding` column is populated by the DB trigger. The application never touches it.

---

### `GET /api/products/search?q=...` — Semantic search

```bash
curl "http://localhost:8080/api/products/search?q=tropical+weather"
curl "http://localhost:8080/api/products/search?q=winter+protection&limit=3"
curl "http://localhost:8080/api/products/search?q=something+for+the+rain"
```

Results are ranked by cosine similarity. "tropical weather" returns "Tropical Sun Hat" and "Eversummer Jacket" because Gemini understands semantic proximity — not because any keyword matched.

```json
[
  { "id": 5, "name": "Tropical Sun Hat", "description": "Wide brim hat with UV protection...", "price": 29.99 },
  { "id": 1, "name": "Eversummer Jacket", "description": "Lightweight breathable windbreaker...", "price": 89.99 }
]
```

---

### `GET /api/products/{id}` — Get by ID

```bash
curl http://localhost:8080/api/products/1   # 200 OK
curl http://localhost:8080/api/products/999 # 404 Not Found
```

---

## How It Works

### The trigger

```sql
CREATE OR REPLACE FUNCTION auto_generate_embedding()
RETURNS TRIGGER AS $$
BEGIN
    -- Called from inside PostgreSQL. Zero application involvement.
    NEW.embedding = ai.litellm_embed(
        'gemini/gemini-embedding-2-preview',  -- Gemini via LiteLLM proxy
        NEW.name || ': ' || NEW.description,  -- text to embed
        api_key_name => 'GOOGLE_API_KEY',     -- reads env var automatically
        extra_options => '{"dimensions": 768}'::jsonb
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_products_embed
    BEFORE INSERT OR UPDATE ON products
    FOR EACH ROW EXECUTE FUNCTION auto_generate_embedding();
```

### The entity

```java
@Entity
@Table(name = "products")
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;

    // DB trigger owns this. Spring never writes to it.
    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 768)
    private float[] embedding;
}
```

Hibernate 6.6 maps `float[]` to `VECTOR(768)` natively via the `hibernate-vector` module. No `pgvector-java` library needed.

### The search query

```java
@Query(value = """
    SELECT id, name, description, price, embedding
    FROM products
    ORDER BY embedding <=> ai.litellm_embed(
        'gemini/gemini-embedding-2-preview',
        :query,
        api_key_name => 'GOOGLE_API_KEY',
        extra_options => '{"dimensions": 768}'::jsonb
    )
    LIMIT :limit
    """, nativeQuery = true)
List<Product> findSemanticMatches(String query, int limit);
```

The query vector is generated inside PostgreSQL. The HNSW index on `products(embedding vector_cosine_ops)` makes it fast.

---

## API Key Flow

`GOOGLE_API_KEY` lives only in the database container's environment. pgai's secret resolution reads it automatically when `api_key_name => 'GOOGLE_API_KEY'` appears in a function call — no session parameters, no `PGOPTIONS`, no application-level key management.

**The Spring Boot app never sees the API key.**

---

## Project Structure

```
vector-catalog/
├── compose.yaml                      # DB + app services
├── Dockerfile                        # eclipse-temurin:21-jre base
├── docker/
│   └── init.sql                      # Extensions, table, trigger, HNSW index
├── .env.example                      # Copy to .env and set GOOGLE_API_KEY
└── src/main/java/com/example/vectorcatalog/
    ├── VectorCatalogApplication.java
    ├── DataSeeder.java               # Seeds 8 products on first boot
    ├── GlobalExceptionHandler.java   # Structured JSON error responses
    └── product/
        ├── Product.java              # JPA entity with @JdbcTypeCode(VECTOR)
        ├── ProductRequest.java       # Inbound DTO (no embedding field)
        ├── ProductResponse.java      # Outbound DTO (no embedding field)
        ├── ProductRepository.java    # JPA repo + native vector search query
        ├── ProductService.java
        └── ProductController.java    # POST /api/products, GET /search, GET /{id}
```

---

## Trade-offs and Limitations

**Insert latency** — each `INSERT` makes a synchronous Gemini API call from inside the DB transaction. Expect 300ms–2s per insert. This is the cost of the consistency guarantee: no row without an embedding, no eventual-consistency complexity.

**`ai.gemini_embed` doesn't exist (yet)** — pgai 0.11.2 has no dedicated Gemini embed function. This project uses `ai.litellm_embed` with the `gemini/` model prefix, which routes through LiteLLM's Gemini integration. The architecture is identical to what a native `ai.gemini_embed` would look like.

**768-dim truncation** — `gemini-embedding-2-preview` outputs 3072 dimensions by default. We request 768 via `extra_options => '{"dimensions": 768}'` to match the column type and reduce index size.

---

## Further Reading

- [pgai — AI functions for PostgreSQL](https://github.com/timescale/pgai)
- [pgvector — vector similarity search for PostgreSQL](https://github.com/pgvector/pgvector)
- [Hibernate Vector module](https://docs.jboss.org/hibernate/orm/6.6/userguide/html_single/Hibernate_User_Guide.html#basic-vector)
- [Google Gemini Embeddings API](https://ai.google.dev/gemini-api/docs/embeddings)
