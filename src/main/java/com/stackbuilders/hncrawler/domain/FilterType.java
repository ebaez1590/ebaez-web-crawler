package com.stackbuilders.hncrawler.domain;

/**
 * Filter operations supported by the crawler API.
 */
public enum FilterType {

    /**
     * No filter: return the scraped entries as-is.
     */
    NONE,

    /**
     * Titles with more than five words, ordered by comments descending.
     */
    LONG_TITLES_BY_COMMENTS,

    /**
     * Titles with five or fewer words, ordered by points descending.
     */
    SHORT_TITLES_BY_POINTS;

    /**
     * Resolves a query/API value such as {@code long_titles} or {@code short_titles}.
     *
     * @throws IllegalArgumentException if the value is unknown
     */
    public static FilterType fromParam(String value) {
        if (value == null || value.isBlank()) {
            return NONE;
        }
        return switch (value.trim().toLowerCase()) {
            case "none", "all" -> NONE;
            case "long_titles", "long" -> LONG_TITLES_BY_COMMENTS;
            case "short_titles", "short" -> SHORT_TITLES_BY_POINTS;
            default -> throw new IllegalArgumentException("Unknown filter type: " + value);
        };
    }
}
