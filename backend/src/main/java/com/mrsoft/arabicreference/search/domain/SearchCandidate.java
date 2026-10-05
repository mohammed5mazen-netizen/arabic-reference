package com.mrsoft.arabicreference.search.domain;

import java.util.UUID;

public record SearchCandidate(
        SearchEntityType type,
        UUID entityId,
        String titleOriginal,
        String titleNormalized,
        String searchKey,
        String snippet,
        String subtitle,
        String urlPath,
        String rootNormalized,
        String rootLabel,
        String partOfSpeech,
        String category,
        int relatedCount,
        MatchReason reason,
        boolean fuzzyBody) {
}
