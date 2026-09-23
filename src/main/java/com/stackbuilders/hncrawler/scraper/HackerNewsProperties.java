package com.stackbuilders.hncrawler.scraper;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hn.crawler")
public class HackerNewsProperties {

    /**
     * Homepage URL to scrape.
     */
    private String url = "https://news.ycombinator.com/";

    /**
     * Identifying User-Agent sent on live requests.
     */
    private String userAgent = "ebaez-web-crawler/0.1 (+StackBuilders interview exercise)";

    /**
     * HTTP timeout in milliseconds.
     */
    private int timeoutMs = 10_000;

    /**
     * Maximum number of entries to return from the first page.
     */
    private int limit = 30;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
