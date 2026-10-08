package com.mrsoft.arabicreference.ai.application;

import com.mrsoft.arabicreference.ai.application.AiModelPort.ModelCompletion;
import com.mrsoft.arabicreference.ai.application.AiViews.CitationCard;
import com.mrsoft.arabicreference.ai.application.AiViews.EvidenceCard;
import com.mrsoft.arabicreference.ai.application.EvidenceSelector.Selected;
import com.mrsoft.arabicreference.ai.domain.GroundingStatus;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class CitationGuard {

    private CitationGuard() {
    }

    public record Guarded(GroundingStatus status, boolean uncertain, String answer, List<CitationCard> citations, List<EvidenceCard> evidence, List<String> limitations) {
    }

    public static Guarded accept(ModelCompletion completion, List<Selected> selected, boolean parsing, boolean comparisonWithoutRelation, boolean missingSide) {
        Set<String> cited = new LinkedHashSet<>();
        if (completion.citedEvidenceIds() != null) {
            for (String id : completion.citedEvidenceIds()) {
                if (selected.stream().anyMatch(item -> item.evidenceId().equals(id))) {
                    cited.add(id);
                }
            }
        }
        if (cited.isEmpty()) {
            return new Guarded(GroundingStatus.INSUFFICIENT_EVIDENCE, false, AiTexts.INSUFFICIENT, List.of(), List.of(), List.of(AiTexts.INSUFFICIENT));
        }
        List<CitationCard> citations = new ArrayList<>();
        List<EvidenceCard> cards = new ArrayList<>();
        boolean uncertain = false;
        for (Selected item : selected) {
            if (!cited.contains(item.evidenceId())) {
                continue;
            }
            uncertain = uncertain || item.evidence().ruleDerived();
            citations.add(new CitationCard(item.evidenceId(), item.evidence().title(), item.evidence().canonicalUrl(), blankToNull(item.evidence().sourceLabel())));
            cards.add(new EvidenceCard(
                    item.evidenceId(),
                    item.evidence().title(),
                    typeLabel(item.evidence().entityType()),
                    item.evidence().excerpt(),
                    blankToNull(item.evidence().sourceLabel()),
                    item.evidence().canonicalUrl(),
                    provenanceLabel(item.evidence().provenance())));
        }
        List<String> limitations = new ArrayList<>();
        if (completion.limitations() != null) {
            for (String limitation : completion.limitations()) {
                String plain = AiTexts.plain(limitation, 180);
                if (!plain.isBlank() && limitations.size() < 4) {
                    limitations.add(plain);
                }
            }
        }
        if (parsing) {
            limitations.add(AiTexts.PARSING_LIMIT);
        }
        if (uncertain) {
            limitations.add(AiTexts.RULE_LIMIT);
        }
        if (missingSide) {
            limitations.add(AiTexts.MISSING_SIDE);
        }
        if (comparisonWithoutRelation) {
            limitations.add(AiTexts.COMPARISON_LIMIT);
        }
        GroundingStatus status = parsing || uncertain || missingSide || comparisonWithoutRelation
                ? GroundingStatus.PARTIALLY_GROUNDED
                : GroundingStatus.GROUNDED;
        return new Guarded(status, uncertain, AiTexts.plain(completion.answer(), 1600), citations, cards, List.copyOf(limitations));
    }

    private static String typeLabel(String type) {
        return switch (type) {
            case "DICTIONARY_ENTRY" -> "مدخل معجمي";
            case "ROOT" -> "جذر";
            case "GRAMMAR_RULE" -> "قاعدة نحوية";
            case "GRAMMAR_TOPIC" -> "موضوع نحوي";
            case "GRAMMAR_CONCEPT" -> "مصطلح نحوي";
            case "SPELLING_RULE", "SPELLING_TOPIC" -> "إملاء";
            case "RHETORIC_DEVICE", "RHETORIC_TOPIC" -> "بلاغة";
            case "LITERARY_WORK", "LITERARY_FIGURE", "LITERARY_ERA" -> "أدب";
            case "ARTICLE" -> "مقال";
            case "LESSON" -> "درس";
            case "LEARNING_PATH" -> "مسار تعليمي";
            case "MORPHOLOGY" -> "صرف";
            default -> "معرفة منشورة";
        };
    }

    private static String provenanceLabel(String provenance) {
        return switch (provenance) {
            case "EXACT_DICTIONARY" -> "مطابقة معجمية";
            case "MANUAL_VERIFIED" -> "موثّق ومراجع";
            case "RULE_DERIVED" -> "مستنتج بقاعدة";
            case "SEARCH_SUGGESTION" -> "نتيجة قريبة";
            default -> "موثّق في المرجع المنشور";
        };
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
