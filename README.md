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
curl http://localhost:8080/api/entries
```

Returns the first 30 scraped entries and logs a usage event with filter `NONE`.

## Status

Foundation, scraping, filters, usage persistence, and `GET /api/entries` are in place. Filter endpoints / Swagger / collections come next.

See [DESIGN.md](DESIGN.md) for architecture, word-count rules, and usage-field rationale.
