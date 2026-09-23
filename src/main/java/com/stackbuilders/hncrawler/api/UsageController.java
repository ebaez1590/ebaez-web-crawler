package com.stackbuilders.hncrawler.api;

import com.stackbuilders.hncrawler.api.dto.UsageEventResponse;
import com.stackbuilders.hncrawler.service.UsageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usage")
@Tag(name = "Usage", description = "Inspect recorded crawl/filter interactions")
public class UsageController {

    private final UsageService usageService;

    public UsageController(UsageService usageService) {
        this.usageService = usageService;
    }

    @GetMapping
    @Operation(summary = "List usage events", description = "Returns usage events newest first.")
    @ApiResponse(responseCode = "200", description = "Usage history")
    public List<UsageEventResponse> listUsage() {
        return usageService.findAllNewestFirst().stream()
                .map(UsageEventResponse::from)
                .toList();
    }
}
