package com.stackbuilders.hncrawler.service;

import com.stackbuilders.hncrawler.domain.FilterType;
import com.stackbuilders.hncrawler.domain.HnEntry;
import com.stackbuilders.hncrawler.scraper.HackerNewsClient;
import com.stackbuilders.hncrawler.scraper.HackerNewsFetchException;
import com.stackbuilders.hncrawler.scraper.HackerNewsProperties;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Orchestrates scraping and usage logging for entry retrieval.
 */
@Service
public class CrawlService {

    private final HackerNewsClient hackerNewsClient;
    private final UsageService usageService;
    private final HackerNewsProperties properties;
    private final Clock clock;

    public CrawlService(
            HackerNewsClient hackerNewsClient,
            UsageService usageService,
            HackerNewsProperties properties,
            Clock clock) {
        this.hackerNewsClient = hackerNewsClient;
        this.usageService = usageService;
        this.properties = properties;
        this.clock = clock;
    }

    /**
     * Fetches the top entries without applying a title filter and logs usage as {@link FilterType#NONE}.
     */
    public List<HnEntry> getTopEntries() {
        Instant started = Instant.now(clock);
        try {
            List<HnEntry> entries = hackerNewsClient.fetchTopEntries();
            usageService.recordSuccess(
                    FilterType.NONE,
                    entries.size(),
                    elapsedMs(started),
                    properties.getUrl());
            return entries;
        } catch (HackerNewsFetchException ex) {
            usageService.recordFailure(
                    FilterType.NONE,
                    elapsedMs(started),
                    properties.getUrl(),
                    ex.getMessage());
            throw ex;
        }
    }

    private long elapsedMs(Instant started) {
        return Duration.between(started, Instant.now(clock)).toMillis();
    }
}
