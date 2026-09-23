package com.stackbuilders.hncrawler.scraper;

import com.stackbuilders.hncrawler.domain.HnEntry;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Coverage for HN job posts without score/comments (HN-013).
 * Shape observed live on 2026-09-23 (QuestDB hiring post).
 */
class HnHtmlParserJobPostTest {

    @Test
    void jobPostWithoutScoreOrCommentsMapsToZeros() {
        // Mirrors live HN job markup: no votelinks, no span.score, no comments link.
        String html = """
                <table>
                  <tr class="athing submission" id="49814780">
                    <td align="right" valign="top" class="title"><span class="rank">10.</span></td>
                    <td><img src="s.gif" height="1" width="14"></td>
                    <td class="title">
                      <span class="titleline">
                        <a href="https://questdb.com/careers/pre-sales-engineer-north-america/" rel="nofollow">
                          QuestDB (YC S20) Is Hiring a Sales Engineer
                        </a>
                        <span class="sitebit comhead">
                          (<a href="from?site=questdb.com"><span class="sitestr">questdb.com</span></a>)
                        </span>
                      </span>
                    </td>
                  </tr>
                  <tr>
                    <td colspan="2"></td>
                    <td class="subtext">
                      <span class="age" title="2026-09-23T12:01:04">
                        <a href="item?id=49814780">2 hours ago</a>
                      </span>
                      | <a href="hide?id=49814780&amp;goto=news">hide</a>
                    </td>
                  </tr>
                </table>
                """;

        List<HnEntry> entries = HnHtmlParser.parse(Jsoup.parse(html), 30);

        assertThat(entries).hasSize(1);
        HnEntry job = entries.get(0);
        assertThat(job.number()).isEqualTo(10);
        assertThat(job.title()).isEqualTo("QuestDB (YC S20) Is Hiring a Sales Engineer");
        assertThat(job.points()).isZero();
        assertThat(job.comments()).isZero();
    }

    @Test
    void jobMixedWithRegularStoryDoesNotBreakParsing() {
        String html = """
                <table>
                  <tr class="athing submission" id="1">
                    <td><span class="rank">1.</span></td>
                    <td><span class="titleline"><a href="#">Regular Story Title</a></span></td>
                  </tr>
                  <tr>
                    <td class="subtext">
                      <span class="score">12 points</span>
                      <a href="item?id=1">4&nbsp;comments</a>
                    </td>
                  </tr>
                  <tr class="athing submission" id="2">
                    <td><span class="rank">2.</span></td>
                    <td><span class="titleline"><a href="#" rel="nofollow">Company Is Hiring Engineers</a></span></td>
                  </tr>
                  <tr>
                    <td class="subtext">
                      <span class="age"><a href="item?id=2">1 hour ago</a></span>
                      | <a href="hide?id=2">hide</a>
                    </td>
                  </tr>
                </table>
                """;

        List<HnEntry> entries = HnHtmlParser.parse(Jsoup.parse(html), 30);

        assertThat(entries).containsExactly(
                new HnEntry(1, "Regular Story Title", 12, 4),
                new HnEntry(2, "Company Is Hiring Engineers", 0, 0)
        );
    }
}
