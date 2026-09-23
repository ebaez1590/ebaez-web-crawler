package com.stackbuilders.hncrawler.domain;

import java.util.Arrays;

/**
 * Counts words in a title using the interview rule:
 * split on spaces, strip symbols from each token, ignore empty tokens.
 * <p>
 * Example: {@code "This is - a self-explained example"} → 5 words.
 */
public final class WordCounter {

    private WordCounter() {
    }

    /**
     * @return number of spaced words after removing symbols; {@code 0} for null/blank
     */
    public static int countWords(String title) {
        if (title == null || title.isBlank()) {
            return 0;
        }
        return (int) Arrays.stream(title.trim().split("\\s+"))
                .map(token -> token.replaceAll("[^\\p{L}\\p{N}]", ""))
                .filter(token -> !token.isEmpty())
                .count();
    }
}
