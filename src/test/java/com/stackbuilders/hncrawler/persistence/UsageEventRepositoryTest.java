package com.stackbuilders.hncrawler.persistence;

import com.stackbuilders.hncrawler.domain.FilterType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Persistence slice for usage events: save + reload of required and extra fields.
 */
@DataJpaTest
class UsageEventRepositoryTest {

    @Autowired
    private UsageEventRepository usageEventRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Persists and reloads filterApplied, requestedAt, and extras")
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
        entityManager.flush();
        entityManager.clear();

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
    @DisplayName("Persists failure events with errorMessage and null resultCount")
    void persistsFailureEventWithErrorMessage() {
        UsageEventEntity saved = usageEventRepository.save(new UsageEventEntity(
                Instant.parse("2026-09-23T21:00:00Z"),
                FilterType.NONE,
                null,
                55L,
                "https://news.ycombinator.com/",
                false,
                "Timed out after 1000ms"
        ));
        entityManager.flush();
        entityManager.clear();

        UsageEventEntity loaded = usageEventRepository.findById(saved.getId()).orElseThrow();

        assertThat(loaded.isSuccess()).isFalse();
        assertThat(loaded.getResultCount()).isNull();
        assertThat(loaded.getErrorMessage()).isEqualTo("Timed out after 1000ms");
        assertThat(loaded.getFilterApplied()).isEqualTo(FilterType.NONE);
        assertThat(loaded.getRequestedAt()).isEqualTo(Instant.parse("2026-09-23T21:00:00Z"));
    }

    @ParameterizedTest
    @EnumSource(FilterType.class)
    @DisplayName("Every FilterType round-trips as STRING enum")
    void persistsEveryFilterType(FilterType filterType) {
        UsageEventEntity saved = usageEventRepository.save(new UsageEventEntity(
                Instant.parse("2026-09-23T22:00:00Z"),
                filterType,
                1,
                10L,
                "https://news.ycombinator.com/",
                true,
                null
        ));
        entityManager.flush();
        entityManager.clear();

        assertThat(usageEventRepository.findById(saved.getId()).orElseThrow().getFilterApplied())
                .isEqualTo(filterType);
    }

    @Test
    @DisplayName("findAllByOrderByRequestedAtDesc returns newest first")
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
        entityManager.flush();
        entityManager.clear();

        List<UsageEventEntity> events = usageEventRepository.findAllByOrderByRequestedAtDesc();

        assertThat(events).hasSize(2);
        assertThat(events.get(0).getFilterApplied()).isEqualTo(FilterType.SHORT_TITLES_BY_POINTS);
        assertThat(events.get(0).getRequestedAt()).isEqualTo(Instant.parse("2026-09-23T12:00:00Z"));
        assertThat(events.get(0).isSuccess()).isFalse();
        assertThat(events.get(0).getErrorMessage()).isEqualTo("timeout");
        assertThat(events.get(1).getFilterApplied()).isEqualTo(FilterType.NONE);
        assertThat(events.get(1).getRequestedAt()).isEqualTo(Instant.parse("2026-09-23T10:00:00Z"));
    }

    @Test
    @DisplayName("findByFilterAppliedOrderByRequestedAtDesc scopes and orders")
    void findsByFilterAppliedNewestFirst() {
        usageEventRepository.save(eventAt("2026-09-23T09:00:00Z", FilterType.LONG_TITLES_BY_COMMENTS));
        usageEventRepository.save(eventAt("2026-09-23T11:00:00Z", FilterType.LONG_TITLES_BY_COMMENTS));
        usageEventRepository.save(eventAt("2026-09-23T12:00:00Z", FilterType.SHORT_TITLES_BY_POINTS));
        entityManager.flush();
        entityManager.clear();

        List<UsageEventEntity> longOnly =
                usageEventRepository.findByFilterAppliedOrderByRequestedAtDesc(
                        FilterType.LONG_TITLES_BY_COMMENTS);

        assertThat(longOnly).hasSize(2);
        assertThat(longOnly).extracting(UsageEventEntity::getRequestedAt)
                .containsExactly(
                        Instant.parse("2026-09-23T11:00:00Z"),
                        Instant.parse("2026-09-23T09:00:00Z"));
        assertThat(longOnly).allMatch(e -> e.getFilterApplied() == FilterType.LONG_TITLES_BY_COMMENTS);
    }

    private static UsageEventEntity eventAt(String instant, FilterType filter) {
        return new UsageEventEntity(
                Instant.parse(instant),
                filter,
                1,
                1L,
                "https://news.ycombinator.com/",
                true,
                null
        );
    }
}
