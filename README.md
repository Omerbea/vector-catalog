# vector-catalog

A semantic product catalog built with **Spring Boot**, **PostgreSQL + pgai**, and **Google Gemini** — demonstrating zero-glue-code embedding generation.

## The Core Idea

Most embedding pipelines look like this:

```
Application → embedding SDK → AI API → back to application → INSERT into DB
```

This project flips that:

```
Application → INSERT → DB trigger → AI API → embedding stored automatically
```

Every time a product row is inserted or updated, a PostgreSQL trigger fires **inside the database** and calls Google Gemini to generate a 768-dimensional vector embedding. The Spring Boot layer has **no embedding SDK** and no embedding logic. It just inserts rows.

Semantic search works the same way — the query embedding is generated inline in SQL:

```sql
ORDER BY embedding <=> ai.litellm_embed('gemini/gemini-embedding-2-preview', :query, ...)
```

This is the "zero glue code" architecture.

## Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.4 |
| Database | PostgreSQL 16 (via Timescale HA image) |
| AI Extensions | pgvector + pgai 0.11.2 |
| Embedding Model | Google Gemini `gemini-embedding-2-preview` (768-dim) |
| LiteLLM Bridge | `ai.litellm_embed` — pgai's LiteLLM proxy function |

> **Note on `ai.gemini_embed`:** The spec for this project referenced `ai.gemini_embed`, but that function does not exist in pgai 0.11.2. The equivalent is `ai.litellm_embed('gemini/gemini-embedding-2-preview', ...)` — LiteLLM supports Gemini as a backend via the `gemini/` model prefix. The architecture and zero-glue-code pattern are identical.

## Prerequisites

- Docker + Docker Compose
- A Google Gemini API key ([get one free](https://aistudio.google.com/app/apikey))

## Quickstart

```bash
# 1. Clone and configure
git clone https://github.com/your-username/vector-catalog
cd vector-catalog
cp .env.example .env
# Edit .env and set GOOGLE_API_KEY=your_key_here

# 2. Start everything
docker compose up --build

# Wait ~20-30 seconds for the DB to initialize, the app to start,
# and the seed products to be embedded (8 Gemini API calls on first boot).
```

## API

### Add a product

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Eversummer Jacket", "description": "Lightweight breathable windbreaker for humid climates", "price": 89.99}'
```

Response `201 Created`:
```json
{"id": 1, "name": "Eversummer Jacket", "description": "Lightweight breathable windbreaker for humid climates", "price": 89.99}
```

The `embedding` column is populated automatically by the DB trigger — the application never touches it.

### Semantic search

```bash
curl "http://localhost:8080/api/products/search?q=tropical+weather"
curl "http://localhost:8080/api/products/search?q=winter+protection&limit=3"
```

Response `200 OK`:
```json
[
  {"id": 5, "name": "Tropical Sun Hat", ...},
  {"id": 1, "name": "Eversummer Jacket", ...}
]
```

Searching "tropical weather" returns "Tropical Sun Hat" and "Eversummer Jacket" — not because they contain those words, but because Gemini understands the semantic relationship.

### Get product by ID

```bash
curl http://localhost:8080/api/products/1
```

## How It Works

### Database trigger

```sql
CREATE OR REPLACE FUNCTION auto_generate_embedding()
RETURNS TRIGGER AS $$
BEGIN
    NEW.embedding = ai.litellm_embed(
        'gemini/gemini-embedding-2-preview',
        NEW.name || ': ' || NEW.description,
        api_key_name => 'GOOGLE_API_KEY',
        extra_options => '{"dimensions": 768}'::jsonb
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_products_embed
    BEFORE INSERT OR UPDATE ON products
    FOR EACH ROW EXECUTE FUNCTION auto_generate_embedding();
```

The trigger runs **before** the row commits — so if the Gemini API call fails, the INSERT fails too. You can never have a product without an embedding.

### Spring Boot entity

```java
@JdbcTypeCode(SqlTypes.VECTOR)
@Array(length = 768)
@Column(name = "embedding")
private float[] embedding;
```

Hibernate 6.4+ maps `float[]` to pgvector's `VECTOR(768)` type natively.

### Search query

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

## API Key Security

`GOOGLE_API_KEY` is passed as an environment variable to the database container. pgai's secret resolution reads it automatically when `api_key_name => 'GOOGLE_API_KEY'` is used in a function call — no application-level key management required.

The key is only needed by the database container. The Spring Boot app never sees it.

## Known Limitations

- **Insert latency:** Each INSERT makes a synchronous Gemini API call from inside PostgreSQL. Expect 300ms–2s per insert depending on network conditions. This is a deliberate tradeoff — the trigger guarantees consistency (no row without an embedding).
- **`ai.gemini_embed` not available:** pgai 0.11.2 doesn't have a dedicated Gemini embed function. We use `ai.litellm_embed` with the `gemini/` prefix, which routes through LiteLLM's Gemini integration.
- **768-dim truncation:** `gemini-embedding-2-preview` defaults to 3072 dimensions. We pass `{"dimensions": 768}` via `extra_options` to truncate — this matches the spec and reduces storage/index size.

## Project Structure

```
vector-catalog/
├── compose.yaml                      # Docker Compose (DB + app)
├── Dockerfile                        # Spring Boot app image
├── docker/
│   └── init.sql                      # DB schema, trigger, HNSW index
├── .env.example                      # Environment variable template
└── src/main/java/com/example/vectorcatalog/
    ├── VectorCatalogApplication.java
    ├── DataSeeder.java               # Seeds 8 sample products on first boot
    ├── GlobalExceptionHandler.java   # Structured JSON error responses
    └── product/
        ├── Product.java              # JPA entity with vector mapping
        ├── ProductRequest.java       # Inbound DTO
        ├── ProductResponse.java      # Outbound DTO (no embedding)
        ├── ProductRepository.java    # JPA repo + native search query
        ├── ProductService.java       # Business logic
        └── ProductController.java    # REST endpoints
```
