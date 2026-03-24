---
id: T02
parent: S02
milestone: M001
provides:
  - "Dockerfile — eclipse-temurin:21-jre-jammy base, copies fat jar, EXPOSE 8080"
  - "compose.yaml — app service with build context, depends_on database (service_healthy), SPRING_DATASOURCE_* env vars"
  - "Full stack verified: docker compose up --build starts both services; POST /api/products → 201 + DB trigger fires; GET /api/products/1 → 200; GET /api/products/999 → 404"
key_files:
  - Dockerfile
  - compose.yaml
key_decisions:
  - "eclipse-temurin:21-jre-jammy as base image — standard LTS JRE, small footprint vs JDK"
  - "depends_on with service_healthy — app waits for DB healthcheck before starting"
patterns_established:
  - "compose.yaml app service pattern: build+depends_on+env vars for Spring datasource"
drill_down_paths:
  - .gsd/milestones/M001/slices/S02/tasks/T01-SUMMARY.md
duration: 10min
verification_result: pass
completed_at: 2026-03-24T17:20:00Z
---

# T02: Dockerfile + compose app service + integration test

**Full compose stack running: both services healthy, REST endpoints verified end-to-end.**

## What Happened

Wrote Dockerfile using `eclipse-temurin:21-jre-jammy`, copies the fat jar from `target/`. Added `app` service to `compose.yaml` with `depends_on: database: condition: service_healthy` to wait for the DB to pass its healthcheck before starting.

Integration test: `docker compose up --build` started both services in ~14s. `POST /api/products` returned `201` with clean JSON, embedding appeared in DB (768-dim vector). `GET /api/products/1` returned `200`. `GET /api/products/999` returned `404`.

## Files Created/Modified

- `Dockerfile` (new)
- `compose.yaml` — app service added
