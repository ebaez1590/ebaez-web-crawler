package com.stackbuilders.hncrawler.persistence;

import com.stackbuilders.hncrawler.domain.FilterType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Persisted usage event for a crawl/filter request.
 */
@Entity
@Table(name = "usage_events")
public class UsageEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant requestedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private FilterType filterApplied;

    private Integer resultCount;

    private Long durationMs;

    @Column(length = 512)
    private String sourceUrl;

    @Column(nullable = false)
    private boolean success = true;

    @Column(length = 1024)
    private String errorMessage;

    protected UsageEventEntity() {
        // JPA
    }

    public UsageEventEntity(
            Instant requestedAt,
            FilterType filterApplied,
            Integer resultCount,
            Long durationMs,
            String sourceUrl,
            boolean success,
            String errorMessage) {
        this.requestedAt = requestedAt;
        this.filterApplied = filterApplied;
        this.resultCount = resultCount;
        this.durationMs = durationMs;
        this.sourceUrl = sourceUrl;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public Long getId() {
        return id;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public FilterType getFilterApplied() {
        return filterApplied;
    }

    public Integer getResultCount() {
        return resultCount;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
