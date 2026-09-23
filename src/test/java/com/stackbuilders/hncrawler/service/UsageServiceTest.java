package com.stackbuilders.hncrawler.service;

import com.stackbuilders.hncrawler.domain.FilterType;
import com.stackbuilders.hncrawler.persistence.UsageEventEntity;
import com.stackbuilders.hncrawler.persistence.UsageEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({UsageService.class, UsageServiceTest.FixedClockConfig.class})
class UsageServiceTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2026-09-23T21:00:00Z");

    @Autowired
    private UsageService usageService;

    @Autowired
    private UsageEventRepository usageEventRepository;

    @Test
    void recordPersistsAppendOnlyUsageEvent() {
        UsageEventEntity saved = usageService.record(
                FilterType.SHORT_TITLES_BY_POINTS,
                8,
                210L,
                "https://news.ycombinator.com/",
                true,
                null
        );

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getRequestedAt()).isEqualTo(FIXED_INSTANT);
        assertThat(saved.getFilterApplied()).isEqualTo(FilterType.SHORT_TITLES_BY_POINTS);
        assertThat(saved.getResultCount()).isEqualTo(8);
        assertThat(saved.getDurationMs()).isEqualTo(210L);
        assertThat(saved.isSuccess()).isTrue();

        assertThat(usageEventRepository.count()).isEqualTo(1);
    }

    @Test
    void recordAppendsMultipleEventsWithoutUpdatingPreviousOnes() {
        UsageEventEntity first = usageService.record(
                FilterType.NONE, 30, 100L, "https://news.ycombinator.com/", true, null);
        UsageEventEntity second = usageService.record(
                FilterType.LONG_TITLES_BY_COMMENTS, 11, 120L, "https://news.ycombinator.com/", false, "timeout");

        List<UsageEventEntity> events = usageService.findAllNewestFirst();

        assertThat(events).hasSize(2);
        assertThat(events).extracting(UsageEventEntity::getFilterApplied)
                .containsExactlyInAnyOrder(FilterType.NONE, FilterType.LONG_TITLES_BY_COMMENTS);
        assertThat(usageEventRepository.findById(first.getId())).get()
                .extracting(UsageEventEntity::getFilterApplied)
                .isEqualTo(FilterType.NONE);
        assertThat(usageEventRepository.findById(second.getId())).get()
                .extracting(UsageEventEntity::isSuccess)
                .isEqualTo(false);
    }

    @Test
    void recordRequiresFilterApplied() {
        assertThatThrownBy(() -> usageService.record(null, 0, 1L, null, true, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("filterApplied");
    }

    @Test
    void recordSuccessSetsSuccessFlagAndOmitsErrorMessage() {
        UsageEventEntity saved = usageService.recordSuccess(
                FilterType.LONG_TITLES_BY_COMMENTS,
                12,
                88L,
                "https://news.ycombinator.com/"
        );

        assertThat(saved.isSuccess()).isTrue();
        assertThat(saved.getErrorMessage()).isNull();
        assertThat(saved.getResultCount()).isEqualTo(12);
        assertThat(saved.getDurationMs()).isEqualTo(88L);
        assertThat(saved.getRequestedAt()).isEqualTo(FIXED_INSTANT);
    }

    @Test
    void recordFailureSetsFailureFlagAndStoresErrorMessage() {
        UsageEventEntity saved = usageService.recordFailure(
                FilterType.NONE,
                55L,
                "https://news.ycombinator.com/",
                "Timed out after 1000ms"
        );

        assertThat(saved.isSuccess()).isFalse();
        assertThat(saved.getErrorMessage()).isEqualTo("Timed out after 1000ms");
        assertThat(saved.getResultCount()).isNull();
        assertThat(saved.getDurationMs()).isEqualTo(55L);
    }

    @Test
    void recordFailureRequiresErrorMessage() {
        assertThatThrownBy(() -> usageService.recordFailure(FilterType.NONE, 1L, null, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("errorMessage");
    }

    @TestConfiguration
    static class FixedClockConfig {
        @Bean
        Clock clock() {
            return Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);
        }
    }
}
