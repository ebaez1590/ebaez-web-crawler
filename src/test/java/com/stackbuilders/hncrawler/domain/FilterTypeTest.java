package com.stackbuilders.hncrawler.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FilterTypeTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "none", "NONE", "all"})
    void mapsBlankOrNoneAliasesToNone(String value) {
        assertThat(FilterType.fromParam(value)).isEqualTo(FilterType.NONE);
    }

    @ParameterizedTest
    @CsvSource({
            "long_titles, LONG_TITLES_BY_COMMENTS",
            "long, LONG_TITLES_BY_COMMENTS",
            "LONG_TITLES, LONG_TITLES_BY_COMMENTS",
            "short_titles, SHORT_TITLES_BY_POINTS",
            "short, SHORT_TITLES_BY_POINTS"
    })
    void mapsKnownAliases(String value, FilterType expected) {
        assertThat(FilterType.fromParam(value)).isEqualTo(expected);
    }

    @Test
    void rejectsUnknownValues() {
        assertThatThrownBy(() -> FilterType.fromParam("invalid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid");
    }
}
