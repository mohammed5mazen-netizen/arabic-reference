package com.mrsoft.arabicreference.search.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SearchDocument(
        SearchEntityType entityType,
        UUID entityId,
        String titleOriginal,
        String titleNormalized,
        String searchKey,
        String searchableText,
        String rootNormalized,
        String rootLabel,
        List<SearchToken> tokens,
        String partOfSpeech,
        String category,
        String subtitle,
        String snippet,
        String urlPath,
        Instant publishedAt,
        int relatedCount,
        int popularity,
        int sourceQuality,
        int indexVersion) {

    public SearchDocument {
        tokens = tokens == null ? List.of() : List.copyOf(tokens);
        popularity = Math.max(popularity, 0);
        sourceQuality = Math.max(sourceQuality, 0);
    }
}
