package com.example.vectorcatalog.product;

import jakarta.persistence.*;
import java.math.BigDecimal;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Product entity.
 *
 * The {@code embedding} column is populated automatically by the Postgres trigger
 * {@code trg_products_embed}, which calls {@code ai.litellm_embed} (Gemini via LiteLLM)
 * before each INSERT or UPDATE. Spring never sets this field — it appears after the
 * DB round-trip completes.
 *
 * Hibernate 6.4+ maps {@code float[]} to pgvector's VECTOR type via
 * {@code @JdbcTypeCode(SqlTypes.VECTOR)} + {@code @Array(length = 768)}.
 */
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * 768-dimensional Gemini embedding, generated DB-side by the pgai trigger.
     * Excluded from API request/response DTOs — the DB owns it.
     */
    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 768)
    @Column(name = "embedding")
    private float[] embedding;

    // --- Constructors ---

    public Product() {}

    public Product(String name, String description, BigDecimal price) {
        this.name = name;
        this.description = description;
        this.price = price;
    }

    // --- Getters & Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public float[] getEmbedding() { return embedding; }
    public void setEmbedding(float[] embedding) { this.embedding = embedding; }
}
