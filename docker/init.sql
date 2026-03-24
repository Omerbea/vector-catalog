-- vector-catalog init script
-- Runs automatically on first container boot via docker-entrypoint-initdb.d

-- Extensions
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS plpython3u;
CREATE EXTENSION IF NOT EXISTS ai CASCADE;

-- Products table
CREATE TABLE IF NOT EXISTS products (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT         NOT NULL,
    price       DECIMAL(10, 2),
    embedding   VECTOR(768)  -- Gemini embedding-2-preview dimension
);

-- Embedding trigger function
-- Uses ai.litellm_embed with LiteLLM's Gemini provider (gemini/ prefix).
-- The GOOGLE_API_KEY env var is read automatically by pgai's secret resolution.
-- api_key_name='GOOGLE_API_KEY' tells pgai to look up that name in env vars / GUC.
CREATE OR REPLACE FUNCTION auto_generate_embedding()
RETURNS TRIGGER AS $$
BEGIN
    -- gemini-embedding-2-preview defaults to 3072 dims; we request 768 via LiteLLM extra_options.
    -- api_key_name tells pgai to resolve GOOGLE_API_KEY from the container env var automatically.
    NEW.embedding = ai.litellm_embed(
        'gemini/gemini-embedding-2-preview',
        NEW.name || ': ' || NEW.description,
        api_key_name => 'GOOGLE_API_KEY',
        extra_options => '{"dimensions": 768}'::jsonb
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Attach trigger: fires before every INSERT or UPDATE
CREATE TRIGGER trg_products_embed
    BEFORE INSERT OR UPDATE ON products
    FOR EACH ROW
    EXECUTE FUNCTION auto_generate_embedding();

-- HNSW index for fast cosine similarity search
-- Created after table so it doesn't interfere with schema setup.
-- Populated as products are inserted.
CREATE INDEX IF NOT EXISTS products_embedding_hnsw_idx
    ON products USING hnsw (embedding vector_cosine_ops);
