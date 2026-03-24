# T01: Project scaffold — pom.xml, entity, repository, service, controller

**Slice:** S02
**Milestone:** M001

## Goal

Build the complete Spring Boot project: pom.xml, entity with vector column mapping, repository, service, controller, and application.yml. No stubs — all files real.

## Must-Haves

### Truths
- `./mvnw spring-boot:run` compiles and starts without error (connecting to the running DB from compose)
- `POST /api/products` with `{"name":"X","description":"Y","price":9.99}` returns 201 with `{id,name,description,price}`
- The `embedding` column is NOT included in the request/response JSON — it's the DB's responsibility
- `GET /api/products/{id}` returns 200 with the product or 404

### Artifacts
- `pom.xml` — Spring Boot 3.4+, spring-boot-starter-web, spring-boot-starter-data-jpa, postgresql driver, hibernate-vector 6.6+
- `src/main/java/com/example/vectorcatalog/VectorCatalogApplication.java` — main class
- `src/main/java/com/example/vectorcatalog/product/Product.java` — JPA entity, `embedding` field with `@JdbcTypeCode(SqlTypes.VECTOR) @Array(length = 768) float[]`
- `src/main/java/com/example/vectorcatalog/product/ProductRequest.java` — DTO (name, description, price)
- `src/main/java/com/example/vectorcatalog/product/ProductResponse.java` — DTO (id, name, description, price — no embedding)
- `src/main/java/com/example/vectorcatalog/product/ProductRepository.java` — JpaRepository<Product, Long>
- `src/main/java/com/example/vectorcatalog/product/ProductService.java` — save(), findById()
- `src/main/java/com/example/vectorcatalog/product/ProductController.java` — POST /api/products, GET /api/products/{id}
- `src/main/resources/application.yml` — datasource, jpa config

### Key Links
- `ProductController` → `ProductService` via `@Autowired` / constructor injection
- `ProductService` → `ProductRepository` via constructor injection
- `Product.embedding` field → `VECTOR(768)` column via `@JdbcTypeCode(SqlTypes.VECTOR)`

## Steps
1. Create directory structure under `src/main/java/com/example/vectorcatalog/`
2. Write `pom.xml`
3. Write `VectorCatalogApplication.java`
4. Write `Product.java` entity — embedding field as `float[]` with Hibernate vector annotations
5. Write `ProductRequest.java` and `ProductResponse.java` DTOs
6. Write `ProductRepository.java`
7. Write `ProductService.java`
8. Write `ProductController.java`
9. Write `application.yml`
10. Build: `./mvnw compile` — fix any errors

## Context
- Hibernate 6.6 ships with Spring Boot 3.4 — hibernate-vector module is separate artifact
- `@JdbcTypeCode(SqlTypes.VECTOR)` + `@Array(length = 768)` on `float[]` field — this is the correct Hibernate 6.4+ pattern
- JPA `ddl-auto: validate` — Hibernate validates schema against DB, does not create/alter tables (init.sql owns the schema)
- The `embedding` column is populated by the DB trigger — Spring just does a plain `save()`, embedding appears automatically
- DB URL for local dev (when running outside compose): `jdbc:postgresql://localhost:5432/vectorcatalog`
