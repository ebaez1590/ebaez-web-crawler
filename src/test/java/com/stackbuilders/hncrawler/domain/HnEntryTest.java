package com.stackbuilders.hncrawler.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HnEntryTest {

    @Test
    void holdsBriefFields() {
        HnEntry entry = new HnEntry(1, "Claude Opus 5.5", 412, 467);

        assertThat(entry.number()).isEqualTo(1);
        assertThat(entry.title()).isEqualTo("Claude Opus 5.5");
        assertThat(entry.points()).isEqualTo(412);
        assertThat(entry.comments()).isEqualTo(467);
    }
}
