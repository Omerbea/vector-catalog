package com.example.vectorcatalog.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for products.
 *
 * The semantic search query generates the query embedding DB-side via ai.litellm_embed,
 * maintaining the zero-glue-code pattern for both writes and reads.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Finds products semantically similar to the given query text.
     *
     * The query embedding is generated inline inside PostgreSQL via ai.litellm_embed,
     * so no embedding SDK is needed in the application layer.
     *
     * Ordering uses pgvector's cosine distance operator (<=>) — lower is more similar.
     * The HNSW index on products(embedding vector_cosine_ops) makes this fast.
     *
     * @param query  natural language search query (e.g. "tropical weather")
     * @param limit  maximum number of results to return
     */
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
}
