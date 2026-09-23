package com.stackbuilders.hncrawler.service;

import com.stackbuilders.hncrawler.domain.FilterType;
import com.stackbuilders.hncrawler.domain.HnEntry;
import com.stackbuilders.hncrawler.scraper.HackerNewsClient;
import com.stackbuilders.hncrawler.scraper.HackerNewsFetchException;
import com.stackbuilders.hncrawler.scraper.HackerNewsProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CrawlServiceTest {

    private static final Instant FIXED = Instant.parse("2026-09-23T22:00:00Z");

    @Mock
    private HackerNewsClient hackerNewsClient;

    @Mock
    private FilterService filterService;

    @Mock
    private UsageService usageService;

    private HackerNewsProperties properties;
    private CrawlService crawlService;

    @BeforeEach
    void setUp() {
        properties = new HackerNewsProperties();
        properties.setUrl("https://news.ycombinator.com/");
        crawlService = new CrawlService(
                hackerNewsClient,
                filterService,
                usageService,
                properties,
                Clock.fixed(FIXED, ZoneOffset.UTC)
        );
    }

    @Test
    void getTopEntriesReturnsScrapedEntriesAndLogsSuccess() {
        List<HnEntry> scraped = List.of(new HnEntry(1, "Hello", 10, 2));
        when(hackerNewsClient.fetchTopEntries()).thenReturn(scraped);
        when(filterService.apply(scraped, FilterType.NONE)).thenReturn(scraped);

        List<HnEntry> result = crawlService.getTopEntries();

        assertThat(result).isEqualTo(scraped);
        verify(usageService).recordSuccess(
                FilterType.NONE,
                1,
                0L,
                "https://news.ycombinator.com/"
        );
    }

    @Test
    void getFilteredEntriesAppliesFilterAndLogsThatFilter() {
        List<HnEntry> scraped = List.of(
                new HnEntry(1, "one two three four five six", 1, 9),
                new HnEntry(2, "short", 50, 1)
        );
        List<HnEntry> filtered = List.of(scraped.get(0));
        when(hackerNewsClient.fetchTopEntries()).thenReturn(scraped);
        when(filterService.apply(scraped, FilterType.LONG_TITLES_BY_COMMENTS)).thenReturn(filtered);

        List<HnEntry> result = crawlService.getFilteredEntries(FilterType.LONG_TITLES_BY_COMMENTS);

        assertThat(result).isEqualTo(filtered);
        verify(usageService).recordSuccess(
                FilterType.LONG_TITLES_BY_COMMENTS,
                1,
                0L,
                "https://news.ycombinator.com/"
        );
    }

    @Test
    void getTopEntriesLogsFailureAndRethrowsWhenFetchFails() {
        when(hackerNewsClient.fetchTopEntries())
                .thenThrow(new HackerNewsFetchException(
                        "Timed out",
                        HackerNewsFetchException.Kind.TIMEOUT,
                        null,
                        null));

        assertThatThrownBy(() -> crawlService.getTopEntries())
                .isInstanceOf(HackerNewsFetchException.class)
                .hasMessageContaining("Timed out");

        verify(usageService).recordFailure(
                eq(FilterType.NONE),
                anyLong(),
                eq("https://news.ycombinator.com/"),
                eq("Timed out")
        );
    }
}
