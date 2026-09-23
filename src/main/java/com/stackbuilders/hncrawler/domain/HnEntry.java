package com.stackbuilders.hncrawler.domain;

/**
 * A single Hacker News listing entry.
 * Fields match the interview brief: number, title, points, and comments.
 */
public record HnEntry(
        int number,
        String title,
        int points,
        int comments
) {
}
