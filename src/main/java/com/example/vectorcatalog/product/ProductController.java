package com.example.vectorcatalog.product;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    /**
     * Creates a product. The DB trigger handles embedding generation automatically.
     *
     * POST /api/products
     * Body: {"name": "...", "description": "...", "price": 9.99}
     * Response: 201 Created with {id, name, description, price}
     */
    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        Product saved = service.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductResponse.from(saved));
    }

    /**
     * Retrieves a product by ID.
     *
     * GET /api/products/{id}
     * Response: 200 OK with {id, name, description, price} or 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ProductResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Semantic search — finds products by meaning, not keyword.
     * Query embedding is generated DB-side; no AI SDK in the application layer.
     *
     * GET /api/products/search?q=tropical+weather&limit=5
     * Response: 200 OK with array of {id, name, description, price} ordered by similarity
     */
    @GetMapping("/search")
    public ResponseEntity<List<SearchResult>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(service.search(q, limit));
    }
}
