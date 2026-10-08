package com.mrsoft.arabicreference.ai.infrastructure;

import com.mrsoft.arabicreference.ai.application.KnowledgeRetrievalPort;
import com.mrsoft.arabicreference.ai.domain.AssistantIntent;
import com.mrsoft.arabicreference.ai.domain.EvidenceScores;
import com.mrsoft.arabicreference.ai.domain.RetrievedEvidence;
import com.mrsoft.arabicreference.dictionary.application.PublishedDictionaryQuery;
import com.mrsoft.arabicreference.dictionary.application.PublishedDictionaryQuery.PublishedLemma;
import com.mrsoft.arabicreference.dictionary.application.PublishedDictionaryQuery.PublishedRelation;
import com.mrsoft.arabicreference.dictionary.application.PublishedDictionaryQuery.PublishedRoot;
import com.mrsoft.arabicreference.dictionary.application.PublishedDictionaryQuery.PublishedSense;
import com.mrsoft.arabicreference.grammar.application.GrammarQueryService;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.SearchHit;
import com.mrsoft.arabicreference.linguistics.domain.text.ArabicSearchNormalizer;
import com.mrsoft.arabicreference.linguistics.domain.text.ArabicTextNormalizer;
import com.mrsoft.arabicreference.morphology.application.MorphologyService;
import com.mrsoft.arabicreference.morphology.domain.AnalysisCandidate;
import com.mrsoft.arabicreference.morphology.domain.AnalysisProvenance;
import com.mrsoft.arabicreference.morphology.domain.AnalysisReport;
import com.mrsoft.arabicreference.search.domain.LinguisticSearchPort;
import com.mrsoft.arabicreference.search.domain.MatchReason;
import com.mrsoft.arabicreference.search.domain.SearchCandidate;
import com.mrsoft.arabicreference.search.domain.SearchEntityType;
import com.mrsoft.arabicreference.search.domain.SearchGeneration;
import com.mrsoft.arabicreference.search.domain.SearchIndex;
import com.mrsoft.arabicreference.search.domain.SearchTuning;
import com.mrsoft.arabicreference.spelling.application.SpellingQueryService;
import com.mrsoft.arabicreference.spelling.application.SpellingViews.SpellingMatch;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PublishedKnowledgeRetrieval implements KnowledgeRetrievalPort {

    private static final Logger log = LoggerFactory.getLogger(PublishedKnowledgeRetrieval.class);
    private static final ArabicTextNormalizer NORMALIZER = new ArabicTextNormalizer();

    private final PublishedDictionaryQuery dictionary;
    private final GrammarQueryService grammar;
    private final SpellingQueryService spelling;
    private final MorphologyService morphology;
    private final LinguisticSearchPort search;
    private final SearchGeneration generation;
    private final SearchIndex index;

    public PublishedKnowledgeRetrieval(
            PublishedDictionaryQuery dictionary,
            GrammarQueryService grammar,
            SpellingQueryService spelling,
            MorphologyService morphology,
            LinguisticSearchPort search,
            SearchGeneration generation,
            SearchIndex index) {
        this.dictionary = dictionary;
        this.grammar = grammar;
        this.spelling = spelling;
        this.morphology = morphology;
        this.search = search;
        this.generation = generation;
        this.index = index;
    }

    @Override
    public List<String> suggestions() {
        List<String> suggestions = new ArrayList<>();
        if (!dictionary.lemmas(normalize("كتاب")).isEmpty()) {
            suggestions.add("ما معنى كتاب؟");
            suggestions.add("ما جذر كتاب؟");
        }
        try {
            if (!grammar.search("فاعل", 0, 1).items().isEmpty()) {
                suggestions.add("ما هو الفاعل؟");
            }
        } catch (RuntimeException exception) {
            log.warn("Grammar suggestion lookup skipped");
        }
        return suggestions;
    }

    @Override
    public KnowledgeStamp stamp() {
        return new KnowledgeStamp(dictionary.contentStamp(), morphology.ruleGeneration(), generation.current(), index.state().indexVersion());
    }

    @Override
    public RetrievalBatch retrieve(RetrievalRequest request, String client) {
        List<RetrievedEvidence> evidence = new ArrayList<>();
        boolean relation = false;
        List<String> terms = request.terms().isEmpty() ? List.of(shorten(request.normalized())) : request.terms();
        for (String term : terms) {
            if (term == null || term.isBlank()) {
                continue;
            }
            relation |= route(request.intent(), term, evidence, client);
            search(term, evidence, typesFor(request.intent()));
        }
        return new RetrievalBatch(evidence, relation);
    }

    private boolean route(AssistantIntent intent, String term, List<RetrievedEvidence> evidence, String client) {
        return switch (intent) {
            case WORD_MEANING, COMPARISON, GENERAL_LINGUISTIC -> dictionary(term, evidence);
            case ROOT -> {
                root(term, evidence);
                morphology(term, evidence, client);
                yield dictionary(term, evidence);
            }
            case MORPHOLOGY -> {
                morphology(term, evidence, client);
                yield dictionary(term, evidence);
            }
            case GRAMMAR -> {
                grammar(term, evidence);
                yield false;
            }
            case SPELLING -> {
                spelling(term, evidence);
                yield dictionary(term, evidence);
            }
            case RHETORIC, LITERATURE -> false;
        };
    }

    private boolean dictionary(String term, List<RetrievedEvidence> evidence) {
        boolean relation = false;
        String normalized = normalize(term);
        int added = 0;
        for (PublishedLemma lemma : dictionary.lemmas(normalized)) {
            if (added == 4) {
                break;
            }
            added++;
            evidence.add(item("DICTIONARY_ENTRY", lemma.id().toString(), lemma.lemmaOriginal(), senseExcerpt(lemma), "/word/" + lemma.slug(), null, "EXACT", "EXACT_DICTIONARY", stampVersion(), EvidenceScores.EXACT));
            for (PublishedRelation link : dictionary.publishedRelations(lemma.id())) {
                if (!"SYNONYM".equals(link.relationType()) && !"ANTONYM".equals(link.relationType())) {
                    continue;
                }
                relation = true;
                evidence.add(item("DICTIONARY_ENTRY", lemma.id() + ":" + link.otherSlug(), link.otherLemma(), link.relationType(), "/word/" + link.otherSlug(), null, "RELATION", "EXACT_DICTIONARY", stampVersion(), EvidenceScores.EXACT));
            }
        }
        return relation;
    }

    private void root(String term, List<RetrievedEvidence> evidence) {
        dictionary.root(normalize(term)).ifPresent(root -> addRoot(evidence, root));
        for (PublishedLemma lemma : dictionary.lemmas(normalize(term))) {
            if (lemma.rootId() != null && lemma.rootNormalized() != null) {
                dictionary.root(lemma.rootNormalized()).ifPresent(root -> addRoot(evidence, root));
            }
        }
    }

    private void addRoot(List<RetrievedEvidence> evidence, PublishedRoot root) {
        evidence.add(item("ROOT", root.id().toString(), root.original(), "جذر موثّق", "/root/" + root.slug(), null, "EXACT", "EXACT_DICTIONARY", stampVersion(), EvidenceScores.EXACT));
    }

    private void morphology(String term, List<RetrievedEvidence> evidence, String client) {
        try {
            AnalysisReport report = morphology.analyze(term, "ai:" + (client == null ? "anonymous" : client));
            int added = 0;
            for (AnalysisCandidate candidate : report.analyses()) {
                if (added == 3) {
                    break;
                }
                added++;
                boolean rule = candidate.provenance() == AnalysisProvenance.RULE_DERIVED;
                int score = rule ? EvidenceScores.RULE : candidate.provenance() == AnalysisProvenance.MANUAL_VERIFIED ? EvidenceScores.VERIFIED : EvidenceScores.EXACT;
                String excerpt = rule ? "تحليل صرفي محتمل. الجذر: " + safe(candidate.root()) : "جذر موثّق: " + safe(candidate.root());
                String id = candidate.lexicalEntryId() == null ? term : candidate.lexicalEntryId().toString();
                evidence.add(item("MORPHOLOGY", id + ":" + added, safe(candidate.lemma()), excerpt, "/tools/morphology?word=" + term, null, candidate.provenance().name(), candidate.provenance().name(), report.ruleSetVersion(), score));
            }
        } catch (RuntimeException exception) {
            log.warn("Morphology retrieval skipped");
        }
    }

    private void grammar(String term, List<RetrievedEvidence> evidence) {
        try {
            for (SearchHit hit : grammar.search(term, 0, 4).items()) {
                String url = switch (hit.kind()) {
                    case "RULE" -> "/grammar/rules/" + hit.slug();
                    case "CONCEPT" -> "/grammar/concepts/" + hit.slug();
                    default -> "/grammar/" + hit.slug();
                };
                String type = switch (hit.kind()) {
                    case "RULE" -> "GRAMMAR_RULE";
                    case "CONCEPT" -> "GRAMMAR_CONCEPT";
                    default -> "GRAMMAR_TOPIC";
                };
                evidence.add(item(type, hit.slug(), hit.title(), hit.summary(), url, null, "DOMAIN", "PUBLISHED_REFERENCE", "published", EvidenceScores.DOMAIN));
            }
        } catch (RuntimeException exception) {
            log.warn("Grammar retrieval skipped");
        }
    }

    private void spelling(String term, List<RetrievedEvidence> evidence) {
        try {
            for (SpellingMatch match : spelling.matches(normalize(term))) {
                String excerpt = match.explanation() == null || match.explanation().isBlank() ? match.correctForm() : match.explanation();
                evidence.add(item("SPELLING_RULE", match.ruleSlug(), match.ruleTitle(), excerpt, "/spelling/rules/" + match.ruleSlug(), null, "DOMAIN", "PUBLISHED_REFERENCE", "published", EvidenceScores.DOMAIN));
            }
        } catch (RuntimeException exception) {
            log.warn("Spelling retrieval skipped");
        }
    }

    private void search(String term, List<RetrievedEvidence> evidence, Set<SearchEntityType> types) {
        if (term.codePointCount(0, term.length()) < 2) {
            return;
        }
        try {
            String key = ArabicSearchNormalizer.key(term);
            List<SearchCandidate> candidates = new ArrayList<>(search.collect(term, key, ArabicSearchNormalizer.folded(term)));
            if (key.codePointCount(0, key.length()) >= SearchTuning.FUZZY_MIN_CODE_POINTS) {
                candidates.addAll(search.fuzzy(key, SearchTuning.FUZZY_THRESHOLD, 3));
            }
            int added = 0;
            for (SearchCandidate candidate : candidates) {
                if (added == 8) {
                    break;
                }
                if (types != null && !types.contains(candidate.type())) {
                    continue;
                }
                boolean fuzzy = candidate.reason() == MatchReason.FUZZY || candidate.fuzzyBody();
                int score = fuzzy ? EvidenceScores.FUZZY : searchScore(candidate.reason());
                evidence.add(item(candidate.type().name(), candidate.entityId().toString(), candidate.titleOriginal(), candidate.snippet(), candidate.urlPath(), candidate.subtitle(), candidate.reason().name(), fuzzy ? "SEARCH_SUGGESTION" : "PUBLISHED_REFERENCE", "published", score));
                added++;
            }
        } catch (RuntimeException exception) {
            log.warn("Search retrieval skipped");
        }
    }

    private static int searchScore(MatchReason reason) {
        return switch (reason) {
            case EXACT, NORMALIZED_EXACT -> EvidenceScores.EXACT;
            case WORD_FORM, ALIAS -> EvidenceScores.VERIFIED;
            case ROOT, MORPHOLOGY -> EvidenceScores.DOMAIN;
            case TITLE_PREFIX, DEFINITION -> EvidenceScores.SEARCH;
            case FUZZY -> EvidenceScores.FUZZY;
        };
    }

    private static Set<SearchEntityType> typesFor(AssistantIntent intent) {
        return switch (intent) {
            case WORD_MEANING, COMPARISON -> EnumSet.of(SearchEntityType.DICTIONARY_ENTRY, SearchEntityType.ARTICLE);
            case ROOT, MORPHOLOGY -> EnumSet.of(SearchEntityType.ROOT, SearchEntityType.DICTIONARY_ENTRY);
            case GRAMMAR -> EnumSet.of(SearchEntityType.GRAMMAR_TOPIC, SearchEntityType.GRAMMAR_RULE, SearchEntityType.GRAMMAR_CONCEPT);
            case SPELLING -> EnumSet.of(SearchEntityType.SPELLING_TOPIC, SearchEntityType.SPELLING_RULE);
            case RHETORIC -> EnumSet.of(SearchEntityType.RHETORIC_TOPIC, SearchEntityType.RHETORIC_DEVICE);
            case LITERATURE -> EnumSet.of(SearchEntityType.LITERARY_ERA, SearchEntityType.LITERARY_FIGURE, SearchEntityType.LITERARY_WORK);
            case GENERAL_LINGUISTIC -> null;
        };
    }

    private String senseExcerpt(PublishedLemma lemma) {
        List<PublishedSense> senses = dictionary.publishedSenses(lemma.id());
        if (senses.isEmpty()) {
            return lemma.lemmaOriginal();
        }
        PublishedSense sense = senses.get(0);
        String definition = sense.shortDefinition() == null || sense.shortDefinition().isBlank() ? sense.definition() : sense.shortDefinition();
        return definition == null ? lemma.lemmaOriginal() : definition;
    }

    private String stampVersion() {
        return dictionary.contentStamp();
    }

    private static RetrievedEvidence item(String type, String id, String title, String excerpt, String url, String source, String reason, String provenance, String version, int score) {
        return new RetrievedEvidence(type, id, title == null ? "" : title, excerpt == null ? "" : excerpt, url, source, reason, provenance, version == null ? "published" : version, score);
    }

    private static String normalize(String value) {
        return NORMALIZER.normalize(value).normalizedText();
    }

    private static String shorten(String value) {
        String[] parts = value.split(" ");
        return parts.length == 0 ? value : parts[0];
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
