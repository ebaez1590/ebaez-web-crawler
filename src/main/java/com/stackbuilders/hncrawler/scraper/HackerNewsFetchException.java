package com.stackbuilders.hncrawler.scraper;

/**
 * Raised when the Hacker News page cannot be retrieved or parsed.
 */
public class HackerNewsFetchException extends RuntimeException {

    public enum Kind {
        TIMEOUT,
        HTTP_STATUS,
        IO,
        UNKNOWN
    }

    private final Kind kind;
    private final Integer statusCode;

    public HackerNewsFetchException(String message, Kind kind, Integer statusCode, Throwable cause) {
        super(message, cause);
        this.kind = kind;
        this.statusCode = statusCode;
    }

    public HackerNewsFetchException(String message, Throwable cause) {
        this(message, Kind.UNKNOWN, null, cause);
    }

    public HackerNewsFetchException(String message) {
        this(message, Kind.UNKNOWN, null, null);
    }

    public Kind getKind() {
        return kind;
    }

    public Integer getStatusCode() {
        return statusCode;
    }
}
