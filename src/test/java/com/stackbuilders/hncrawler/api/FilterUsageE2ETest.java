package com.stackbuilders.hncrawler.api;

import com.stackbuilders.hncrawler.domain.FilterType;
import com.stackbuilders.hncrawler.domain.HnEntry;
import com.stackbuilders.hncrawler.persistence.UsageEventRepository;
import com.stackbuilders.hncrawler.scraper.HackerNewsClient;
import com.stackbuilders.hncrawler.scraper.HackerNewsFetchException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end API tests against the full Spring context with HN mocked (no network).
 */
@SpringBootTest
@AutoConfigureMockMvc
class FilterUsageE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsageEventRepository usageEventRepository;

    @MockitoBean
    private HackerNewsClient hackerNewsClient;

    @BeforeEach
    void setUp() {
        usageEventRepository.deleteAll();
        when(hackerNewsClient.fetchTopEntries()).thenReturn(fixedThirtyEntries());
    }

    @Test
    @DisplayName("long_titles → subset by comments desc + usage event")
    void filterLongTitlesOrdersByCommentsAndRecordsUsage() throws Exception {
        mockMvc.perform(get("/api/entries/filter").param("type", "long_titles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filter").value("LONG_TITLES_BY_COMMENTS"))
                .andExpect(jsonPath("$.count").value(15))
                .andExpect(jsonPath("$.entries", hasSize(15)))
                // Highest comments first (entry #1 has 99, #2 has 98, ...)
                .andExpect(jsonPath("$.entries[0].number").value(1))
                .andExpect(jsonPath("$.entries[0].comments").value(99))
                .andExpect(jsonPath("$.entries[1].number").value(2))
                .andExpect(jsonPath("$.entries[1].comments").value(98))
                .andExpect(jsonPath("$.entries[14].number").value(15))
                .andExpect(jsonPath("$.entries[14].comments").value(85));

        assertThat(usageEventRepository.findAllByOrderByRequestedAtDesc()).hasSize(1);
        var usage = usageEventRepository.findAllByOrderByRequestedAtDesc().get(0);
        assertThat(usage.getFilterApplied()).isEqualTo(FilterType.LONG_TITLES_BY_COMMENTS);
        assertThat(usage.getResultCount()).isEqualTo(15);
        assertThat(usage.isSuccess()).isTrue();
        assertThat(usage.getRequestedAt()).isNotNull();
    }

    @Test
    @DisplayName("short_titles → subset by points desc + usage event")
    void filterShortTitlesOrdersByPointsAndRecordsUsage() throws Exception {
        mockMvc.perform(get("/api/entries/filter").param("type", "short_titles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filter").value("SHORT_TITLES_BY_POINTS"))
                .andExpect(jsonPath("$.count").value(15))
                .andExpect(jsonPath("$.entries", hasSize(15)))
                // Highest points first (entry #16 has 150, #17 has 140, ...)
                .andExpect(jsonPath("$.entries[0].number").value(16))
                .andExpect(jsonPath("$.entries[0].points").value(150))
                .andExpect(jsonPath("$.entries[1].number").value(17))
                .andExpect(jsonPath("$.entries[1].points").value(140))
                .andExpect(jsonPath("$.entries[14].number").value(30))
                .andExpect(jsonPath("$.entries[14].points").value(10));

        assertThat(usageEventRepository.findAllByOrderByRequestedAtDesc()).hasSize(1);
        var usage = usageEventRepository.findAllByOrderByRequestedAtDesc().get(0);
        assertThat(usage.getFilterApplied()).isEqualTo(FilterType.SHORT_TITLES_BY_POINTS);
        assertThat(usage.getResultCount()).isEqualTo(15);
        assertThat(usage.isSuccess()).isTrue();
    }

    @Test
    @DisplayName("GET /api/usage contains long and short filter events")
    void usageEndpointListsFilterEventsFromPriorCalls() throws Exception {
        mockMvc.perform(get("/api/entries/filter").param("type", "long_titles"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/entries/filter").param("type", "short_titles"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/usage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].filterApplied").value("SHORT_TITLES_BY_POINTS"))
                .andExpect(jsonPath("$[0].success").value(true))
                .andExpect(jsonPath("$[0].requestedAt").exists())
                .andExpect(jsonPath("$[1].filterApplied").value("LONG_TITLES_BY_COMMENTS"))
                .andExpect(jsonPath("$[1].success").value(true))
                .andExpect(jsonPath("$[1].requestedAt").exists());
    }

    @Test
    @DisplayName("fetch failure → 502 and usage success=false")
    void fetchFailureReturnsBadGatewayAndRecordsFailedUsage() throws Exception {
        when(hackerNewsClient.fetchTopEntries()).thenThrow(new HackerNewsFetchException(
                "Simulated upstream failure",
                HackerNewsFetchException.Kind.IO,
                null,
                null));

        mockMvc.perform(get("/api/entries/filter").param("type", "long_titles"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.message").value("Simulated upstream failure"));

        assertThat(usageEventRepository.findAll()).hasSize(1);
        var usage = usageEventRepository.findAll().get(0);
        assertThat(usage.isSuccess()).isFalse();
        assertThat(usage.getFilterApplied()).isEqualTo(FilterType.LONG_TITLES_BY_COMMENTS);
        assertThat(usage.getErrorMessage()).isEqualTo("Simulated upstream failure");
        assertThat(usage.getResultCount()).isNull();
    }

    @Test
    @DisplayName("exactly-5-word titles stay in short filter only")
    void frontierFiveWordsGoesToShortOnly() throws Exception {
        when(hackerNewsClient.fetchTopEntries()).thenReturn(List.of(
                new HnEntry(1, "one two three four five", 42, 7),
                new HnEntry(2, "one two three four five six", 1, 99)
        ));

        mockMvc.perform(get("/api/entries/filter").param("type", "short_titles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.entries[0].number").value(1))
                .andExpect(jsonPath("$.entries[0].points").value(42));

        mockMvc.perform(get("/api/entries/filter").param("type", "long_titles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.entries[0].number").value(2))
                .andExpect(jsonPath("$.entries[0].comments").value(99));
    }

    /**
     * 30 fixed entries: 1–15 long titles (6 words) with comments 99→85;
     * 16–30 short titles with points 150→10.
     */
    private static List<HnEntry> fixedThirtyEntries() {
        List<HnEntry> entries = new ArrayList<>(30);
        for (int n = 1; n <= 15; n++) {
            entries.add(new HnEntry(
                    n,
                    "alpha beta gamma delta epsilon item" + n,
                    n,
                    100 - n));
        }
        for (int n = 16; n <= 30; n++) {
            entries.add(new HnEntry(
                    n,
                    "short " + n,
                    (31 - n) * 10,
                    0));
        }
        assertThat(entries).hasSize(30);
        assertThat(entries.subList(0, 15)).allMatch(e -> e.title().split("\\s+").length > 5);
        assertThat(entries.subList(15, 30)).allMatch(e -> e.title().split("\\s+").length <= 5);
        return List.copyOf(entries);
    }
}
