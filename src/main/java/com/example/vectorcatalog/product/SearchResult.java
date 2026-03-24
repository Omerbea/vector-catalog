package com.example.vectorcatalog.product;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Search result with similarity score.
 *
 * score: cosine similarity in [0, 1] — higher means more similar.
 *   1.0 = identical meaning
 *   0.0 = completely unrelated
 *
 * Derived from pgvector cosine distance: score = 1 - (embedding <=> query_vector)
 */
public record SearchResult(
        Long id,
        String name,
        String description,
        BigDecimal price,
        double score
) {
    public static SearchResult from(ProductSearchProjection p) {
        // Round to 4 decimal places — enough precision to compare results meaningfully
        double score = BigDecimal.valueOf(p.getScore())
                .setScale(4, RoundingMode.HALF_UP)
                .doubleValue();
        return new SearchResult(p.getId(), p.getName(), p.getDescription(), p.getPrice(), score);
    }
}
