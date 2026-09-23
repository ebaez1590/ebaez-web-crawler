package com.stackbuilders.hncrawler.scraper;

import org.jsoup.nodes.Document;

import java.io.IOException;

/**
 * Fetches a remote HTML document. Extracted so live HTTP can be tested with fakes (no network in CI).
 */
@FunctionalInterface
public interface HnPageFetcher {

    Document fetch(String url, String userAgent, int timeoutMs) throws IOException;
}
