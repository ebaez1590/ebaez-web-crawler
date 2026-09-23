package com.stackbuilders.hncrawler.service;

import com.stackbuilders.hncrawler.domain.FilterType;
import com.stackbuilders.hncrawler.domain.HnEntry;
import com.stackbuilders.hncrawler.domain.WordCounter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit edges for {@link FilterService}, covering the full PLAN §7.2 table.
 */
class FilterServiceTest {

    private FilterService filterService;

    @BeforeEach
    void setUp() {
        filterService = new FilterService();
    }

    @Test
    @DisplayName("Happy: long titles — only wordCount > 5, ordered by comments desc")
    void longTitlesKeepsMoreThanFiveWordsOrderedByCommentsDesc() {
        List<HnEntry> input = List.of(
                entry(1, "one two three four five six", 10, 50),
                entry(2, "short title here", 100, 200),
                entry(3, "alpha beta gamma delta epsilon zeta", 5, 80),
                entry(4, "exactly five words right here", 20, 10)
        );

        List<HnEntry> result = filterService.apply(input, FilterType.LONG_TITLES_BY_COMMENTS);

        assertThat(result).extracting(HnEntry::number).containsExactly(3, 1);
        assertThat(result).allMatch(e -> WordCounter.countWords(e.title()) > 5);
    }

    @Test
    @DisplayName("Happy: short titles — only wordCount <= 5, ordered by points desc")
    void shortTitlesKeepsFiveOrFewerWordsOrderedByPointsDesc() {
        List<HnEntry> input = List.of(
                entry(1, "one two three four five six", 999, 1),
                entry(2, "short title", 50, 0),
                entry(3, "exactly five words right here", 80, 0),
                entry(4, "tiny", 10, 0)
        );

        List<HnEntry> result = filterService.apply(input, FilterType.SHORT_TITLES_BY_POINTS);

        assertThat(result).extracting(HnEntry::number).containsExactly(3, 2, 4);
        assertThat(result).allMatch(e -> WordCounter.countWords(e.title()) <= 5);
    }

    @Test
    @DisplayName("Frontier: exactly 5 words goes to short only, not long")
    void exactlyFiveWordsGoesToShortFilterOnly() {
        HnEntry frontier = entry(7, "one two three four five", 1, 1);
        List<HnEntry> input = List.of(frontier);

        assertThat(filterService.apply(input, FilterType.SHORT_TITLES_BY_POINTS))
                .containsExactly(frontier);
        assertThat(filterService.apply(input, FilterType.LONG_TITLES_BY_COMMENTS))
                .isEmpty();
    }

    @Test
    @DisplayName("Tie-break: equal comments → number ascending")
    void tiesOnCommentsBrokenByNumberAscending() {
        List<HnEntry> input = List.of(
                entry(5, "one two three four five six", 1, 100),
                entry(2, "six five four three two one", 1, 100),
                entry(9, "aaaa bbbb cccc dddd eeee ffff", 1, 100)
        );

        List<HnEntry> result = filterService.apply(input, FilterType.LONG_TITLES_BY_COMMENTS);

        assertThat(result).extracting(HnEntry::number).containsExactly(2, 5, 9);
    }

    @Test
    @DisplayName("Tie-break: equal points → number ascending")
    void tiesOnPointsBrokenByNumberAscending() {
        List<HnEntry> input = List.of(
                entry(8, "alpha", 50, 0),
                entry(3, "beta", 50, 0),
                entry(1, "gamma", 50, 0)
        );

        List<HnEntry> result = filterService.apply(input, FilterType.SHORT_TITLES_BY_POINTS);

        assertThat(result).extracting(HnEntry::number).containsExactly(1, 3, 8);
    }

    @Test
    @DisplayName("Empty / null input → empty list, no exception")
    void emptyOrNullInputReturnsEmptyList() {
        assertThat(filterService.apply(List.of(), FilterType.LONG_TITLES_BY_COMMENTS)).isEmpty();
        assertThat(filterService.apply(null, FilterType.SHORT_TITLES_BY_POINTS)).isEmpty();
    }

    @Test
    @DisplayName("All long → short filter empty; all short → long filter empty")
    void allLongOrAllShortLeavesOtherFilterEmpty() {
        List<HnEntry> allLong = List.of(
                entry(1, "one two three four five six", 1, 10),
                entry(2, "seven eight nine ten eleven twelve", 2, 20)
        );
        assertThat(filterService.apply(allLong, FilterType.LONG_TITLES_BY_COMMENTS)).hasSize(2);
        assertThat(filterService.apply(allLong, FilterType.SHORT_TITLES_BY_POINTS)).isEmpty();

        List<HnEntry> allShort = List.of(
                entry(1, "alpha", 10, 1),
                entry(2, "beta gamma", 20, 2),
                entry(3, "one two three four five", 5, 3)
        );
        assertThat(filterService.apply(allShort, FilterType.SHORT_TITLES_BY_POINTS)).hasSize(3);
        assertThat(filterService.apply(allShort, FilterType.LONG_TITLES_BY_COMMENTS)).isEmpty();
    }

    @Test
    @DisplayName("Zero points/comments: stable order, no NPE")
    void zeroPointsAndCommentsDoNotBreakOrdering() {
        List<HnEntry> shortInput = List.of(
                entry(2, "short one", 0, 0),
                entry(1, "short two", 0, 0)
        );
        assertThat(filterService.apply(shortInput, FilterType.SHORT_TITLES_BY_POINTS))
                .extracting(HnEntry::number)
                .containsExactly(1, 2);

        List<HnEntry> longInput = List.of(
                entry(4, "one two three four five six", 0, 0),
                entry(3, "six five four three two one", 0, 0)
        );
        assertThat(filterService.apply(longInput, FilterType.LONG_TITLES_BY_COMMENTS))
                .extracting(HnEntry::number)
                .containsExactly(3, 4);
    }

    @Test
    @DisplayName("NONE returns defensive copy in original order")
    void noneReturnsOriginalOrderCopy() {
        List<HnEntry> input = new ArrayList<>(List.of(
                entry(1, "a b c d e f", 1, 1),
                entry(2, "short", 2, 2)
        ));

        List<HnEntry> result = filterService.apply(input, FilterType.NONE);

        assertThat(result).containsExactlyElementsOf(input);
        assertThat(result).isNotSameAs(input);
        input.clear();
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("null filterType is treated as NONE")
    void nullFilterTypeTreatedAsNone() {
        List<HnEntry> input = List.of(entry(1, "title", 1, 1));

        assertThat(filterService.apply(input, null)).containsExactlyElementsOf(input);
    }

    private static HnEntry entry(int number, String title, int points, int comments) {
        return new HnEntry(number, title, points, comments);
    }
}
