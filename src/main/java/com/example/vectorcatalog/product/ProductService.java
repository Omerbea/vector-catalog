package com.example.vectorcatalog.product;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    /**
     * Persists a new product. The DB trigger fires automatically on INSERT,
     * populating the embedding column via Gemini/LiteLLM. No embedding logic here.
     */
    @Transactional
    public Product save(ProductRequest request) {
        Product product = new Product(
                request.name(),
                request.description(),
                request.price()
        );
        return repository.save(product);
    }

    public Optional<Product> findById(Long id) {
        return repository.findById(id);
    }

    /**
     * Returns products ordered by semantic similarity to the query.
     * Query embedding is generated DB-side — no embedding SDK in the application.
     */
    public List<Product> search(String query, int limit) {
        return repository.findSemanticMatches(query, limit);
    }
}
