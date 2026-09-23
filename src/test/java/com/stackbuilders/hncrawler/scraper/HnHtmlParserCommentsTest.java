package com.stackbuilders.hncrawler.scraper;

import com.stackbuilders.hncrawler.domain.HnEntry;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Dedicated coverage for comment-count variants observed on HN (HN-012).
 */
class HnHtmlParserCommentsTest {

    @Test
    void discussMapsToZeroComments() {
        String html = entryHtml(
                1,
                "Show HN: New Project",
                "10 points",
                "<a href=\"item?id=1\">discuss</a>"
        );

        HnEntry entry = parseSingle(html);

        assertThat(entry.comments()).isZero();
        assertThat(entry.points()).isEqualTo(10);
    }

    @ParameterizedTest
    @CsvSource({
            "'1&nbsp;comment', 1",
            "'1 comment', 1",
            "'2&nbsp;comments', 2",
            "'467&nbsp;comments', 467",
            "'467 comments', 467"
    })
    void parsesSingularPluralAndNbspVariants(String commentsAnchorHtml, int expectedComments) {
        String html = entryHtml(
                3,
                "Sample Title Here",
                "25 points",
                "<a href=\"item?id=3\">" + commentsAnchorHtml + "</a>"
        );

        assertThat(parseSingle(html).comments()).isEqualTo(expectedComments);
    }

    @Test
    void prefersCommentsLinkWhenAgeLinkAlsoPointsToItem() {
        String html = """
                <table>
                  <tr class="athing submission" id="9">
                    <td><span class="rank">9.</span></td>
                    <td><span class="titleline"><a href="#">Story With Age Link</a></span></td>
                  </tr>
                  <tr>
                    <td class="subtext">
                      <span class="score">8 points</span>
                      by <a href="user?id=x">x</a>
                      <span class="age"><a href="item?id=9">2 hours ago</a></span>
                      | <a href="item?id=9">14&nbsp;comments</a>
                    </td>
                  </tr>
                </table>
                """;

        HnEntry entry = parseSingle(html);

        assertThat(entry.comments()).isEqualTo(14);
        assertThat(entry.points()).isEqualTo(8);
    }

    @Test
    void missingCommentsLinkMapsToZero() {
        String html = """
                <table>
                  <tr class="athing submission" id="10">
                    <td><span class="rank">10.</span></td>
                    <td><span class="titleline"><a href="#">Job Post Style Subtext</a></span></td>
                  </tr>
                  <tr>
                    <td class="subtext">
                      <span class="age"><a href="item?id=10">1 hour ago</a></span>
                      | <a href="hide?id=10">hide</a>
                    </td>
                  </tr>
                </table>
                """;

        HnEntry entry = parseSingle(html);

        assertThat(entry.comments()).isZero();
        assertThat(entry.points()).isZero();
    }

    private static HnEntry parseSingle(String html) {
        List<HnEntry> entries = HnHtmlParser.parse(Jsoup.parse(html), 30);
        assertThat(entries).hasSize(1);
        return entries.get(0);
    }

    private static String entryHtml(int number, String title, String scoreText, String commentsAnchor) {
        return """
                <table>
                  <tr class="athing submission" id="%d">
                    <td><span class="rank">%d.</span></td>
                    <td><span class="titleline"><a href="#">%s</a></span></td>
                  </tr>
                  <tr>
                    <td class="subtext">
                      <span class="score">%s</span>
                      %s
                    </td>
                  </tr>
                </table>
                """.formatted(number, number, title, scoreText, commentsAnchor);
    }
}
