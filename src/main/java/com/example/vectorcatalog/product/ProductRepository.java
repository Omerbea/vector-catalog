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
     * Finds products semantically similar to the given query text, with similarity scores.
     *
     * score = 1 - cosine_distance, ranging from 0 (unrelated) to 1 (identical meaning).
     * The query embedding is generated inline inside PostgreSQL via ai.litellm_embed.
     * The HNSW index on products(embedding vector_cosine_ops) makes this fast.
     *
     * @param query  natural language search query (e.g. "tropical weather")
     * @param limit  maximum number of results to return
     */
    @Query(value = """
            SELECT
                id,
                name,
                description,
                price,
                1 - (embedding <=> ai.litellm_embed(
                    'gemini/gemini-embedding-2-preview',
                    :query,
                    api_key_name => 'GOOGLE_API_KEY',
                    extra_options => '{"dimensions": 768}'::jsonb
                )) AS score
            FROM products
            ORDER BY score DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<ProductSearchProjection> findSemanticMatches(String query, int limit);
}
