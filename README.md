# Hacker News Web Crawler

Java solution for the StackBuilders technical exercise: scrape the first 30 entries from
[Hacker News](https://news.ycombinator.com/), filter by title word count, and persist usage data.

## Stack

- Java 17
- Maven
- Spring Boot 3.5 (Web, Data JPA)
- H2 (file-based locally; in-memory for tests)

## Requirements

- JDK 17+
- Maven 3.9+

## How to run

```bash
# Run tests
mvn test

# Start the application
mvn spring-boot:run
```

App listens on `http://localhost:8080`.  
H2 console (local profile defaults): `http://localhost:8080/h2-console`  
(JDBC URL: `jdbc:h2:file:./data/hn-crawler`)

## API (so far)

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

## Swagger / OpenAPI

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

Each successful call appends a usage event with the applied filter.

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

## Status

Core APIs, Swagger UI, Postman, and Bruno collections are available.

See [DESIGN.md](DESIGN.md) for architecture, word-count rules, and usage-field rationale.
