package com.stackbuilders.hncrawler.persistence;

import com.stackbuilders.hncrawler.domain.FilterType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UsageEventRepositoryTest {

    @Autowired
    private UsageEventRepository usageEventRepository;

    @Test
    void persistsAndReadsRequiredAndExtraFields() {
        Instant requestedAt = Instant.parse("2026-09-23T20:00:00Z");
        UsageEventEntity saved = usageEventRepository.save(new UsageEventEntity(
                requestedAt,
                FilterType.LONG_TITLES_BY_COMMENTS,
                12,
                345L,
                "https://news.ycombinator.com/",
                true,
                null
        ));

        assertThat(saved.getId()).isNotNull();

        UsageEventEntity loaded = usageEventRepository.findById(saved.getId()).orElseThrow();

        assertThat(loaded.getRequestedAt()).isEqualTo(requestedAt);
        assertThat(loaded.getFilterApplied()).isEqualTo(FilterType.LONG_TITLES_BY_COMMENTS);
        assertThat(loaded.getResultCount()).isEqualTo(12);
        assertThat(loaded.getDurationMs()).isEqualTo(345L);
        assertThat(loaded.getSourceUrl()).isEqualTo("https://news.ycombinator.com/");
        assertThat(loaded.isSuccess()).isTrue();
        assertThat(loaded.getErrorMessage()).isNull();
    }

    @Test
    void findsEventsOrderedByRequestedAtDescending() {
        usageEventRepository.save(new UsageEventEntity(
                Instant.parse("2026-09-23T10:00:00Z"),
                FilterType.NONE,
                30,
                100L,
                "https://news.ycombinator.com/",
                true,
                null
        ));
        usageEventRepository.save(new UsageEventEntity(
                Instant.parse("2026-09-23T12:00:00Z"),
                FilterType.SHORT_TITLES_BY_POINTS,
                8,
                90L,
                "https://news.ycombinator.com/",
                false,
                "timeout"
        ));

        List<UsageEventEntity> events = usageEventRepository.findAllByOrderByRequestedAtDesc();

        assertThat(events).hasSize(2);
        assertThat(events.get(0).getFilterApplied()).isEqualTo(FilterType.SHORT_TITLES_BY_POINTS);
        assertThat(events.get(0).isSuccess()).isFalse();
        assertThat(events.get(0).getErrorMessage()).isEqualTo("timeout");
        assertThat(events.get(1).getFilterApplied()).isEqualTo(FilterType.NONE);
    }
}
