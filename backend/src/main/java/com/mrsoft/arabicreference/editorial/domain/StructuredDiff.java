package com.mrsoft.arabicreference.editorial.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class StructuredDiff {

    private static final Set<String> HIDDEN = Set.of(
            "password", "passwordhash", "secret", "token", "apikey", "refreshtoken",
            "correct", "answer", "answerkey", "correctindex", "hash");

    private StructuredDiff() {
    }

    public record Change(String code, String label, String field, List<ArabicDiff.Segment> textDiff) {
    }

    public static Map<String, Object> safeFields(Map<String, ?> source) {
        Map<String, Object> safe = new LinkedHashMap<>();
        if (source == null) {
            return safe;
        }
        for (Map.Entry<String, ?> entry : source.entrySet()) {
            if (entry.getKey() != null && !hidden(entry.getKey())) {
                safe.put(entry.getKey(), entry.getValue());
            }
        }
        return safe;
    }

    public static List<Change> compare(Map<String, ?> before, Map<String, ?> after) {
        Map<String, Object> left = safeFields(before);
        Map<String, Object> right = safeFields(after);
        List<Change> changes = new ArrayList<>();
        for (String key : left.keySet()) {
            if (!right.containsKey(key)) {
                changes.add(describe(key, String.valueOf(left.get(key)), null, true));
            }
        }
        for (String key : right.keySet()) {
            if (!left.containsKey(key)) {
                changes.add(describe(key, null, String.valueOf(right.get(key)), false));
            } else if (!String.valueOf(left.get(key)).equals(String.valueOf(right.get(key)))) {
                changes.add(describe(key, String.valueOf(left.get(key)), String.valueOf(right.get(key)), false));
            }
        }
        return List.copyOf(changes);
    }

    public static boolean hidden(String key) {
        String normalized = key.toLowerCase(Locale.ROOT).replace("_", "");
        return HIDDEN.stream().anyMatch(normalized::contains);
    }

    private static Change describe(String key, String before, String after, boolean removed) {
        String normalized = key.toLowerCase(Locale.ROOT);
        String code = "FIELD_CHANGED";
        String label = "تغيّر الحقل " + key;
        if (normalized.contains("title") || normalized.contains("lemma") || normalized.equals("name")) {
            code = "TITLE_CHANGED";
            label = "تغيّر العنوان";
        } else if (normalized.contains("summary") || normalized.contains("excerpt")) {
            code = "SUMMARY_CHANGED";
            label = "تغيّر الملخص";
        } else if (normalized.contains("citation") || normalized.contains("source")) {
            code = removed ? "CITATION_REMOVED" : "CITATION_CHANGED";
            label = removed ? "حُذف استشهاد" : "تغيّر الاستشهاد";
        } else if (normalized.contains("component")) {
            code = removed ? "COMPONENT_REMOVED" : "COMPONENT_ADDED";
            label = removed ? "حُذف مكوّن" : "أُضيف مكوّن";
        } else if (normalized.contains("example")) {
            code = "EXAMPLE_EDITED";
            label = "عُدّل مثال";
        }
        return new Change(code, label, key, ArabicDiff.diff(before == null ? "" : before, after == null ? "" : after));
    }
}
