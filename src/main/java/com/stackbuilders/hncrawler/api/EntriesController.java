package com.stackbuilders.hncrawler.api;

import com.stackbuilders.hncrawler.api.dto.EntriesResponse;
import com.stackbuilders.hncrawler.domain.FilterType;
import com.stackbuilders.hncrawler.domain.HnEntry;
import com.stackbuilders.hncrawler.service.CrawlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/entries")
@Tag(name = "Entries", description = "Scrape and filter Hacker News entries")
public class EntriesController {

    private final CrawlService crawlService;

    public EntriesController(CrawlService crawlService) {
        this.crawlService = crawlService;
    }

    @GetMapping
    @Operation(summary = "Get top entries", description = "Scrapes the first 30 HN entries without filtering.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Entries returned",
                    content = @Content(schema = @Schema(implementation = EntriesResponse.class))),
            @ApiResponse(responseCode = "502", description = "Upstream Hacker News fetch failed")
    })
    public EntriesResponse getEntries() {
        List<HnEntry> entries = crawlService.getTopEntries();
        return EntriesResponse.of(FilterType.NONE, entries);
    }

    @GetMapping("/filter")
    @Operation(summary = "Filter top entries", description = """
            Scrapes HN and applies a title-length filter:
            long_titles (>5 words, by comments) or short_titles (<=5 words, by points).
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Filtered entries returned",
                    content = @Content(schema = @Schema(implementation = EntriesResponse.class))),
            @ApiResponse(responseCode = "400", description = "Unknown or missing filter type"),
            @ApiResponse(responseCode = "502", description = "Upstream Hacker News fetch failed")
    })
    public EntriesResponse filterEntries(
            @Parameter(description = "Filter type", example = "long_titles",
                    schema = @Schema(allowableValues = {"long_titles", "short_titles", "long", "short", "none", "all"}))
            @RequestParam("type") String type) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Query parameter 'type' is required");
        }
        FilterType filterType = FilterType.fromParam(type);
        List<HnEntry> entries = crawlService.getFilteredEntries(filterType);
        return EntriesResponse.of(filterType, entries);
    }
}
