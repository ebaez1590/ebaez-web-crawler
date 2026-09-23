package com.stackbuilders.hncrawler.scraper;

/**
 * Raised when the Hacker News page cannot be retrieved or parsed.
 */
public class HackerNewsFetchException extends RuntimeException {

    public HackerNewsFetchException(String message, Throwable cause) {
        super(message, cause);
    }

    public HackerNewsFetchException(String message) {
        super(message);
    }
}
