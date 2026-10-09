package com.mrsoft.arabicreference.search.application;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class SearchViews {

    private SearchViews() {
    }

    public record HighlightView(String field, int start, int end) {
    }

    public record SearchHitView(
            String type,
            UUID id,
            String title,
            String subtitle,
            String snippet,
            String url,
            String matchReason,
            List<HighlightView> highlights,
            Map<String, String> metadata) {
    }

    public record FacetView(long dictionary, long roots, long grammar, long content, long learning) {
    }

    public record SearchPageView(String query, List<SearchHitView> items, int page, int size, long total, FacetView facets) {
    }

    public record SuggestionView(String kind, String title, String url) {
    }

    public record SearchStatusView(
            int indexVersion,
            long documentCount,
            String lastRebuildAt,
            Map<String, Long> counts,
            boolean consistent,
            int missingDocuments,
            int staleDocuments,
            int outdatedVersions) {
    }
}
