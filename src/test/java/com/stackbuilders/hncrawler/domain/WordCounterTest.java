package com.stackbuilders.hncrawler.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit edges for {@link WordCounter}, covering the full PLAN §7.2 table.
 */
class WordCounterTest {

    @Test
    @DisplayName("Interview example: spaced words, hyphen token stripped")
    void matchesInterviewExample() {
        assertThat(WordCounter.countWords("This is - a self-explained example")).isEqualTo(5);
    }

    @ParameterizedTest(name = "{0} → {2}")
    @MethodSource("planTableCases")
    void countsWordsPerPlanTable(String caseName, String title, int expected) {
        assertThat(WordCounter.countWords(title))
                .as(caseName)
                .isEqualTo(expected);
    }

    static Stream<Arguments> planTableCases() {
        return Stream.of(
                Arguments.of("exactly 5 words", "one two three four five", 5),
                Arguments.of("more than 5 words", "one two three four five six", 6),
                Arguments.of("fewer than 5 words", "hello world", 2),
                Arguments.of("symbols / hyphens only", "- -- ...", 0),
                Arguments.of("empty string", "", 0),
                Arguments.of("blank whitespace", "   ", 0),
                Arguments.of("punctuation attached to words", "Hello, world!", 2),
                Arguments.of("numbers as tokens", "Top 10 tools 2024", 4),
                Arguments.of("unicode / accents", "Canción en español hoy", 4)
        );
    }

    @ParameterizedTest(name = "extra edge: \"{0}\" → {1}")
    @MethodSource("extraEdgeCases")
    void countsWordsForExtraEdges(String title, int expected) {
        assertThat(WordCounter.countWords(title)).isEqualTo(expected);
    }

    static Stream<Arguments> extraEdgeCases() {
        return Stream.of(
                Arguments.of("I Don't Want the Details", 5),
                Arguments.of("self-explained", 1),
                Arguments.of("  leading and trailing  ", 3),
                Arguments.of("one\ttwo\tthree", 3),
                Arguments.of("Hello!!!", 1),
                Arguments.of("2024", 1)
        );
    }

    @ParameterizedTest(name = "null/blank/symbols → 0 [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n", "- -- ...", "!!!", "—", "...", "---"})
    void returnsZeroForNullBlankOrSymbolsOnly(String title) {
        assertThat(WordCounter.countWords(title)).isZero();
    }
}
