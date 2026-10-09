package com.mrsoft.arabicreference.search.application;

import com.mrsoft.arabicreference.search.application.SearchViews.FacetView;
import com.mrsoft.arabicreference.search.application.SearchViews.HighlightView;
import com.mrsoft.arabicreference.search.application.SearchViews.SearchHitView;
import com.mrsoft.arabicreference.search.application.SearchViews.SearchPageView;
import com.mrsoft.arabicreference.search.application.SearchViews.SuggestionView;
import com.mrsoft.arabicreference.search.domain.LinguisticSearchPort;
import com.mrsoft.arabicreference.search.domain.MatchReason;
import com.mrsoft.arabicreference.search.domain.MorphologySearchAssist;
import com.mrsoft.arabicreference.search.domain.SearchCandidate;
import com.mrsoft.arabicreference.search.domain.SearchEntityType;
import com.mrsoft.arabicreference.search.domain.SearchFilters;
import com.mrsoft.arabicreference.search.domain.SearchFilters.TypeGroup;
import com.mrsoft.arabicreference.search.domain.SearchGeneration;
import com.mrsoft.arabicreference.search.domain.SearchIndex;
import com.mrsoft.arabicreference.search.domain.SearchQueryPolicy;
import com.mrsoft.arabicreference.search.domain.SearchQueryPolicy.PreparedQuery;
import com.mrsoft.arabicreference.search.domain.SearchRanking;
import com.mrsoft.arabicreference.search.domain.SearchSnippets;
import com.mrsoft.arabicreference.search.domain.SearchThrottle;
import com.mrsoft.arabicreference.search.domain.SearchTuning;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SearchQueryService {

    private static final Logger log = LoggerFactory.getLogger(SearchQueryService.class);
    private static final long CACHE_TTL_MILLIS = 20_000L;
    private static final int CACHE_MAX = 64;

    private final LinguisticSearchPort search;
    private final MorphologySearchAssist morphology;
    private final SearchGeneration generation;
    private final SearchIndex index;
    private final SearchThrottle throttle;
    private final Counter requests;
    private final Counter zeroResults;
    private final Counter suggestionRequests;
    private final Timer duration;
    private final DistributionSummary resultCount;
    private final ConcurrentHashMap<String, Cached<SearchPageView>> pages = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Cached<List<SuggestionView>>> suggestions = new ConcurrentHashMap<>();

    public SearchQueryService(
            LinguisticSearchPort search,
            MorphologySearchAssist morphology,
            SearchGeneration generation,
            SearchIndex index,
            SearchThrottle throttle,
            MeterRegistry meters) {
        this.search = search;
        this.morphology = morphology;
        this.generation = generation;
        this.index = index;
        this.throttle = throttle;
        this.requests = meters.counter("search.requests");
        this.zeroResults = meters.counter("search.zero_results");
        this.suggestionRequests = meters.counter("search.suggestions");
        this.duration = meters.timer("search.duration");
        this.resultCount = DistributionSummary.builder("search.results").register(meters);
    }

    @Transactional(readOnly = true)
    public SearchPageView search(String query, String type, String partOfSpeech, int page, int size, String clientAddress) {
        throttle.acquire(clientAddress);
        Timer.Sample sample = Timer.start();
        requests.increment();
        try {
            PreparedQuery prepared = SearchQueryPolicy.prepare(query);
            TypeGroup group = SearchFilters.type(type);
            String speech = SearchFilters.partOfSpeech(partOfSpeech);
            int safePage = SearchQueryPolicy.page(page);
            int safeSize = SearchQueryPolicy.size(size);
            String cacheKey = generation.current() + "\n" + index.state().indexVersion() + "\n" + prepared.display() + "\n" + prepared.key() + "\n" + group + "\n" + speech + "\n" + safePage + "\n" + safeSize;
            Cached<SearchPageView> cached = pages.get(cacheKey);
            if (cached != null && cached.expiresAt() > System.currentTimeMillis()) {
                return cached.value();
            }
            SearchPageView view = execute(prepared, group, speech, safePage, safeSize);
            remember(pages, cacheKey, view);
            resultCount.record(view.total());
            if (view.total() == 0) {
                zeroResults.increment();
            }
            log.info("search completed results={} durationMs={}", view.total(), sample.stop(duration) / 1_000_000);
            return view;
        } catch (RuntimeException exception) {
            sample.stop(duration);
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public List<SuggestionView> suggest(String query, String clientAddress) {
        throttle.acquire(clientAddress);
        suggestionRequests.increment();
        PreparedQuery prepared = SearchQueryPolicy.prepare(query);
        String cacheKey = generation.current() + "|" + index.state().indexVersion() + "|" + prepared.key();
        Cached<List<SuggestionView>> cached = suggestions.get(cacheKey);
        if (cached != null && cached.expiresAt() > System.currentTimeMillis()) {
            return cached.value();
        }
        List<SuggestionView> views = search.suggest(prepared.key(), SearchTuning.SUGGESTION_CAP).stream()
                .map(item -> new SuggestionView(item.kind(), item.title(), item.url()))
                .toList();
        remember(suggestions, cacheKey, views);
        return views;
    }

    private SearchPageView execute(PreparedQuery prepared, TypeGroup group, String speech, int page, int size) {
        Map<String, Ranked> best = new LinkedHashMap<>();
        for (SearchCandidate candidate : search.collect(prepared.display(), prepared.key(), prepared.folded())) {
            add(best, ranked(candidate, false));
        }
        boolean strong = best.values().stream().anyMatch(hit -> hit.score() >= MatchReason.ROOT.score());
        if (!strong) {
            for (UUID entryId : morphology.entryIds(prepared.display())) {
                search.dictionaryEntry(entryId).ifPresent(candidate -> add(best, ranked(candidate, false)));
            }
        }
        long solid = best.values().stream().filter(hit -> hit.score() >= MatchReason.DEFINITION.score()).count();
        if (solid < SearchTuning.SOLID_RESULTS_BEFORE_FUZZY && prepared.key().codePointCount(0, prepared.key().length()) >= SearchTuning.FUZZY_MIN_CODE_POINTS) {
            for (SearchCandidate candidate : search.fuzzy(prepared.key(), SearchTuning.FUZZY_THRESHOLD, SearchTuning.FUZZY_CAP)) {
                add(best, ranked(candidate, false));
            }
        }
        List<Ranked> roots = best.values().stream()
                .filter(hit -> hit.type() == SearchEntityType.ROOT && hit.reason() == MatchReason.ROOT && hit.score() >= MatchReason.ROOT.score())
                .toList();
        for (Ranked root : roots) {
            if (root.rootNormalized() == null) {
                continue;
            }
            for (SearchCandidate related : search.dictionaryWithRoot(root.rootNormalized(), SearchTuning.ROOT_RELATED_CAP)) {
                add(best, ranked(related, true));
            }
        }
        List<Ranked> ordered = best.values().stream().sorted(SearchRanking.order()).toList();
        List<Ranked> speechFiltered = ordered.stream().filter(hit -> speech == null || speech.equals(hit.partOfSpeech())).toList();
        FacetView facets = facets(speechFiltered);
        List<Ranked> filtered = speechFiltered.stream().filter(hit -> SearchFilters.accepts(group, hit.type())).toList();
        int from = Math.min(filtered.size(), (page - 1) * size);
        int to = Math.min(filtered.size(), from + size);
        List<SearchHitView> items = new ArrayList<>();
        for (Ranked hit : filtered.subList(from, to)) {
            items.add(present(hit, prepared));
        }
        return new SearchPageView(prepared.display(), items, page, size, filtered.size(), facets);
    }

    private static void add(Map<String, Ranked> best, Ranked hit) {
        String key = hit.type().name() + ":" + hit.id();
        Ranked existing = best.get(key);
        if (existing == null || hit.score() > existing.score()) {
            best.put(key, hit);
        }
    }

    private static Ranked ranked(SearchCandidate candidate, boolean rootRelated) {
        int score = SearchRanking.score(candidate.reason(), candidate.fuzzyBody(), rootRelated);
        return new Ranked(
                candidate.type(),
                candidate.entityId(),
                candidate.titleOriginal(),
                candidate.titleNormalized() == null ? "" : candidate.titleNormalized(),
                candidate.snippet(),
                candidate.subtitle(),
                candidate.urlPath(),
                candidate.rootNormalized(),
                candidate.rootLabel(),
                candidate.partOfSpeech(),
                candidate.category(),
                candidate.relatedCount(),
                candidate.reason(),
                score);
    }

    private static FacetView facets(List<Ranked> hits) {
        long dictionary = hits.stream().filter(hit -> hit.type() == SearchEntityType.DICTIONARY_ENTRY).count();
        long roots = hits.stream().filter(hit -> hit.type() == SearchEntityType.ROOT).count();
        long grammar = hits.stream().filter(hit -> hit.type() == SearchEntityType.GRAMMAR_TOPIC || hit.type() == SearchEntityType.GRAMMAR_RULE || hit.type() == SearchEntityType.GRAMMAR_CONCEPT).count();
        long content = hits.stream().filter(hit -> SearchFilters.accepts(SearchFilters.TypeGroup.CONTENT, hit.type())).count();
        long learning = hits.stream().filter(hit -> SearchFilters.accepts(SearchFilters.TypeGroup.LEARNING, hit.type())).count();
        return new FacetView(dictionary, roots, grammar, content, learning);
    }

    private static SearchHitView present(Ranked hit, PreparedQuery prepared) {
        String snippet = SearchSnippets.window(hit.snippet(), prepared.folded(), SearchTuning.SNIPPET_CODE_POINTS, false);
        List<HighlightView> highlights = new ArrayList<>();
        SearchSnippets.highlights("title", hit.title(), prepared.key(), true).forEach(range -> highlights.add(new HighlightView(range.field(), range.start(), range.end())));
        SearchSnippets.highlights("snippet", snippet, prepared.folded(), false).forEach(range -> highlights.add(new HighlightView(range.field(), range.start(), range.end())));
        Map<String, String> metadata = new LinkedHashMap<>();
        if (hit.partOfSpeech() != null) {
            metadata.put("partOfSpeech", hit.partOfSpeech());
        }
        if (hit.rootLabel() != null) {
            metadata.put("root", hit.rootLabel());
        }
        if (hit.category() != null) {
            metadata.put("category", hit.category());
        }
        if (hit.type() == SearchEntityType.ROOT) {
            metadata.put("relatedCount", Integer.toString(hit.relatedCount()));
        }
        return new SearchHitView(hit.type().name(), hit.id(), hit.title(), hit.subtitle(), snippet, hit.url(), hit.reason().name(), highlights, metadata);
    }

    private <T> void remember(ConcurrentHashMap<String, Cached<T>> cache, String key, T value) {
        if (cache.size() > CACHE_MAX) {
            cache.clear();
        }
        cache.put(key, new Cached<>(value, System.currentTimeMillis() + CACHE_TTL_MILLIS));
    }

    private record Cached<T>(T value, long expiresAt) {
    }

    private record Ranked(
            SearchEntityType type,
            UUID id,
            String title,
            String titleNormalized,
            String snippet,
            String subtitle,
            String url,
            String rootNormalized,
            String rootLabel,
            String partOfSpeech,
            String category,
            int relatedCount,
            MatchReason reason,
            int score) implements SearchRanking.Ordered {
    }
}
