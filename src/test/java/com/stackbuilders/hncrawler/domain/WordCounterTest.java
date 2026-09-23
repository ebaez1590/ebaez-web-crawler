package com.stackbuilders.hncrawler.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class WordCounterTest {

    @Test
    void matchesInterviewExample() {
        assertThat(WordCounter.countWords("This is - a self-explained example")).isEqualTo(5);
    }

    @ParameterizedTest
    @CsvSource({
            "one two three four five, 5",
            "one two three four five six, 6",
            "hello world, 2",
            "'Hello, world!', 2",
            "Top 10 tools 2024, 4",
            "Canción en español hoy, 4",
            "I Don't Want the Details, 5",
            "self-explained, 1"
    })
    void countsExpectedWords(String title, int expected) {
        assertThat(WordCounter.countWords(title)).isEqualTo(expected);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "- -- ...", "!!!", "—"})
    void returnsZeroForNullBlankOrSymbolsOnly(String title) {
        assertThat(WordCounter.countWords(title)).isZero();
    }
}
