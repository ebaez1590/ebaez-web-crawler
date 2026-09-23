package com.stackbuilders.hncrawler.scraper;

import com.stackbuilders.hncrawler.domain.HnEntry;

import java.util.List;

/**
 * Port for retrieving the top Hacker News entries.
 */
public interface HackerNewsClient {

    /**
     * Fetches the first page entries (limited by configuration, default 30).
     */
    List<HnEntry> fetchTopEntries();
}
