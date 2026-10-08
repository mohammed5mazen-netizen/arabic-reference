package com.mrsoft.arabicreference.ai.application;

import com.mrsoft.arabicreference.ai.domain.RetrievedEvidence;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EvidenceSelector {

    private EvidenceSelector() {
    }

    public record Selected(String evidenceId, RetrievedEvidence evidence) {
    }

    public record Selection(List<Selected> usable, List<RetrievedEvidence> nearby) {
        public boolean hasUsable() {
            return !usable.isEmpty();
        }
    }

    public static Selection select(List<RetrievedEvidence> evidence, int maxItems, int maxExcerptChars, int totalBudget) {
        Map<String, RetrievedEvidence> best = new LinkedHashMap<>();
        List<RetrievedEvidence> nearby = new ArrayList<>();
        for (RetrievedEvidence item : evidence) {
            RetrievedEvidence trimmed = trim(item, maxExcerptChars);
            if (!trimmed.usable()) {
                if (nearby.size() < 5) {
                    nearby.add(trimmed);
                }
                continue;
            }
            String key = trimmed.entityType() + ":" + trimmed.entityId();
            RetrievedEvidence current = best.get(key);
            if (current == null || trimmed.score() > current.score()) {
                best.put(key, trimmed);
            }
        }
        List<RetrievedEvidence> ranked = best.values().stream()
                .sorted((left, right) -> Integer.compare(right.score(), left.score()))
                .toList();
        List<Selected> chosen = new ArrayList<>();
        int used = 0;
        for (RetrievedEvidence item : ranked) {
            if (chosen.size() >= maxItems) {
                break;
            }
            int length = item.excerpt().codePointCount(0, item.excerpt().length());
            if (used > 0 && used + length > totalBudget) {
                break;
            }
            used += length;
            chosen.add(new Selected("E" + (chosen.size() + 1), item));
        }
        return new Selection(List.copyOf(chosen), List.copyOf(nearby));
    }

    private static RetrievedEvidence trim(RetrievedEvidence item, int maxExcerptChars) {
        return new RetrievedEvidence(
                item.entityType(),
                item.entityId(),
                AiTexts.plain(item.title(), 80),
                AiTexts.plain(item.excerpt(), maxExcerptChars),
                item.canonicalUrl(),
                AiTexts.plain(item.sourceLabel(), 120),
                item.matchReason(),
                item.provenance(),
                item.publishedVersion(),
                item.score());
    }
}
