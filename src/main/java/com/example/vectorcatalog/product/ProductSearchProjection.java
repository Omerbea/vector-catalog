package com.example.vectorcatalog.product;

import java.math.BigDecimal;

/**
 * Native query projection for semantic search results.
 * Maps the SQL columns id, name, description, price, score from findSemanticMatches.
 */
public interface ProductSearchProjection {
    Long getId();
    String getName();
    String getDescription();
    BigDecimal getPrice();
    double getScore();
}
