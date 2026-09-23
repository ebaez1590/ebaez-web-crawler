package com.stackbuilders.hncrawler.service;

import com.stackbuilders.hncrawler.domain.FilterType;
import com.stackbuilders.hncrawler.domain.HnEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FilterServiceTest {

    private FilterService filterService;

    @BeforeEach
    void setUp() {
        filterService = new FilterService();
    }

    @Test
    void longTitlesKeepsMoreThanFiveWordsOrderedByCommentsDesc() {
        List<HnEntry> input = List.of(
                entry(1, "one two three four five six", 10, 50),
                entry(2, "short title here", 100, 200),
                entry(3, "alpha beta gamma delta epsilon zeta", 5, 80),
                entry(4, "exactly five words right here", 20, 10)
        );

        List<HnEntry> result = filterService.apply(input, FilterType.LONG_TITLES_BY_COMMENTS);

        assertThat(result).extracting(HnEntry::number).containsExactly(3, 1);
        assertThat(result).allMatch(e -> e.number() == 1 || e.number() == 3);
    }

    @Test
    void shortTitlesKeepsFiveOrFewerWordsOrderedByPointsDesc() {
        List<HnEntry> input = List.of(
                entry(1, "one two three four five six", 999, 1),
                entry(2, "short title", 50, 0),
                entry(3, "exactly five words right here", 80, 0),
                entry(4, "tiny", 10, 0)
        );

        List<HnEntry> result = filterService.apply(input, FilterType.SHORT_TITLES_BY_POINTS);

        assertThat(result).extracting(HnEntry::number).containsExactly(3, 2, 4);
    }

    @Test
    void exactlyFiveWordsGoesToShortFilterOnly() {
        HnEntry frontier = entry(7, "one two three four five", 1, 1);
        List<HnEntry> input = List.of(frontier);

        assertThat(filterService.apply(input, FilterType.SHORT_TITLES_BY_POINTS))
                .containsExactly(frontier);
        assertThat(filterService.apply(input, FilterType.LONG_TITLES_BY_COMMENTS))
                .isEmpty();
    }

    @Test
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
    void emptyOrNullInputReturnsEmptyList() {
        assertThat(filterService.apply(List.of(), FilterType.LONG_TITLES_BY_COMMENTS)).isEmpty();
        assertThat(filterService.apply(null, FilterType.SHORT_TITLES_BY_POINTS)).isEmpty();
    }

    @Test
    void noneReturnsOriginalOrderCopy() {
        List<HnEntry> input = List.of(
                entry(1, "a b c d e f", 1, 1),
                entry(2, "short", 2, 2)
        );

        List<HnEntry> result = filterService.apply(input, FilterType.NONE);

        assertThat(result).containsExactlyElementsOf(input);
        assertThat(result).isNotSameAs(input);
    }

    @Test
    void allLongLeavesShortFilterEmpty() {
        List<HnEntry> input = List.of(
                entry(1, "one two three four five six", 1, 1),
                entry(2, "seven eight nine ten eleven twelve", 2, 2)
        );

        assertThat(filterService.apply(input, FilterType.LONG_TITLES_BY_COMMENTS)).hasSize(2);
        assertThat(filterService.apply(input, FilterType.SHORT_TITLES_BY_POINTS)).isEmpty();
    }

    @Test
    void zeroPointsAndCommentsDoNotBreakOrdering() {
        List<HnEntry> input = List.of(
                entry(2, "short one", 0, 0),
                entry(1, "short two", 0, 0)
        );

        List<HnEntry> result = filterService.apply(input, FilterType.SHORT_TITLES_BY_POINTS);

        assertThat(result).extracting(HnEntry::number).containsExactly(1, 2);
    }

    private static HnEntry entry(int number, String title, int points, int comments) {
        return new HnEntry(number, title, points, comments);
    }
}
