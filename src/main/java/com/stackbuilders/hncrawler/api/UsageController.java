package com.stackbuilders.hncrawler.api;

import com.stackbuilders.hncrawler.api.dto.UsageEventResponse;
import com.stackbuilders.hncrawler.service.UsageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usage")
public class UsageController {

    private final UsageService usageService;

    public UsageController(UsageService usageService) {
        this.usageService = usageService;
    }

    @GetMapping
    public List<UsageEventResponse> listUsage() {
        return usageService.findAllNewestFirst().stream()
                .map(UsageEventResponse::from)
                .toList();
    }
}
