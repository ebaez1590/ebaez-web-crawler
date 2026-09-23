package com.stackbuilders.hncrawler.api;

import com.stackbuilders.hncrawler.domain.FilterType;
import com.stackbuilders.hncrawler.domain.HnEntry;
import com.stackbuilders.hncrawler.scraper.HackerNewsFetchException;
import com.stackbuilders.hncrawler.service.CrawlService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Slice tests for {@link EntriesController}: HTTP contract with mocked {@link CrawlService}.
 */
@WebMvcTest(controllers = EntriesController.class)
@Import(ApiExceptionHandler.class)
class EntriesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CrawlService crawlService;

    @Test
    @DisplayName("GET /api/entries → 200 with NONE payload")
    void getEntriesReturnsRawListWithNoneFilter() throws Exception {
        when(crawlService.getTopEntries()).thenReturn(List.of(
                new HnEntry(1, "Claude Opus 5.5", 412, 467),
                new HnEntry(2, "Short Title", 10, 0)
        ));

        mockMvc.perform(get("/api/entries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filter").value("NONE"))
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.entries[0].number").value(1))
                .andExpect(jsonPath("$.entries[0].title").value("Claude Opus 5.5"))
                .andExpect(jsonPath("$.entries[0].points").value(412))
                .andExpect(jsonPath("$.entries[0].comments").value(467))
                .andExpect(jsonPath("$.entries[1].comments").value(0));
    }

    @ParameterizedTest(name = "type={0} → filter={1}")
    @CsvSource({
            "long_titles, LONG_TITLES_BY_COMMENTS",
            "long, LONG_TITLES_BY_COMMENTS",
            "short_titles, SHORT_TITLES_BY_POINTS",
            "short, SHORT_TITLES_BY_POINTS",
            "none, NONE",
            "all, NONE"
    })
    @DisplayName("GET /api/entries/filter valid types → 200")
    void filterValidTypesReturnOk(String typeParam, FilterType expectedFilter) throws Exception {
        when(crawlService.getFilteredEntries(expectedFilter))
                .thenReturn(List.of(new HnEntry(1, "sample title here ok", 10, 5)));

        mockMvc.perform(get("/api/entries/filter").param("type", typeParam))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filter").value(expectedFilter.name()))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.entries[0].number").value(1));

        verify(crawlService).getFilteredEntries(expectedFilter);
    }

    @Test
    @DisplayName("GET /api/entries/filter?type=invalid → 400")
    void filterInvalidTypeReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/entries/filter").param("type", "invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Unknown filter type: invalid"));

        verifyNoInteractions(crawlService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("GET /api/entries/filter blank type → 400")
    void filterBlankTypeReturnsBadRequest(String blank) throws Exception {
        mockMvc.perform(get("/api/entries/filter").param("type", blank))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Query parameter 'type' is required"));

        verifyNoInteractions(crawlService);
    }

    @Test
    @DisplayName("GET /api/entries/filter without type → 400")
    void filterMissingTypeReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/entries/filter"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(crawlService);
    }

    @Test
    @DisplayName("GET /api/entries → 502 when crawl fails")
    void getEntriesReturnsBadGatewayWhenFetchFails() throws Exception {
        when(crawlService.getTopEntries()).thenThrow(new HackerNewsFetchException(
                "Failed to fetch",
                HackerNewsFetchException.Kind.IO,
                null,
                null));

        mockMvc.perform(get("/api/entries"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.kind").value("IO"))
                .andExpect(jsonPath("$.message").value("Failed to fetch"));
    }

    @Test
    @DisplayName("GET /api/entries/filter → 502 when crawl fails")
    void filterReturnsBadGatewayWhenFetchFails() throws Exception {
        when(crawlService.getFilteredEntries(FilterType.LONG_TITLES_BY_COMMENTS))
                .thenThrow(new HackerNewsFetchException(
                        "Upstream timeout",
                        HackerNewsFetchException.Kind.TIMEOUT,
                        null,
                        null));

        mockMvc.perform(get("/api/entries/filter").param("type", "long_titles"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.kind").value("TIMEOUT"))
                .andExpect(jsonPath("$.message").value("Upstream timeout"));
    }
}
