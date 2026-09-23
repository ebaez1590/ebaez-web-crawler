package com.stackbuilders.hncrawler.api;

import com.stackbuilders.hncrawler.domain.HnEntry;
import com.stackbuilders.hncrawler.scraper.HackerNewsFetchException;
import com.stackbuilders.hncrawler.service.CrawlService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EntriesController.class)
@Import(ApiExceptionHandler.class)
class EntriesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CrawlService crawlService;

    @Test
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

    @Test
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
}
