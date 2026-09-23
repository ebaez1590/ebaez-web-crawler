package com.stackbuilders.hncrawler.scraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Default live fetcher using Jsoup with configurable User-Agent and timeout.
 */
@Component
public class JsoupPageFetcher implements HnPageFetcher {

    @Override
    public Document fetch(String url, String userAgent, int timeoutMs) throws IOException {
        return Jsoup.connect(url)
                .userAgent(userAgent)
                .timeout(timeoutMs)
                .get();
    }
}
