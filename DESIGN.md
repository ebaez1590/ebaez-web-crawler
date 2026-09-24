# Design notes

Design decisions for the StackBuilders Hacker News crawler exercise.

This document is the **short** rationale a reviewer can read in a few minutes.
It summarizes conclusions from an earlier stack comparison (Jsoup vs browser automation, H2 vs Postgres/Redis, etc.) and from a live DOM POC against `news.ycombinator.com`.

## Architecture

```text
API (controllers + DTOs + exception handler)
        ↓
Application services (CrawlService, FilterService, UsageService)
        ↓
Domain (HnEntry, FilterType, WordCounter)     ← pure Java, no I/O
        ↓
Infrastructure
  · HackerNewsClient (port) → JsoupHackerNewsClient + HnHtmlParser
  · UsageEventRepository (JPA) → H2
```

- Controllers stay thin: validate `type`, call services, return DTOs.
- `CrawlService` is the single path: **fetch → filter → record usage** (success or failure).
- Scraping is behind a port so tests can mock HN without network.

## Stack choices (and what we skipped)

| Choice | Why | Main alternative rejected |
|--------|-----|---------------------------|
| **Java 17 + Maven** | Familiar to reviewers; LTS; simple build | Kotlin / Gradle |
| **Spring Boot 3** | Layering, DI, REST, JPA testing without ceremony | Plain Java + embedded server |
| **Jsoup** | HN homepage is **server-rendered HTML**; CSS selectors + fixture tests | Selenium / Playwright / HtmlUnit (overkill; slower; harder CI) |
| **H2 + Spring Data JPA** | Usage log with zero ops for the reviewer | Postgres / Redis (usage is append-only, not a cache) |
| **springdoc** | Interactive demo at `/swagger-ui.html` | README-only curl samples |
| **Postman + Bruno** | Same demo sequence; cover both tool preferences | A single collection format |
| **JUnit 5 + AssertJ + Mockito** | Deterministic pyramid without live HN | Live-only smoke in CI |

**Intentionally out of scope:** pagination (`?p=2`), auth, frontend, Docker Compose, Redis, browser E2E, live HN in CI.

## Word count rule

Aligned with the brief:

1. Split on whitespace.
2. Strip non-letter / non-digit symbols from each token (`\p{L}` / `\p{N}`).
3. Ignore empty tokens after stripping.
4. Brief example: `This is - a self-explained example` → **5** words (`-` discarded; `self-explained` → one token).

Edge cases covered in unit tests: null/blank, symbols-only, punctuation (`Hello, world!`), numbers as tokens, unicode/accents, apostrophes.

## Filters

| Filter | Rule | Order | Tie-break |
|--------|------|-------|-----------|
| Long titles | word count **> 5** | comments ↓ | `number` ↑ |
| Short titles | word count **≤ 5** | points ↓ | `number` ↑ |
| None | no filter | original scrape order | — |

Exactly **5** words belongs to the **short** filter only.

## Scraping (DOM)

Validated against the live HN homepage structure (table `#hnmain`, 30 stories per page):

| Field | Selector / strategy |
|-------|---------------------|
| `number` | `tr.athing.submission span.rank` (strip trailing `.`) |
| `title` | `span.titleline > a` (first link only; ignore `sitebit`) |
| `points` | sibling row `span.score` → first integer; **missing (jobs) → 0** |
| `comments` | sibling `a[href^=item?id=]` → parse `N comments` / `1 comment`; **`discuss` or missing → 0** |

Each story is two table rows (`tr.athing` + subtext) plus a spacer. JavaScript is not required to read the listing.

Live HTTP: configurable URL, User-Agent, timeout (`hn.crawler.*`). Failures become typed `HackerNewsFetchException` (`TIMEOUT`, `HTTP_STATUS`, `IO`) → API **502**.

## Usage persistence

Brief requires at least **timestamp** + **filter id**. We store extras for observability:

| Field | Role |
|-------|------|
| `requestedAt` | When the interaction happened |
| `filterApplied` | `NONE` / long / short |
| `resultCount` | Returned size (`null` on hard failure before a list exists) |
| `durationMs` | Fetch + filter timing (lightweight performance insight) |
| `sourceUrl` | Scraped URL |
| `success` / `errorMessage` | Outcome detail |

Append-only: each request inserts a row; past events are never updated. Helpers: `recordSuccess` / `recordFailure`.

## Test pyramid

```text
        / E2E (@SpringBootTest, HN mocked) \
       /  Integration (@WebMvcTest, @DataJpaTest) \
      /   Unit + edges (WordCounter, FilterService, parser fixtures) \
     ---------------------------------------------------------------
                    Manual: Swagger + Postman/Bruno
                 (optional live curl — requires network, not CI)
```

Automated tests never depend on `news.ycombinator.com`. Parser tests use HTML fixtures (including discuss + job posts).

## API surface

| Endpoint | Behavior |
|----------|----------|
| `GET /api/entries` | Top 30, filter `NONE` |
| `GET /api/entries/filter?type=` | `long_titles` / `short_titles` (+ aliases) |
| `GET /api/usage` | Newest-first history |
| Errors | `400` invalid type; `502` upstream fetch failure |

Demo docs: Swagger UI, Postman (`postman/`), Bruno (`bruno/`). See [README.md](README.md).
