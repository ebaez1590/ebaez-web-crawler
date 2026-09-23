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

/**
 * Single fixture covering normal stories, discuss, and job posts (HN-015).
 */
class HnHtmlParserCompositeFixtureTest {

    private static List<HnEntry> entries;

    @BeforeAll
    static void loadAndParse() throws IOException {
        try (InputStream in = HnHtmlParserCompositeFixtureTest.class
                .getResourceAsStream("/fixtures/hn-composite.html")) {
            assertThat(in).as("classpath fixture /fixtures/hn-composite.html").isNotNull();
            String html = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            Document document = Jsoup.parse(html, "https://news.ycombinator.com/");
            entries = HnHtmlParser.parse(document, 30);
        }
    }

    @Test
    void parsesAllCompositeRowsInOrder() {
        assertThat(entries).hasSize(4);
        assertThat(entries).extracting(HnEntry::number).containsExactly(1, 2, 3, 4);
    }

    @Test
    void regularStoryKeepsPointsAndComments() {
        assertThat(entries.get(0)).isEqualTo(
                new HnEntry(1, "Regular Story With Enough Words Here", 100, 12));
        assertThat(entries.get(3)).isEqualTo(
                new HnEntry(4, "Another Regular Post About Testing Parsers", 7, 1));
    }

    @Test
    void discussMapsToZeroComments() {
        HnEntry discuss = entries.get(1);

        assertThat(discuss.title()).isEqualTo("Short Title");
        assertThat(discuss.points()).isEqualTo(50);
        assertThat(discuss.comments()).isZero();
    }

    @Test
    void jobPostMapsToZeroPointsAndComments() {
        HnEntry job = entries.get(2);

        assertThat(job.title()).isEqualTo("Example Corp Is Hiring Platform Engineers");
        assertThat(job.points()).isZero();
        assertThat(job.comments()).isZero();
    }
}
