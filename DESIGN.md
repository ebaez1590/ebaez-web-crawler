# Design notes

Brief design decisions for the StackBuilders Hacker News crawler exercise.

## Architecture

```text
API (controllers)  →  Application services  →  Domain
                                      ↘
                                       Infrastructure (Jsoup scraper, H2 usage log)
```

- **Domain** (`HnEntry`, `FilterType`, `WordCounter`) has no Spring/Jsoup dependencies.
- **FilterService** applies title-length filters and ordering rules.
- **HackerNewsClient** is a port; `JsoupHackerNewsClient` is the adapter.
- **UsageService** appends interaction events to H2 via JPA.

## Word count rule

1. Split on whitespace.
2. Strip non-letter/non-digit symbols from each token.
3. Ignore empty tokens after stripping.
4. Example from the brief: `This is - a self-explained example` → **5** words.

## Filters

| Filter | Rule | Order |
|--------|------|-------|
| Long titles | word count **> 5** | comments descending |
| Short titles | word count **≤ 5** | points descending |
| Tie-break | same comments/points | `number` ascending |

## Scraping

- Source: first page of `https://news.ycombinator.com/` (limit 30; no `?p=2`).
- Missing score (jobs) → `points = 0`.
- `discuss` or missing comments link → `comments = 0`.
- Live HTTP uses configurable User-Agent and timeout; failures map to typed `HackerNewsFetchException` (`TIMEOUT`, `HTTP_STATUS`, `IO`).
- CI tests use HTML fixtures / fakes — never the live network.

## Usage storage extras

Required by the brief:

| Field | Purpose |
|-------|---------|
| `requestedAt` | When the interaction happened |
| `filterApplied` | Which filter was requested (`NONE`, long, short) |

Additional fields we persist to observe behavior and performance:

| Field | Purpose |
|-------|---------|
| `resultCount` | How many entries were returned (null on hard failures before a result list exists) |
| `durationMs` | End-to-end timing for the operation (fetch + filter) |
| `sourceUrl` | Which URL was scraped |
| `success` | Whether the operation completed successfully |
| `errorMessage` | Failure detail when `success = false` |

Helpers on `UsageService`:

- `recordSuccess(filter, resultCount, durationMs, sourceUrl)`
- `recordFailure(filter, durationMs, sourceUrl, errorMessage)`

Storage is **append-only**: each request inserts a new row; previous events are not updated.

## Why these tools

| Choice | Reason |
|--------|--------|
| Jsoup | HN homepage is static HTML; CSS selectors + fixture tests |
| Spring Boot | Clear layering, DI, REST, JPA testing |
| H2 | Embedded DB; zero setup for reviewers |
| springdoc / Postman / Bruno | API docs and demo collections |
