package com.stackbuilders.hncrawler.scraper;

import com.stackbuilders.hncrawler.domain.HnEntry;
import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;

/**
 * Jsoup-based {@link HackerNewsClient} that scrapes the HN homepage.
 */
@Component
public class JsoupHackerNewsClient implements HackerNewsClient {

    private final HackerNewsProperties properties;
    private final HnPageFetcher pageFetcher;

    public JsoupHackerNewsClient(HackerNewsProperties properties, HnPageFetcher pageFetcher) {
        this.properties = properties;
        this.pageFetcher = pageFetcher;
    }

    @Override
    public List<HnEntry> fetchTopEntries() {
        String url = properties.getUrl();
        String userAgent = properties.getUserAgent();
        int timeoutMs = properties.getTimeoutMs();

        try {
            Document document = pageFetcher.fetch(url, userAgent, timeoutMs);
            return HnHtmlParser.parse(document, properties.getLimit());
        } catch (HttpStatusException ex) {
            throw new HackerNewsFetchException(
                    "Hacker News returned HTTP " + ex.getStatusCode() + " for " + url,
                    HackerNewsFetchException.Kind.HTTP_STATUS,
                    ex.getStatusCode(),
                    ex);
        } catch (SocketTimeoutException ex) {
            throw new HackerNewsFetchException(
                    "Timed out after " + timeoutMs + "ms fetching " + url,
                    HackerNewsFetchException.Kind.TIMEOUT,
                    null,
                    ex);
        } catch (IOException ex) {
            throw new HackerNewsFetchException(
                    "Failed to fetch Hacker News page: " + url,
                    HackerNewsFetchException.Kind.IO,
                    null,
                    ex);
        }
    }
}
