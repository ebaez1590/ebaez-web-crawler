package com.stackbuilders.hncrawler.scraper;

import com.stackbuilders.hncrawler.domain.HnEntry;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsoupHackerNewsClientTest {

    @Test
    void passesConfiguredUrlUserAgentAndTimeoutToFetcher() throws Exception {
        AtomicReference<String> seenUrl = new AtomicReference<>();
        AtomicReference<String> seenUa = new AtomicReference<>();
        AtomicReference<Integer> seenTimeout = new AtomicReference<>();

        HackerNewsProperties properties = properties(
                "https://example.test/news",
                "ebaez-web-crawler-test/1.0",
                2500,
                30);

        String html = """
                <table>
                  <tr class="athing submission" id="1">
                    <td><span class="rank">1.</span></td>
                    <td><span class="titleline"><a href="#">Hello</a></span></td>
                  </tr>
                  <tr><td class="subtext"><span class="score">1 point</span><a href="item?id=1">discuss</a></td></tr>
                </table>
                """;

        HnPageFetcher fetcher = (url, userAgent, timeoutMs) -> {
            seenUrl.set(url);
            seenUa.set(userAgent);
            seenTimeout.set(timeoutMs);
            return Jsoup.parse(html, url);
        };

        JsoupHackerNewsClient client = new JsoupHackerNewsClient(properties, fetcher);
        List<HnEntry> entries = client.fetchTopEntries();

        assertThat(seenUrl.get()).isEqualTo("https://example.test/news");
        assertThat(seenUa.get()).isEqualTo("ebaez-web-crawler-test/1.0");
        assertThat(seenTimeout.get()).isEqualTo(2500);
        assertThat(entries).hasSize(1);
        assertThat(entries.get(0).title()).isEqualTo("Hello");
    }

    @Test
    void mapsHttpStatusExceptionToTypedFetchError() {
        HackerNewsProperties properties = properties("https://example.test/news", "ua", 1000, 30);
        HnPageFetcher fetcher = (url, userAgent, timeoutMs) -> {
            throw new HttpStatusException("boom", 503, url);
        };

        JsoupHackerNewsClient client = new JsoupHackerNewsClient(properties, fetcher);

        assertThatThrownBy(client::fetchTopEntries)
                .isInstanceOf(HackerNewsFetchException.class)
                .satisfies(ex -> {
                    HackerNewsFetchException fetchEx = (HackerNewsFetchException) ex;
                    assertThat(fetchEx.getKind()).isEqualTo(HackerNewsFetchException.Kind.HTTP_STATUS);
                    assertThat(fetchEx.getStatusCode()).isEqualTo(503);
                });
    }

    @Test
    void mapsSocketTimeoutExceptionToTimeoutKind() {
        HackerNewsProperties properties = properties("https://example.test/news", "ua", 1000, 30);
        HnPageFetcher fetcher = (url, userAgent, timeoutMs) -> {
            throw new SocketTimeoutException("slow");
        };

        JsoupHackerNewsClient client = new JsoupHackerNewsClient(properties, fetcher);

        assertThatThrownBy(client::fetchTopEntries)
                .isInstanceOf(HackerNewsFetchException.class)
                .satisfies(ex -> {
                    HackerNewsFetchException fetchEx = (HackerNewsFetchException) ex;
                    assertThat(fetchEx.getKind()).isEqualTo(HackerNewsFetchException.Kind.TIMEOUT);
                    assertThat(fetchEx.getMessage()).contains("1000ms");
                });
    }

    @Test
    void mapsGenericIoExceptionToIoKind() {
        HackerNewsProperties properties = properties("https://example.test/news", "ua", 1000, 30);
        HnPageFetcher fetcher = (url, userAgent, timeoutMs) -> {
            throw new IOException("connection reset");
        };

        JsoupHackerNewsClient client = new JsoupHackerNewsClient(properties, fetcher);

        assertThatThrownBy(client::fetchTopEntries)
                .isInstanceOf(HackerNewsFetchException.class)
                .satisfies(ex -> assertThat(((HackerNewsFetchException) ex).getKind())
                        .isEqualTo(HackerNewsFetchException.Kind.IO));
    }

    private static HackerNewsProperties properties(String url, String userAgent, int timeoutMs, int limit) {
        HackerNewsProperties properties = new HackerNewsProperties();
        properties.setUrl(url);
        properties.setUserAgent(userAgent);
        properties.setTimeoutMs(timeoutMs);
        properties.setLimit(limit);
        return properties;
    }
}
