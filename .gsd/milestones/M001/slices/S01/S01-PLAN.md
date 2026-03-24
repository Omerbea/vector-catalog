# S01: Infrastructure + DB Embedding Proof

**Goal:** Prove that `ai.gemini_embed` exists and fires correctly in the Timescale container before writing any application code. Deliver the `compose.yaml`, `init.sql`, and confirmed function signature.

**Demo:** The Timescale container is running locally with `pgvector` and `pgai` extensions active; a test INSERT fires the trigger; the `embedding` column contains a real 768-dim vector. Verified via psql.

## Must-Haves

- `ai.gemini_embed('gemini-embedding-2-preview', text)` confirmed to exist in `timescale/timescaledb-ha:pg16-latest`
- Correct `GEMINI_API_KEY` passthrough mechanism confirmed (env var name or session parameter)
- `init.sql` creates extensions, `products` table, trigger function, trigger
- `compose.yaml` database service definition working
- A real INSERT produces a non-null `VECTOR(768)` in the `embedding` column (verified via `SELECT id, embedding IS NOT NULL`)

## Tasks

- [ ] **T01: Container verification — confirm `ai.gemini_embed` exists**
  Pull the Timescale image, start a container, install extensions, probe the `ai` schema for Gemini-related functions. No API key needed — just confirm the function exists in the catalog.

- [ ] **T02: Write `init.sql` and `compose.yaml` database service**
  Using the confirmed function signature from T01, write the full init SQL (extensions, table, trigger function, trigger) and the `compose.yaml` database service stanza.

- [ ] **T03: Live trigger test with real Gemini API key**
  Start the compose database service with a real `GEMINI_API_KEY`, run the init SQL, INSERT a test product, verify the `embedding` column is non-null and 768-dimensional.

## Files Likely Touched

- `compose.yaml`
- `src/main/resources/db/init.sql` (or `docker-entrypoint-initdb.d/init.sql`)
- `.env.example`
