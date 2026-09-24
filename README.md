# Hacker News Web Crawler

Java solution for the StackBuilders technical exercise: scrape the first 30 entries from
[Hacker News](https://news.ycombinator.com/), filter by title word count, and persist usage data.

## Stack

- Java 17
- Maven
- Spring Boot 3.5 (Web, Data JPA)
- H2 (file-based locally; in-memory for tests)
- springdoc (Swagger UI)
- Postman / Bruno collections for manual demos

## Requirements

- JDK 17+
- Maven 3.9+

## How to run

```bash
# Automated tests (no network — HN is mocked in E2E)
mvn test

# Start the application
mvn spring-boot:run
```

App listens on `http://localhost:8080`.  
H2 console (local profile defaults): `http://localhost:8080/h2-console`  
(JDBC URL: `jdbc:h2:file:./data/hn-crawler`)

## API

```bash
# Raw top entries (filter NONE)
curl "http://localhost:8080/api/entries"

# Long titles (>5 words), ordered by comments
curl "http://localhost:8080/api/entries/filter?type=long_titles"

# Short titles (<=5 words), ordered by points
curl "http://localhost:8080/api/entries/filter?type=short_titles"

# Usage history (newest first)
curl "http://localhost:8080/api/usage"
```

Each successful call appends a usage event with the applied filter. Invalid `type` → `400`. Upstream HN failure → `502`.

## Swagger / OpenAPI

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## Postman

1. Start the app (`mvn spring-boot:run`).
2. In Postman: **Import** → `postman/ebaez-web-crawler.postman_collection.json`.
3. Import the environment `postman/local.postman_environment.json` and select **local** (`baseUrl=http://localhost:8080`).
4. Run **Happy path** (entries → long → short → usage), then **Errors** (invalid type → 400).

## Bruno

Same request sequence as Postman, git-friendly `.bru` files:

1. Start the app (`mvn spring-boot:run`).
2. In Bruno: **Open Collection** → `bruno/ebaez-web-crawler/`.
3. Select environment **local** (`baseUrl=http://localhost:8080`).
4. Run **Happy path**, then **Errors**.

## Manual smoke checklist

Use this after `mvn spring-boot:run` to demo the app locally (complements automated tests; does not replace them).

### 1. Swagger UI

1. Open http://localhost:8080/swagger-ui.html
2. Try **GET /api/entries** → expect ~30 items, `filter: NONE`
3. Try **GET /api/entries/filter** with `type=long_titles` → titles with >5 words, ordered by comments
4. Try **GET /api/entries/filter** with `type=short_titles` → titles with ≤5 words, ordered by points
5. Try **GET /api/entries/filter** with `type=invalid` → `400`
6. Try **GET /api/usage** → newest-first events for the filters you just ran

### 2. Postman or Bruno

Run the collection **Happy path**, then **Errors**, and confirm `/api/usage` lists the successful calls.

### 3. Optional live smoke (requires network)

`mvn test` never hits the real site. A live call is optional and can be flaky if HN is slow or unavailable:

```bash
# Requires network — scrapes https://news.ycombinator.com/
curl -s "http://localhost:8080/api/entries" | head -c 400
echo
curl -s "http://localhost:8080/api/usage" | head -c 400
```

Expect HTTP 200 and real titles when HN is reachable; otherwise the API returns `502` and records `success=false` in usage.

## Status

APIs, Swagger, Postman/Bruno collections, and the automated test pyramid (unit → WebMvc/DataJpa → mocked E2E) are in place.

See [DESIGN.md](DESIGN.md) for architecture, word-count rules, and usage-field rationale.
