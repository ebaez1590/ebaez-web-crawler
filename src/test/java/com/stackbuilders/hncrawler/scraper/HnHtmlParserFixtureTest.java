package com.stackbuilders.hncrawler.scraper;

import com.stackbuilders.hncrawler.domain.HnEntry;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HnHtmlParserFixtureTest {

    private static Document fixture;

    @BeforeAll
    static void loadFixture() throws IOException {
        try (InputStream in = HnHtmlParserFixtureTest.class.getResourceAsStream("/fixtures/hn-homepage.html")) {
            assertThat(in).as("classpath fixture /fixtures/hn-homepage.html").isNotNull();
            String html = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            fixture = Jsoup.parse(html, "https://news.ycombinator.com/");
        }
    }

    @Test
    void parsesExactlyThirtyEntries() {
        List<HnEntry> entries = HnHtmlParser.parse(fixture, 30);

        assertThat(entries).hasSize(30);
        assertThat(entries.get(0).number()).isEqualTo(1);
        assertThat(entries.get(29).number()).isEqualTo(30);
    }

    @Test
    void everyEntryHasRequiredShape() {
        List<HnEntry> entries = HnHtmlParser.parse(fixture, 30);

        assertThat(entries).allSatisfy(entry -> {
            assertThat(entry.number()).isBetween(1, 30);
            assertThat(entry.title()).isNotBlank();
            assertThat(entry.points()).isGreaterThanOrEqualTo(0);
            assertThat(entry.comments()).isGreaterThanOrEqualTo(0);
        });
    }

    @Test
    void firstEntryMatchesKnownFixtureValues() {
        HnEntry first = HnHtmlParser.parse(fixture, 1).get(0);

        assertThat(first.number()).isEqualTo(1);
        assertThat(first.title()).isEqualTo("Claude Opus 5.5");
        assertThat(first.points()).isEqualTo(412);
        assertThat(first.comments()).isEqualTo(467);
    }

    @Test
    void mapsDiscussEntriesToZeroComments() {
        List<HnEntry> entries = HnHtmlParser.parse(fixture, 30);

        List<HnEntry> zeroCommentEntries = entries.stream()
                .filter(entry -> entry.comments() == 0)
                .toList();

        assertThat(zeroCommentEntries)
                .as("fixture snapshot includes discuss posts")
                .isNotEmpty();
        assertThat(zeroCommentEntries).allSatisfy(entry -> {
            assertThat(entry.points()).isGreaterThanOrEqualTo(0);
            assertThat(entry.title()).isNotBlank();
        });
    }

    @Test
    void doesNotExceedConfiguredLimitWhenMoreRowsExist() {
        List<HnEntry> entries = HnHtmlParser.parse(fixture, 10);

        assertThat(entries).hasSize(10);
        assertThat(entries).extracting(HnEntry::number)
                .containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }
}
