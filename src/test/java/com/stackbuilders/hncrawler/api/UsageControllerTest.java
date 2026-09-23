package com.stackbuilders.hncrawler.api;

import com.stackbuilders.hncrawler.domain.FilterType;
import com.stackbuilders.hncrawler.persistence.UsageEventEntity;
import com.stackbuilders.hncrawler.service.UsageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Slice tests for {@link UsageController}: HTTP contract with mocked {@link UsageService}.
 */
@WebMvcTest(controllers = UsageController.class)
@Import(ApiExceptionHandler.class)
class UsageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsageService usageService;

    @Test
    @DisplayName("GET /api/usage → 200 newest-first JSON shape")
    void listUsageReturnsNewestFirstPayload() throws Exception {
        when(usageService.findAllNewestFirst()).thenReturn(List.of(
                new UsageEventEntity(
                        Instant.parse("2026-09-23T23:00:00Z"),
                        FilterType.SHORT_TITLES_BY_POINTS,
                        7,
                        120L,
                        "https://news.ycombinator.com/",
                        true,
                        null
                ),
                new UsageEventEntity(
                        Instant.parse("2026-09-23T22:00:00Z"),
                        FilterType.NONE,
                        30,
                        200L,
                        "https://news.ycombinator.com/",
                        false,
                        "Timed out"
                )
        ));

        mockMvc.perform(get("/api/usage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].filterApplied").value("SHORT_TITLES_BY_POINTS"))
                .andExpect(jsonPath("$[0].resultCount").value(7))
                .andExpect(jsonPath("$[0].durationMs").value(120))
                .andExpect(jsonPath("$[0].sourceUrl").value("https://news.ycombinator.com/"))
                .andExpect(jsonPath("$[0].success").value(true))
                .andExpect(jsonPath("$[0].requestedAt").value("2026-09-23T23:00:00Z"))
                .andExpect(jsonPath("$[1].filterApplied").value("NONE"))
                .andExpect(jsonPath("$[1].success").value(false))
                .andExpect(jsonPath("$[1].errorMessage").value("Timed out"));
    }

    @Test
    @DisplayName("GET /api/usage → 200 empty array when no events")
    void listUsageReturnsEmptyArrayWhenNoEvents() throws Exception {
        when(usageService.findAllNewestFirst()).thenReturn(List.of());

        mockMvc.perform(get("/api/usage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
