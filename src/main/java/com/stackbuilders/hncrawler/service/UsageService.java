package com.stackbuilders.hncrawler.service;

import com.stackbuilders.hncrawler.domain.FilterType;
import com.stackbuilders.hncrawler.persistence.UsageEventEntity;
import com.stackbuilders.hncrawler.persistence.UsageEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Append-only usage logging for crawl/filter operations.
 */
@Service
public class UsageService {

    private final UsageEventRepository usageEventRepository;
    private final Clock clock;

    public UsageService(UsageEventRepository usageEventRepository, Clock clock) {
        this.usageEventRepository = usageEventRepository;
        this.clock = clock;
    }

    @Transactional
    public UsageEventEntity record(
            FilterType filterApplied,
            Integer resultCount,
            Long durationMs,
            String sourceUrl,
            boolean success,
            String errorMessage) {
        Objects.requireNonNull(filterApplied, "filterApplied");

        UsageEventEntity event = new UsageEventEntity(
                Instant.now(clock),
                filterApplied,
                resultCount,
                durationMs,
                sourceUrl,
                success,
                errorMessage
        );
        return usageEventRepository.save(event);
    }

    /**
     * Records a successful crawl/filter interaction.
     */
    @Transactional
    public UsageEventEntity recordSuccess(
            FilterType filterApplied,
            int resultCount,
            long durationMs,
            String sourceUrl) {
        return record(filterApplied, resultCount, durationMs, sourceUrl, true, null);
    }

    /**
     * Records a failed crawl/filter interaction.
     */
    @Transactional
    public UsageEventEntity recordFailure(
            FilterType filterApplied,
            long durationMs,
            String sourceUrl,
            String errorMessage) {
        Objects.requireNonNull(errorMessage, "errorMessage");
        return record(filterApplied, null, durationMs, sourceUrl, false, errorMessage);
    }

    @Transactional(readOnly = true)
    public List<UsageEventEntity> findAllNewestFirst() {
        return usageEventRepository.findAllByOrderByRequestedAtDesc();
    }
}