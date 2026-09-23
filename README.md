# POC — Hacker News scraping (Java + Jsoup)

Standalone POC to map the real HN DOM, validate selectors, and shape backlog tickets.  
**Not** the final solution (no Spring, API, usage DB, or Swagger).

## How to run

```bash
cd poc

# Offline tests (fixture, no network)
mvn test

# Demo with saved fixture
mvn -q exec:java

# Demo against live HN
mvn -q exec:java -Dexec.args=live
```

## What it includes

| Piece | Role |
|-------|------|
| `HnPageParser` | Jsoup selectors + points/comments parsing |
| `WordCounter` | Brief rule (spaced words; strip symbols) |
| `PocMain` | Report + filter A/B preview |
| `fixtures/hn-homepage.html` | Real snapshot for stable tests |
| JUnit tests | 30 entries, `discuss`→0, word-count example |

## Practices applied in this POC

1. **Fixture first** — CI/tests do not depend on the network.
2. **Identifiable User-Agent** + timeout on live fetch.
3. **Hard limit of 30** — we do not follow `?p=2`.
4. **Fallbacks to 0** — `discuss`, missing score, odd links.
5. **Minimal domain** (`HnEntry` record) separated from I/O.
6. **Diagnostic flags** (`discussOnly`, `missingScore`) to surface edge cases (not needed on the final API).
