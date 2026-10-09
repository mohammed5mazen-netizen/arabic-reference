package com.mrsoft.arabicreference.editorial.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

/**
 * Grapheme-cluster diff so Arabic combining marks stay attached to their base letter.
 */
public final class ArabicDiff {

    private static final Pattern GRAPHEME = Pattern.compile("\\X");

    private ArabicDiff() {
    }

    public enum Op {
        EQUAL,
        DELETE,
        INSERT
    }

    public record Segment(Op op, String text) {
    }

    public static List<Segment> diff(String before, String after) {
        List<String> left = clusters(before);
        List<String> right = clusters(after);
        int[][] lengths = new int[left.size() + 1][right.size() + 1];
        for (int i = left.size() - 1; i >= 0; i--) {
            for (int j = right.size() - 1; j >= 0; j--) {
                if (left.get(i).equals(right.get(j))) {
                    lengths[i][j] = lengths[i + 1][j + 1] + 1;
                } else {
                    lengths[i][j] = Math.max(lengths[i + 1][j], lengths[i][j + 1]);
                }
            }
        }
        List<Segment> segments = new ArrayList<>();
        int i = 0;
        int j = 0;
        while (i < left.size() && j < right.size()) {
            if (left.get(i).equals(right.get(j))) {
                append(segments, Op.EQUAL, left.get(i));
                i++;
                j++;
            } else if (lengths[i + 1][j] >= lengths[i][j + 1]) {
                append(segments, Op.DELETE, left.get(i));
                i++;
            } else {
                append(segments, Op.INSERT, right.get(j));
                j++;
            }
        }
        while (i < left.size()) {
            append(segments, Op.DELETE, left.get(i));
            i++;
        }
        while (j < right.size()) {
            append(segments, Op.INSERT, right.get(j));
            j++;
        }
        return List.copyOf(segments);
    }

    public static boolean preservesCombiningMark(String value) {
        List<String> clusters = clusters(value);
        return clusters.stream().noneMatch(cluster -> cluster.length() == 1 && Character.getType(cluster.charAt(0)) == Character.NON_SPACING_MARK);
    }

    private static List<String> clusters(String value) {
        if (value == null || value.isEmpty()) {
            return List.of();
        }
        return GRAPHEME.matcher(value).results().map(MatchResult::group).toList();
    }

    private static void append(List<Segment> segments, Op op, String text) {
        if (!segments.isEmpty()) {
            Segment last = segments.get(segments.size() - 1);
            if (last.op == op) {
                segments.set(segments.size() - 1, new Segment(op, last.text + text));
                return;
            }
        }
        segments.add(new Segment(op, text));
    }
}
