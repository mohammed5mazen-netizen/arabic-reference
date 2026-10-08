package com.mrsoft.arabicreference.ai.application;

import com.mrsoft.arabicreference.ai.domain.AssistantIntent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class QuestionClassifier {

    private static final Set<String> STOP = Set.of(
            "ما", "هو", "هي", "في", "من", "عن", "على", "الى", "الي", "هل", "كان", "هذا", "هذه", "ذلك", "تلك",
            "كيف", "لماذا", "تعريف", "معنى", "جذر", "الفرق", "بين", "و", "او", "ثم", "مع", "ان", "لا", "لم",
            "لن", "قد", "كل", "الذي", "التي", "اعراب", "جمله", "قاعدة", "قاعده", "بلاغة", "بلاغه", "ادب", "الأدب");

    private QuestionClassifier() {
    }

    public record Resolution(AssistantIntent intent, List<String> terms, boolean asksForParsing, boolean comparisonGapCandidate) {
    }

    public static Resolution classify(String normalized) {
        boolean parsing = normalized.contains("اعراب");
        AssistantIntent intent = intentOf(normalized);
        List<String> terms = intent == AssistantIntent.COMPARISON ? comparisonTerms(normalized) : contentTerms(normalized);
        return new Resolution(intent, terms, parsing, intent == AssistantIntent.COMPARISON);
    }

    private static AssistantIntent intentOf(String normalized) {
        if (normalized.contains("الفرق بين") || normalized.contains("قارن")) {
            return AssistantIntent.COMPARISON;
        }
        if (normalized.contains("معنى") || normalized.contains("تعريف") || normalized.contains("تعني")) {
            return AssistantIntent.WORD_MEANING;
        }
        if (normalized.contains("جذر")) {
            return AssistantIntent.ROOT;
        }
        if (hasToken(normalized, "همزه", "املاء", "كتابة", "صحيحة")) {
            return AssistantIntent.SPELLING;
        }
        if (hasToken(normalized, "استعارة", "استعاره", "كناية", "كنايه", "مجاز", "جناس", "طباق", "بلاغة", "بلاغه")) {
            return AssistantIntent.RHETORIC;
        }
        if (hasToken(normalized, "شاعر", "قصيدة", "قصيده", "ادب", "اديب")) {
            return AssistantIntent.LITERATURE;
        }
        if (hasToken(normalized, "فاعل", "مفعول", "مبتدا", "خبر", "نحو", "قاعدة", "قاعده", "اعراب")) {
            return AssistantIntent.GRAMMAR;
        }
        if (hasToken(normalized, "وزن", "صرف", "تحليل")) {
            return AssistantIntent.MORPHOLOGY;
        }
        return AssistantIntent.GENERAL_LINGUISTIC;
    }

    private static List<String> comparisonTerms(String normalized) {
        int between = normalized.indexOf("بين");
        String slice = between >= 0 ? normalized.substring(between + "بين".length()) : normalized;
        String[] sides = slice.split(" و ");
        List<String> terms = new ArrayList<>();
        for (String side : sides) {
            List<String> words = contentTerms(side);
            if (!words.isEmpty()) {
                terms.add(words.get(0));
            }
            if (terms.size() == 2) {
                break;
            }
        }
        return terms;
    }

    private static boolean hasToken(String normalized, String... needles) {
        for (String token : normalized.split(" ")) {
            String cleaned = token.replaceAll("[^\\p{IsArabic}]", "");
            if (cleaned.startsWith("ال") && cleaned.length() > 2) {
                cleaned = cleaned.substring(2);
            }
            for (String needle : needles) {
                if (cleaned.equals(needle)) {
                    return true;
                }
            }
        }
        return false;
    }

    static List<String> contentTerms(String normalized) {
        List<String> terms = new ArrayList<>();
        for (String token : normalized.split(" ")) {
            String cleaned = token.replaceAll("[^\\p{IsArabic}]", "");
            if (cleaned.length() < 2 || STOP.contains(cleaned)) {
                continue;
            }
            if (!terms.contains(cleaned)) {
                terms.add(cleaned);
            }
            if (terms.size() == 3) {
                break;
            }
        }
        return terms;
    }
}
