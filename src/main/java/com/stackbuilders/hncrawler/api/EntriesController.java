package com.stackbuilders.hncrawler.api;

import com.stackbuilders.hncrawler.api.dto.EntriesResponse;
import com.stackbuilders.hncrawler.domain.FilterType;
import com.stackbuilders.hncrawler.domain.HnEntry;
import com.stackbuilders.hncrawler.service.CrawlService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
