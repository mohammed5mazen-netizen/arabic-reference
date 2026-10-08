package com.mrsoft.arabicreference.search;

import static org.assertj.core.api.Assertions.assertThat;

import com.mrsoft.arabicreference.search.domain.MatchReason;
import com.mrsoft.arabicreference.search.domain.SearchEntityType;
import com.mrsoft.arabicreference.search.domain.SearchQueryPolicy;
import com.mrsoft.arabicreference.search.domain.SearchRanking;
import com.mrsoft.arabicreference.search.domain.SearchSnippets;
import com.mrsoft.arabicreference.search.domain.SearchTuning;
import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SearchRankingTest {

    @Test
    void scoresPutExactAheadOfMeaningAndMorphologyAheadOfFuzzyBody() {
        assertThat(MatchReason.EXACT.score()).isGreaterThan(MatchReason.DEFINITION.score());
        assertThat(MatchReason.NORMALIZED_EXACT.score()).isGreaterThan(MatchReason.FUZZY.score());
        assertThat(MatchReason.WORD_FORM.score()).isGreaterThan(MatchReason.FUZZY.score());
        assertThat(MatchReason.MORPHOLOGY.score()).isGreaterThan(MatchReason.FUZZY_BODY_SCORE);
        assertThat(MatchReason.MORPHOLOGY.score()).isLessThan(MatchReason.EXACT.score());
        assertThat(MatchReason.ROOT.score()).isGreaterThan(MatchReason.TITLE_PREFIX.score());
    }

    @Test
    void orderingIsStable() {
        UUID first = UUID.fromString("00000000-0000-4000-8000-000000000001");
        UUID second = UUID.fromString("00000000-0000-4000-8000-000000000002");
        List<Hit> hits = new ArrayList<>(List.of(
                hit(second, SearchEntityType.ROOT, "كتاب", MatchReason.EXACT.score()),
                hit(first, SearchEntityType.DICTIONARY_ENTRY, "كتاب", MatchReason.EXACT.score()),
                hit(first, SearchEntityType.GRAMMAR_CONCEPT, "فاعل", MatchReason.DEFINITION.score())));
        List<Hit> reversed = new ArrayList<>(hits.reversed());
        hits.sort(SearchRanking.order());
        reversed.sort(SearchRanking.order());
        assertThat(hits).containsExactlyElementsOf(reversed);
        assertThat(hits.get(0).type()).isEqualTo(SearchEntityType.DICTIONARY_ENTRY);
        assertThat(hits.get(1).type()).isEqualTo(SearchEntityType.ROOT);
        List<Hit> tied = new ArrayList<>(List.of(
                hit(second, SearchEntityType.LESSON, "كتاب", MatchReason.EXACT.score()),
                hit(first, SearchEntityType.DICTIONARY_ENTRY, "كتاب", MatchReason.EXACT.score())));
        tied.sort(SearchRanking.order());
        assertThat(tied.get(0).type()).isEqualTo(SearchEntityType.DICTIONARY_ENTRY);
    }

    @Test
    void queryLimitsAndSnippetsStayUnicodeSafe() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> SearchQueryPolicy.prepare(" ")).isInstanceOf(ValidationException.class);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> SearchQueryPolicy.prepare("ك")).isInstanceOf(ValidationException.class);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> SearchQueryPolicy.prepare("ك".repeat(SearchTuning.MAX_CODE_POINTS + 1))).isInstanceOf(ValidationException.class);
        SearchQueryPolicy.PreparedQuery prepared = SearchQueryPolicy.prepare("كتاب");
        assertThat(prepared.key()).isEqualTo("كتاب");
        String snippet = SearchSnippets.window("الشخص الذي يكتب الدرس في الكتاب كل يوم حتى يمتلئ السطر", "يكتب", 8, false);
        assertThat(snippet.codePointCount(0, snippet.length())).isLessThanOrEqualTo(10);
        List<SearchSnippets.Range> ranges = SearchSnippets.highlights("title", "كتاب", "كتاب", true);
        assertThat(ranges).containsExactly(new SearchSnippets.Range("title", 0, 4));
        assertThat(SearchTuning.FUZZY_THRESHOLD).isGreaterThanOrEqualTo(0.4);
        assertThat(SearchTuning.FUZZY_MIN_CODE_POINTS).isGreaterThanOrEqualTo(4);
    }

    private static Hit hit(UUID id, SearchEntityType type, String title, int score) {
        return new Hit(type, id, title, score);
    }

    private record Hit(SearchEntityType type, UUID id, String titleNormalized, int score) implements SearchRanking.Ordered {
    }
}
