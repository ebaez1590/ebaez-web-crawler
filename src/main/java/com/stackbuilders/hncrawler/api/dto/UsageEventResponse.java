package com.stackbuilders.hncrawler.api.dto;

import com.stackbuilders.hncrawler.domain.FilterType;
import com.stackbuilders.hncrawler.persistence.UsageEventEntity;

import java.time.Instant;

public record UsageEventResponse(
        Long id,
        Instant requestedAt,
        FilterType filterApplied,
        Integer resultCount,
        Long durationMs,
        String sourceUrl,
        boolean success,
        String errorMessage
) {
    public static UsageEventResponse from(UsageEventEntity entity) {
        return new UsageEventResponse(
                entity.getId(),
                entity.getRequestedAt(),
                entity.getFilterApplied(),
                entity.getResultCount(),
                entity.getDurationMs(),
                entity.getSourceUrl(),
                entity.isSuccess(),
                entity.getErrorMessage()
        );
    }
}
