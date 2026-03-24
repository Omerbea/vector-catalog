package com.example.vectorcatalog.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Inbound DTO for creating or updating a product.
 * Intentionally excludes {@code embedding} — that is the database's responsibility.
 */
public record ProductRequest(
        @NotBlank(message = "name is required")
        String name,

        @NotBlank(message = "description is required")
        String description,

        @Positive(message = "price must be positive")
        BigDecimal price
) {}
