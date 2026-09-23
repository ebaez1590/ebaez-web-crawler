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
import java.util.Objects;

/**
 * Orchestrates scraping, filtering, and usage logging.
 */
@Service
public class CrawlService {

    private final HackerNewsClient hackerNewsClient;
    private final FilterService filterService;
    private final UsageService usageService;
    private final HackerNewsProperties properties;
    private final Clock clock;

    public CrawlService(
            HackerNewsClient hackerNewsClient,
            FilterService filterService,
            UsageService usageService,
            HackerNewsProperties properties,
            Clock clock) {
        this.hackerNewsClient = hackerNewsClient;
        this.filterService = filterService;
        this.usageService = usageService;
        this.properties = properties;
        this.clock = clock;
    }

    /**
     * Fetches the top entries without applying a title filter and logs usage as {@link FilterType#NONE}.
     */
    public List<HnEntry> getTopEntries() {
        return getFilteredEntries(FilterType.NONE);
    }

    /**
     * Fetches entries, applies the given filter, and logs usage for that filter.
     */
    public List<HnEntry> getFilteredEntries(FilterType filterType) {
        Objects.requireNonNull(filterType, "filterType");
        Instant started = Instant.now(clock);
        try {
            List<HnEntry> scraped = hackerNewsClient.fetchTopEntries();
            List<HnEntry> result = filterService.apply(scraped, filterType);
            usageService.recordSuccess(
                    filterType,
                    result.size(),
                    elapsedMs(started),
                    properties.getUrl());
            return result;
        } catch (HackerNewsFetchException ex) {
            usageService.recordFailure(
                    filterType,
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
