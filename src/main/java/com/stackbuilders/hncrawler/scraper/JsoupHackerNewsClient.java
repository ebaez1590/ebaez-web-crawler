package com.stackbuilders.hncrawler.scraper;

import com.stackbuilders.hncrawler.domain.HnEntry;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Jsoup-based {@link HackerNewsClient} that scrapes the HN homepage.
 */
@Component
public class JsoupHackerNewsClient implements HackerNewsClient {

    private final HackerNewsProperties properties;

    public JsoupHackerNewsClient(HackerNewsProperties properties) {
        this.properties = properties;
    }

    @Override
    public List<HnEntry> fetchTopEntries() {
        try {
            Document document = Jsoup.connect(properties.getUrl())
                    .userAgent(properties.getUserAgent())
                    .timeout(properties.getTimeoutMs())
                    .get();
            return HnHtmlParser.parse(document, properties.getLimit());
        } catch (IOException ex) {
            throw new HackerNewsFetchException(
                    "Failed to fetch Hacker News page: " + properties.getUrl(), ex);
        }
    }
}
