## After this slice, the user can:
- Run `docker compose up --build` — both services start
- `curl -X POST http://localhost:8080/api/products -H "Content-Type: application/json" -d '{"name":"Test Jacket","description":"A warm jacket","price":99.99}'`
  Expected: `{"id":1,"name":"Test Jacket","description":"A warm jacket","price":99.99}` (HTTP 201)
- `curl http://localhost:8080/api/products/1`
  Expected: same JSON (HTTP 200)
- `docker exec vector-catalog-database-1 psql -U postgres -d vectorcatalog -c "SELECT id, embedding IS NOT NULL, vector_dims(embedding) FROM products;"`
  Expected: `t | 768` — DB trigger auto-populated the embedding

## Test

1. `docker compose up --build -d`
2. `curl -s -X POST http://localhost:8080/api/products -H "Content-Type: application/json" -d '{"name":"Test","description":"Test product","price":10.00}'`
   → Should return `{"id":...,"name":"Test",...}` with HTTP 201
3. `curl -s http://localhost:8080/api/products/1` → HTTP 200
4. `curl -s http://localhost:8080/api/products/9999` → HTTP 404
