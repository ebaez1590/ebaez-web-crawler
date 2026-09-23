# POC — Hacker News scraping (Java + Jsoup)

POC aislado para mapear el DOM real de HN, validar selectores y delimitar tickets del backlog.  
**No es** la solución final (sin Spring, API, usage DB ni Swagger).

## Cómo correr

```bash
cd poc

# Tests (fixture offline, sin red)
mvn test

# Demo con fixture guardado
mvn -q exec:java

# Demo contra HN en vivo
mvn -q exec:java -Dexec.args=live
```

## Qué incluye

| Pieza | Rol |
|-------|-----|
| `HnPageParser` | Selectores Jsoup + parse de points/comments |
| `WordCounter` | Regla del enunciado (símbolos / espacios) |
| `PocMain` | Reporte + preview de filtros A/B |
| `fixtures/hn-homepage.html` | Snapshot real para tests estables |
| Tests JUnit | 30 entries, `discuss`→0, ejemplo word count |

## Buenas prácticas aplicadas en el POC

1. **Fixture primero** — CI/tests no dependen de la red.
2. **User-Agent identificable** + timeout en live fetch.
3. **Límite explícito a 30** — no scrapeamos `?p=2`.
4. **Fallbacks a 0** — `discuss`, score ausente, links raros.
5. **Dominio mínimo** (`HnEntry` record) separado del I/O.
6. **Flags de diagnóstico** (`discussOnly`, `missingScore`) para descobrir edge cases (no hacen falta en la API final).
