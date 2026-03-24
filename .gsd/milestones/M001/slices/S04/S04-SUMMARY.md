---
id: S04
milestone: M001
provides:
  - "GlobalExceptionHandler — @RestControllerAdvice with structured JSON errors for validation, constraint violations, malformed JSON, and generic 500s"
  - "DataSeeder — ApplicationRunner seeding 8 sample products on first boot if table is empty"
  - "README.md — full quickstart, architecture explanation, zero-glue-code description, known limitations, project structure"
  - ".gitignore — excludes .env, target/, GSD runtime dirs, IDE files"
  - "Full milestone DoD verified: all endpoints working, seed data + auto-embedding confirmed, error handling clean"
requires:
  - slice: S03
    provides: All working endpoints
affects: []
key_files:
  - src/main/java/com/example/vectorcatalog/GlobalExceptionHandler.java
  - src/main/java/com/example/vectorcatalog/DataSeeder.java
  - README.md
  - .gitignore
key_decisions: []
patterns_established:
  - "DataSeeder uses ApplicationRunner + repository.count() to avoid re-seeding on restart"
drill_down_paths: []
verification_result: pass
completed_at: 2026-03-24T17:50:00Z
---

# S04: Polish — README, seed data, error handling

**Project ready for GitHub: 8 products auto-seeded on first boot, structured JSON errors, comprehensive README explaining the zero-glue-code architecture.**

## What Was Built

- `GlobalExceptionHandler`: `@RestControllerAdvice` returning structured `{"error": "..."}` or `{"errors": {"field": "..."}}` JSON for all error paths.
- `DataSeeder`: `ApplicationRunner` that inserts 8 sample products on first boot if the table is empty. Each product triggers a live Gemini API call via the DB trigger.
- `README.md`: Full quickstart, architecture explanation, zero-glue-code description, API docs with curl examples, known limitations (insert latency, `ai.gemini_embed` note, 768-dim truncation).
- `.gitignore`: Excludes `.env`, `target/`, GSD runtime directories.

Final verification: all endpoints tested, seed data confirmed, error handling clean.

## Files Created/Modified

- `src/main/java/com/example/vectorcatalog/GlobalExceptionHandler.java` (new)
- `src/main/java/com/example/vectorcatalog/DataSeeder.java` (new)
- `README.md` (new)
- `.gitignore` (new)
