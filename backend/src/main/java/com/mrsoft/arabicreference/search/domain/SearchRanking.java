package com.mrsoft.arabicreference.search.domain;

import java.util.Comparator;
import java.util.UUID;

public final class SearchRanking {

    private SearchRanking() {
    }

    public static int score(MatchReason reason, boolean fuzzyBody, boolean rootRelated) {
        if (reason == MatchReason.FUZZY && fuzzyBody) {
            return MatchReason.FUZZY_BODY_SCORE;
        }
        if (reason == MatchReason.ROOT && rootRelated) {
            return MatchReason.ROOT_RELATED_SCORE;
        }
        return reason.score();
    }

    public static Comparator<Ordered> order() {
        return Comparator.comparingInt(Ordered::score).reversed()
                .thenComparingInt(hit -> hit.type().priority())
                .thenComparing(Ordered::titleNormalized, Comparator.nullsLast(String::compareTo))
                .thenComparing(hit -> hit.id().toString());
    }

    public interface Ordered {
        int score();

        SearchEntityType type();

        String titleNormalized();

        UUID id();
    }
}
