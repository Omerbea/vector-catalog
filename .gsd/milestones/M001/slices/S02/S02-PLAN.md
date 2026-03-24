# S02: Product CRUD API

**Goal:** Build the Spring Boot application layer — `pom.xml`, entity, repository, service, controller, config — and wire it to the running database. Add the app service to `compose.yaml`. Verify `POST /api/products` and `GET /api/products/{id}` work end-to-end.

**Demo:** `docker compose up` starts both services; `curl -X POST localhost:8080/api/products` saves a product; DB trigger fires; `GET /api/products/1` returns it. Verified via curl.

## Must-Haves

- `pom.xml` with correct dependency set (Spring Boot 3.4+, Spring Data JPA, Hibernate Vector 6.4+, PostgreSQL JDBC, Spring Web)
- `Product.java` entity with `@JdbcTypeCode(SqlTypes.VECTOR)` + `@Array(length = 768)` on `embedding`
- `ProductRepository` extends `JpaRepository<Product, Long>`
- `POST /api/products` → 201 Created with `{id, name, description, price}`
- `GET /api/products/{id}` → 200 OK or 404
- `Dockerfile` for the app
- App service added to `compose.yaml` with `depends_on: database`
- `docker compose up` starts both services; curl confirms endpoints work

## Tasks

- [ ] **T01: Project scaffold — pom.xml, entity, repository, service, controller**
  Full Spring Boot project structure with all files. No stubs.

- [ ] **T02: Dockerfile + compose app service + integration test**
  Dockerfile, add app to compose.yaml, start the full stack, verify with curl.

## Files Likely Touched

- `pom.xml`
- `src/main/java/com/example/vectorcatalog/...`
- `src/main/resources/application.yml`
- `Dockerfile`
- `compose.yaml`
