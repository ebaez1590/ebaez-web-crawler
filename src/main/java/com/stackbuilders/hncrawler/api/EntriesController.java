package com.stackbuilders.hncrawler.api;

import com.stackbuilders.hncrawler.api.dto.EntriesResponse;
import com.stackbuilders.hncrawler.domain.FilterType;
import com.stackbuilders.hncrawler.domain.HnEntry;
import com.stackbuilders.hncrawler.service.CrawlService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/entries")
public class EntriesController {

    private final CrawlService crawlService;

    public EntriesController(CrawlService crawlService) {
        this.crawlService = crawlService;
    }

    @GetMapping
    public EntriesResponse getEntries() {
        List<HnEntry> entries = crawlService.getTopEntries();
        return EntriesResponse.of(FilterType.NONE, entries);
    }

    @GetMapping("/filter")
    public EntriesResponse filterEntries(@RequestParam("type") String type) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Query parameter 'type' is required");
        }
        FilterType filterType = FilterType.fromParam(type);
        List<HnEntry> entries = crawlService.getFilteredEntries(filterType);
        return EntriesResponse.of(filterType, entries);
    }
}
