package com.stackbuilders.hncrawler.api.dto;

import com.stackbuilders.hncrawler.domain.HnEntry;

public record HnEntryResponse(
        int number,
        String title,
        int points,
        int comments
) {
    public static HnEntryResponse from(HnEntry entry) {
        return new HnEntryResponse(
                entry.number(),
                entry.title(),
                entry.points(),
                entry.comments()
        );
    }
}
