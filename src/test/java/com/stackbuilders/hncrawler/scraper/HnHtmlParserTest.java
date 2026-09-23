package com.stackbuilders.hncrawler.scraper;

import com.stackbuilders.hncrawler.domain.HnEntry;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke coverage for HN HTML selectors using inline snippets.
 */
class HnHtmlParserTest {

    @Test
    void parsesRankTitlePointsAndComments() {
        String html = """
                <table>
                  <tr class="athing submission" id="1">
                    <td class="title"><span class="rank">1.</span></td>
                    <td class="title"><span class="titleline"><a href="https://example.com">Hello World Story</a></span></td>
                  </tr>
                  <tr>
                    <td class="subtext">
                      <span class="score" id="score_1">42 points</span>
                      <a href="item?id=1">3&nbsp;comments</a>
                    </td>
                  </tr>
                </table>
                """;

        Document document = Jsoup.parse(html);
        List<HnEntry> entries = HnHtmlParser.parse(document, 30);

        assertThat(entries).hasSize(1);
        assertThat(entries.get(0)).isEqualTo(new HnEntry(1, "Hello World Story", 42, 3));
    }

    @Test
    void respectsLimit() {
        String html = """
                <table>
                  <tr class="athing submission" id="1">
                    <td><span class="rank">1.</span></td>
                    <td><span class="titleline"><a href="#">One</a></span></td>
                  </tr>
                  <tr><td class="subtext"><span class="score">1 point</span><a href="item?id=1">discuss</a></td></tr>
                  <tr class="athing submission" id="2">
                    <td><span class="rank">2.</span></td>
                    <td><span class="titleline"><a href="#">Two</a></span></td>
                  </tr>
                  <tr><td class="subtext"><span class="score">2 points</span><a href="item?id=2">1&nbsp;comment</a></td></tr>
                </table>
                """;

        List<HnEntry> entries = HnHtmlParser.parse(Jsoup.parse(html), 1);

        assertThat(entries).hasSize(1);
        assertThat(entries.get(0).number()).isEqualTo(1);
        assertThat(entries.get(0).comments()).isZero();
    }
}
