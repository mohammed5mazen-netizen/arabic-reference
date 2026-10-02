# Infrastructure

S0 runs PostgreSQL 16 and Redis 7 with the root `docker-compose.yml`.

The application processes stay on the host during development. Application images, a search cluster, and a CDN are not part of this stage.

PostgreSQL is the system of record and is created with UTF-8. Redis is reserved for later caching, rate limiting, and expensive linguistic lookups. S0 does not introduce a cache layer.

A future search engine can be added beside these services. The backend depends on `LinguisticSearchPort`, not on a particular engine.
