package com.stackbuilders.hncrawler.service;

import com.stackbuilders.hncrawler.domain.FilterType;
import com.stackbuilders.hncrawler.domain.HnEntry;
import com.stackbuilders.hncrawler.domain.WordCounter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Applies title-length filters and ordering rules to HN entries.
 * <ul>
 *   <li>Long titles (&gt; 5 words): ordered by comments descending</li>
 *   <li>Short titles (≤ 5 words): ordered by points descending</li>
 *   <li>Ties broken by {@code number} ascending</li>
 * </ul>
 */
@Service
public class FilterService {

    private static final int WORD_THRESHOLD = 5;

    public List<HnEntry> apply(List<HnEntry> entries, FilterType filterType) {
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        FilterType type = filterType == null ? FilterType.NONE : filterType;
        return switch (type) {
            case NONE -> List.copyOf(new ArrayList<>(entries));
            case LONG_TITLES_BY_COMMENTS -> filterLongTitles(entries);
            case SHORT_TITLES_BY_POINTS -> filterShortTitles(entries);
        };
    }

    private List<HnEntry> filterLongTitles(List<HnEntry> entries) {
        List<HnEntry> filtered = new ArrayList<>();
        for (HnEntry entry : entries) {
            if (WordCounter.countWords(entry.title()) > WORD_THRESHOLD) {
                filtered.add(entry);
            }
        }
        filtered.sort(Comparator
                .comparingInt(HnEntry::comments).reversed()
                .thenComparingInt(HnEntry::number));
        return List.copyOf(filtered);
    }

    private List<HnEntry> filterShortTitles(List<HnEntry> entries) {
        List<HnEntry> filtered = new ArrayList<>();
        for (HnEntry entry : entries) {
            if (WordCounter.countWords(entry.title()) <= WORD_THRESHOLD) {
                filtered.add(entry);
            }
        }
        filtered.sort(Comparator
                .comparingInt(HnEntry::points).reversed()
                .thenComparingInt(HnEntry::number));
        return List.copyOf(filtered);
    }
}
