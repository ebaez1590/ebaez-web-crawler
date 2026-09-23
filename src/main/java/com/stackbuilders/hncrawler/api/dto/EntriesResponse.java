package com.stackbuilders.hncrawler.api.dto;

import com.stackbuilders.hncrawler.domain.FilterType;
import com.stackbuilders.hncrawler.domain.HnEntry;

import java.util.List;

public record EntriesResponse(
        FilterType filter,
        int count,
        List<HnEntryResponse> entries
) {
    public static EntriesResponse of(FilterType filter, List<HnEntry> entries) {
        List<HnEntryResponse> body = entries.stream().map(HnEntryResponse::from).toList();
        return new EntriesResponse(filter, body.size(), body);
    }
}
