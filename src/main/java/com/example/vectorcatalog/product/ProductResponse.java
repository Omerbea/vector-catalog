package com.example.vectorcatalog.product;

import java.math.BigDecimal;

/**
 * Outbound DTO for product responses.
 * Omits the {@code embedding} field — it's internal to the DB and large (768 floats).
 */
public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice()
        );
    }
}
