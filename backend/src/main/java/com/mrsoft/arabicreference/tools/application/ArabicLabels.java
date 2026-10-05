package com.mrsoft.arabicreference.tools.application;

import com.mrsoft.arabicreference.tools.domain.ProvenanceKind;
import java.util.Map;

public final class ArabicLabels {

    private static final Map<String, String> SPEECH = Map.ofEntries(
            Map.entry("NOUN", "اسم"),
            Map.entry("VERB", "فعل"),
            Map.entry("ADJECTIVE", "صفة"),
            Map.entry("ADVERB", "ظرف"),
            Map.entry("PRONOUN", "ضمير"),
            Map.entry("PREPOSITION", "حرف جر"),
            Map.entry("CONJUNCTION", "حرف عطف"),
            Map.entry("PARTICLE", "أداة"),
            Map.entry("INTERJECTION", "اسم فعل / تعجب"),
            Map.entry("PROPER_NOUN", "علم"),
            Map.entry("OTHER", "أخرى"));

    private static final Map<String, String> PATTERN = Map.ofEntries(
            Map.entry("VERB", "فعل"),
            Map.entry("ACTIVE_PARTICIPLE", "اسم فاعل"),
            Map.entry("PASSIVE_PARTICIPLE", "اسم مفعول"),
            Map.entry("VERBAL_NOUN", "مصدر"),
            Map.entry("NOUN", "اسم"),
            Map.entry("ADJECTIVE", "صفة"),
            Map.entry("PLACE_NOUN", "اسم مكان"),
            Map.entry("TIME_NOUN", "اسم زمان"),
            Map.entry("INSTRUMENT_NOUN", "اسم آلة"),
            Map.entry("OTHER", "أخرى"));

    private static final Map<String, String> RELATION = Map.of(
            "SYNONYM", "مرادف",
            "ANTONYM", "ضد",
            "RELATED", "علاقة",
            "DERIVED_FROM", "مشتق");

    private ArabicLabels() {
    }

    public static String speech(String code) {
        return code == null ? null : SPEECH.getOrDefault(code, "تصنيف منشور");
    }

    public static String pattern(String code) {
        return code == null ? null : PATTERN.getOrDefault(code, "وزن منشور");
    }

    public static String relation(String code) {
        return code == null ? "علاقة" : RELATION.getOrDefault(code, "علاقة");
    }

    public static String provenance(ProvenanceKind kind) {
        return switch (kind) {
            case PUBLISHED_REFERENCE -> "موثّق في المرجع المنشور";
            case MANUAL_VERIFIED -> "موثّق ومراجع";
            case EXACT_DICTIONARY -> "مطابقة معجمية";
            case RULE_DERIVED -> "مستنتج بقاعدة";
            case SEARCH_SUGGESTION -> "نتيجة قريبة";
        };
    }

    public static String usage(String code) {
        if (code == null) {
            return null;
        }
        return switch (code) {
            case "CLASSICAL" -> "فصيحة تراثية";
            case "ARCHAIC" -> "مهجورة";
            case "MODERN" -> "معاصرة";
            case "COLLOQUIAL" -> "دارجة";
            case "CONVENTIONAL" -> "اصطلاحية";
            case "FIGURATIVE" -> "مجازية";
            case "RARE" -> "نادرة";
            case "TECHNICAL" -> "علمية";
            default -> null;
        };
    }

    public static String domain(String code) {
        if (code == null) {
            return null;
        }
        return switch (code) {
            case "GENERAL" -> "عام";
            case "LANGUAGE" -> "لغة";
            case "MEDICINE" -> "طب";
            case "TECHNOLOGY" -> "تقنية";
            case "ECONOMICS" -> "اقتصاد";
            case "LAW" -> "قانون";
            case "RELIGION" -> "دين";
            case "LITERATURE" -> "أدب";
            case "SCIENCE" -> "علم";
            default -> null;
        };
    }
}
