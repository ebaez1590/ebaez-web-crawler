package com.stackbuilders.hncrawler.scraper;

import com.stackbuilders.hncrawler.domain.HnEntry;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses the Hacker News homepage DOM into {@link HnEntry} values.
 *
 * <pre>
 *   tr.athing.submission
 *     span.rank          → number
 *     span.titleline > a → title
 *   sibling tr.subtext
 *     span.score         → points (may be absent for jobs)
 *     a[href^=item?id=]  → comments | "discuss" | absent
 * </pre>
 */
public final class HnHtmlParser {

    private static final Pattern POINTS = Pattern.compile("(\\d+)\\s+points?");
    private static final Pattern COMMENTS_HTML =
            Pattern.compile("(\\d+)\\s*&nbsp;\\s*comments?|(\\d+)\\s+comments?", Pattern.CASE_INSENSITIVE);
    private static final Pattern COMMENTS_TEXT =
            Pattern.compile("(\\d+)\\s+comments?", Pattern.CASE_INSENSITIVE);

    private HnHtmlParser() {
    }

    public static List<HnEntry> parse(Document document, int limit) {
        Elements rows = document.select("tr.athing.submission");
        List<HnEntry> entries = new ArrayList<>();

        for (Element row : rows) {
            if (entries.size() >= limit) {
                break;
            }
            entries.add(parseRow(row));
        }
        return List.copyOf(entries);
    }

    private static HnEntry parseRow(Element athIngRow) {
        int number = parseRank(athIngRow.selectFirst("span.rank"));
        String title = Optional.ofNullable(athIngRow.selectFirst("span.titleline > a"))
                .map(Element::text)
                .orElse("")
                .trim();

        Element subtext = athIngRow.nextElementSibling();
        int points = 0;
        int comments = 0;

        if (subtext != null) {
            Element score = subtext.selectFirst("span.score");
            if (score != null) {
                points = parseFirstInt(POINTS, score.text());
            }
            comments = parseComments(subtext);
        }

        return new HnEntry(number, title, points, comments);
    }

    private static int parseRank(Element rankElement) {
        if (rankElement == null) {
            return 0;
        }
        String digits = rankElement.text().replaceAll("\\D+", "");
        return digits.isEmpty() ? 0 : Integer.parseInt(digits);
    }

    private static int parseComments(Element subtext) {
        Elements itemLinks = subtext.select("a[href^=item?id=]");
        for (int i = itemLinks.size() - 1; i >= 0; i--) {
            Element link = itemLinks.get(i);
            String text = link.text().trim().toLowerCase();
            if ("discuss".equals(text)) {
                return 0;
            }
            Matcher htmlMatcher = COMMENTS_HTML.matcher(link.html());
            if (htmlMatcher.find()) {
                String group = htmlMatcher.group(1) != null ? htmlMatcher.group(1) : htmlMatcher.group(2);
                return Integer.parseInt(group);
            }
            Matcher textMatcher = COMMENTS_TEXT.matcher(link.text());
            if (textMatcher.find()) {
                return Integer.parseInt(textMatcher.group(1));
            }
        }
        return 0;
    }

    private static int parseFirstInt(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        if (!matcher.find()) {
            return 0;
        }
        return Integer.parseInt(matcher.group(1));
    }
}
