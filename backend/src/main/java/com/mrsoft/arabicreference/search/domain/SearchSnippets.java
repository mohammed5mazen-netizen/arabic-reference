package com.mrsoft.arabicreference.search.domain;

import com.mrsoft.arabicreference.linguistics.domain.text.ArabicSearchNormalizer;
import com.mrsoft.arabicreference.linguistics.domain.text.ArabicSearchNormalizer.Alignment;
import java.util.ArrayList;
import java.util.List;

public final class SearchSnippets {

    private SearchSnippets() {
    }

    public record Range(String field, int start, int end) {
    }

    public static String window(String text, String searchKey, int maxCodePoints, boolean stripArticle) {
        if (text == null || text.isBlank()) {
            return "";
        }
        int length = text.codePointCount(0, text.length());
        Alignment alignment = ArabicSearchNormalizer.align(text, stripArticle);
        int found = searchKey == null || searchKey.isBlank() ? -1 : alignment.key().indexOf(searchKey);
        int center = 0;
        if (found >= 0 && alignment.sourceCodePoint().length > 0) {
            int keyIndex = alignment.key().codePointCount(0, found);
            if (keyIndex < alignment.sourceCodePoint().length) {
                center = alignment.sourceCodePoint()[keyIndex];
            }
        }
        int half = Math.max(1, maxCodePoints / 2);
        int start = Math.max(0, center - half);
        int end = Math.min(length, start + maxCodePoints);
        start = Math.max(0, end - maxCodePoints);
        String slice = text.substring(text.offsetByCodePoints(0, start), text.offsetByCodePoints(0, end));
        if (start > 0) {
            slice = "…" + slice;
        }
        if (end < length) {
            slice = slice + "…";
        }
        return slice;
    }

    public static List<Range> highlights(String field, String text, String searchKey, boolean stripArticle) {
        if (text == null || text.isBlank() || searchKey == null || searchKey.isBlank()) {
            return List.of();
        }
        Alignment alignment = ArabicSearchNormalizer.align(text, stripArticle);
        int found = alignment.key().indexOf(searchKey);
        if (found < 0 || alignment.sourceCodePoint().length == 0) {
            return List.of();
        }
        int keyStart = alignment.key().codePointCount(0, found);
        int keyEnd = keyStart + searchKey.codePointCount(0, searchKey.length());
        if (keyStart >= alignment.sourceCodePoint().length || keyEnd == 0) {
            return List.of();
        }
        int endIndex = Math.min(alignment.sourceCodePoint().length, keyEnd) - 1;
        int start = alignment.sourceCodePoint()[Math.min(keyStart, alignment.sourceCodePoint().length - 1)];
        int end = alignment.sourceCodePoint()[endIndex] + 1;
        if (end <= start) {
            return List.of();
        }
        List<Range> ranges = new ArrayList<>();
        ranges.add(new Range(field, start, end));
        return ranges;
    }

    public static String truncate(String value, int maxCodePoints) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.codePointCount(0, trimmed.length()) <= maxCodePoints) {
            return trimmed;
        }
        return trimmed.substring(0, trimmed.offsetByCodePoints(0, maxCodePoints));
    }
}
