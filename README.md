# Hacker News Web Crawler

Java solution for the StackBuilders technical exercise: scrape the first **30** entries from
[Hacker News](https://news.ycombinator.com/), filter by title word count, and persist usage data.

## Quick start for reviewers (< 10 min)

```bash
# 1. Tests (no network — HN is mocked in E2E)
mvn test

# 2. Run the app
mvn spring-boot:run
```

| Step | Action | Expect |
|------|--------|--------|
| 3 | Open [Swagger UI](http://localhost:8080/swagger-ui.html) | Interactive API docs |
| 4 | `GET /api/entries` | ~30 entries, `filter: NONE` |
| 5 | `GET /api/entries/filter?type=long_titles` | Titles with **>5** words, by comments ↓ |
| 6 | `GET /api/entries/filter?type=short_titles` | Titles with **≤5** words, by points ↓ |
| 7 | `GET /api/entries/filter?type=invalid` | `400 Bad Request` |
| 8 | `GET /api/usage` | Newest-first usage events for steps 4–6 |

Prefer Postman or Bruno? Import `postman/` or open `bruno/ebaez-web-crawler/` (same sequence). Details below.

Design decisions: [DESIGN.md](DESIGN.md).

---

## Requirements

- JDK **17+**
- Maven **3.9+**
- Network only for a live demo against HN (optional; `mvn test` does not need it)

## Stack

Java 17 · Maven · Spring Boot 3.5 (Web, Data JPA) · Jsoup · H2 · springdoc · JUnit 5 / AssertJ / Mockito · Postman + Bruno

## Word count (brief rule)

1. Split on whitespace.
2. Strip non-letter / non-digit symbols from each token.
3. Ignore empty tokens.
4. Example: `This is - a self-explained example` → **5** words.

Filters: **long** = `> 5` (order by comments); **short** = `≤ 5` (order by points). Ties break by entry `number` ascending.

## Endpoints

| Method | Path | Notes |
|--------|------|-------|
| `GET` | `/api/entries` | Top 30, no filter (`NONE`) |
| `GET` | `/api/entries/filter?type=` | `long_titles` / `short_titles` (aliases: `long`, `short`, `none`, `all`) |
| `GET` | `/api/usage` | Append-only history (timestamp + filter + extras) |

Errors: invalid `type` → **400**; HN fetch failure → **502** (usage recorded with `success=false`).

```bash
curl "http://localhost:8080/api/entries"
curl "http://localhost:8080/api/entries/filter?type=long_titles"
curl "http://localhost:8080/api/entries/filter?type=short_titles"
curl "http://localhost:8080/api/usage"
```

## Swagger / OpenAPI

- UI: http://localhost:8080/swagger-ui.html
- Spec: http://localhost:8080/v3/api-docs

## Postman

1. `mvn spring-boot:run`
2. Import `postman/ebaez-web-crawler.postman_collection.json`
3. Import `postman/local.postman_environment.json` and select **local** (`baseUrl=http://localhost:8080`)
4. Run **Happy path**, then **Errors**

## Bruno

1. `mvn spring-boot:run`
2. Open collection `bruno/ebaez-web-crawler/`
3. Select environment **local**
4. Run **Happy path**, then **Errors**

## Manual smoke & optional live call

After the app is up, the [Quick start](#quick-start-for-reviewers--10-min) table is the primary checklist.

Optional live smoke (**requires network**; not part of CI):

```bash
curl -s "http://localhost:8080/api/entries" | head -c 400
echo
curl -s "http://localhost:8080/api/usage" | head -c 400
```

## Local H2 console

With the app running: http://localhost:8080/h2-console  
JDBC URL: `jdbc:h2:file:./data/hn-crawler`

## Tests

```bash
mvn test
```

Pyramid: unit (WordCounter, FilterService) → `@WebMvcTest` / `@DataJpaTest` → `@SpringBootTest` E2E with mocked HN (no network).
