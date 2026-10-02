package com.mrsoft.arabicreference.morphology.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * A small, explicit clitic inventory. A leading letter is not a prefix unless it is in this inventory
 * and a stem remains.
 */
public final class CliticSegmenter {

    private static final List<String> PROCLITICS = List.of("و", "ف", "ب", "ل", "ك");
    private static final List<String> SUFFIXES = List.of("ها", "هم", "هن", "كم", "نا", "ك", "ه");
    private static final int MAX = 8;

    private CliticSegmenter() {
    }

    public static List<Segmentation> segment(String normalized) {
        List<Segmentation> found = new ArrayList<>();
        if (normalized == null || normalized.isBlank()) {
            return found;
        }
        found.add(Segmentation.identity(normalized));
        considerProclitic(normalized, found);
        if (normalized.startsWith("ال") && normalized.length() > 2) {
            found.add(new Segmentation(List.of(), List.of("ال"), normalized.substring(2), List.of()));
        }
        for (String proclitic : List.of("و", "ف")) {
            if (normalized.startsWith(proclitic + "ال") && normalized.length() > proclitic.length() + 2) {
                found.add(new Segmentation(List.of(proclitic), List.of("ال"), normalized.substring(proclitic.length() + 2), List.of()));
            }
        }
        for (String suffix : SUFFIXES) {
            if (normalized.endsWith(suffix) && normalized.length() - suffix.length() >= 2) {
                found.add(new Segmentation(List.of(), List.of(), normalized.substring(0, normalized.length() - suffix.length()), List.of(suffix)));
            }
        }
        return found.stream().limit(MAX).toList();
    }

    private static void considerProclitic(String normalized, List<Segmentation> found) {
        for (String proclitic : PROCLITICS) {
            if (normalized.startsWith(proclitic) && normalized.length() - proclitic.length() >= 2) {
                found.add(new Segmentation(List.of(proclitic), List.of(), normalized.substring(proclitic.length()), List.of()));
            }
        }
    }
}
