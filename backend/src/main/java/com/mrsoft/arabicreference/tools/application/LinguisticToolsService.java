package com.mrsoft.arabicreference.tools.application;

import com.mrsoft.arabicreference.content.application.ArticleQueryService;
import com.mrsoft.arabicreference.content.domain.KnowledgeTargetType;
import com.mrsoft.arabicreference.dictionary.application.DictionaryEntryService;
import com.mrsoft.arabicreference.dictionary.application.DictionaryViews.PublicEntry;
import com.mrsoft.arabicreference.dictionary.application.DictionaryViews.PublicForm;
import com.mrsoft.arabicreference.dictionary.application.DictionaryViews.PublicSense;
import com.mrsoft.arabicreference.dictionary.application.PublishedDictionaryQuery;
import com.mrsoft.arabicreference.dictionary.application.PublishedDictionaryQuery.PublishedLemma;
import com.mrsoft.arabicreference.dictionary.application.PublishedDictionaryQuery.PublishedRelation;
import com.mrsoft.arabicreference.dictionary.application.PublishedDictionaryQuery.PublishedRoot;
import com.mrsoft.arabicreference.dictionary.application.PublishedDictionaryQuery.PublishedSense;
import com.mrsoft.arabicreference.grammar.application.GrammarQueryService;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.PublicConcept;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.PublicRule;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.PublicTopic;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.SearchHit;
import com.mrsoft.arabicreference.morphology.application.MorphologyService;
import com.mrsoft.arabicreference.morphology.application.MorphologyViews.PatternView;
import com.mrsoft.arabicreference.morphology.application.MorphologyViews.Reading;
import com.mrsoft.arabicreference.morphology.application.MorphologyViews.RecordedPattern;
import com.mrsoft.arabicreference.morphology.domain.AnalysisCandidate;
import com.mrsoft.arabicreference.morphology.domain.AnalysisProvenance;
import com.mrsoft.arabicreference.morphology.domain.AnalysisReport;
import com.mrsoft.arabicreference.search.domain.LinguisticSearchPort;
import com.mrsoft.arabicreference.search.domain.SearchCandidate;
import com.mrsoft.arabicreference.search.domain.SearchGeneration;
import com.mrsoft.arabicreference.search.domain.SearchIndex;
import com.mrsoft.arabicreference.search.domain.SearchTuning;
import com.mrsoft.arabicreference.shared.kernel.exception.ResourceNotFoundException;
import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import com.mrsoft.arabicreference.spelling.application.SpellingQueryService;
import com.mrsoft.arabicreference.spelling.application.SpellingViews.SpellingMatch;
import com.mrsoft.arabicreference.tools.application.ToolTexts.Prepared;
import com.mrsoft.arabicreference.tools.application.ToolViews.AdminTools;
import com.mrsoft.arabicreference.tools.application.ToolViews.Comparison;
import com.mrsoft.arabicreference.tools.application.ToolViews.DerivationGroup;
import com.mrsoft.arabicreference.tools.application.ToolViews.DerivationResult;
import com.mrsoft.arabicreference.tools.application.ToolViews.EntryLink;
import com.mrsoft.arabicreference.tools.application.ToolViews.GrammarItem;
import com.mrsoft.arabicreference.tools.application.ToolViews.GrammarResult;
import com.mrsoft.arabicreference.tools.application.ToolViews.GraphEdge;
import com.mrsoft.arabicreference.tools.application.ToolViews.GraphNode;
import com.mrsoft.arabicreference.tools.application.ToolViews.GraphResult;
import com.mrsoft.arabicreference.tools.application.ToolViews.MorphLine;
import com.mrsoft.arabicreference.tools.application.ToolViews.PatternCard;
import com.mrsoft.arabicreference.tools.application.ToolViews.PatternResult;
import com.mrsoft.arabicreference.tools.application.ToolViews.ProvenanceNote;
import com.mrsoft.arabicreference.tools.application.ToolViews.RelationResult;
import com.mrsoft.arabicreference.tools.application.ToolViews.RootResult;
import com.mrsoft.arabicreference.tools.application.ToolViews.SenseView;
import com.mrsoft.arabicreference.tools.application.ToolViews.SourceLink;
import com.mrsoft.arabicreference.tools.application.ToolViews.SpellingCheck;
import com.mrsoft.arabicreference.tools.application.ToolViews.SpellingEvidence;
import com.mrsoft.arabicreference.tools.application.ToolViews.Suggestion;
import com.mrsoft.arabicreference.tools.application.ToolViews.ToolCatalogView;
import com.mrsoft.arabicreference.tools.application.ToolViews.ToolEnvelope;
import com.mrsoft.arabicreference.tools.application.ToolViews.WordAnalysis;
import com.mrsoft.arabicreference.tools.application.ToolViews.WordCard;
import com.mrsoft.arabicreference.tools.domain.ProvenanceKind;
import com.mrsoft.arabicreference.tools.domain.ToolCatalog;
import com.mrsoft.arabicreference.tools.domain.ToolCode;
import com.mrsoft.arabicreference.tools.infrastructure.ToolRateLimiter;
import com.mrsoft.arabicreference.tools.infrastructure.ToolResultCache;
import com.mrsoft.arabicreference.tools.infrastructure.ToolsProperties;
import io.micrometer.core.instrument.Timer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class LinguisticToolsService {

    private final PublishedDictionaryQuery dictionary;
    private final DictionaryEntryService entries;
    private final MorphologyService morphology;
    private final GrammarQueryService grammar;
    private final SpellingQueryService spelling;
    private final ArticleQueryService articles;
    private final LinguisticSearchPort search;
    private final SearchGeneration searchGeneration;
    private final SearchIndex searchIndex;
    private final ToolResultCache cache;
    private final ToolRateLimiter rateLimiter;
    private final ToolMetrics metrics;
    private final ToolsProperties properties;

    public LinguisticToolsService(
            PublishedDictionaryQuery dictionary,
            DictionaryEntryService entries,
            MorphologyService morphology,
            GrammarQueryService grammar,
            SpellingQueryService spelling,
            ArticleQueryService articles,
            LinguisticSearchPort search,
            SearchGeneration searchGeneration,
            SearchIndex searchIndex,
            ToolResultCache cache,
            ToolRateLimiter rateLimiter,
            ToolMetrics metrics,
            ToolsProperties properties) {
        this.dictionary = dictionary;
        this.entries = entries;
        this.morphology = morphology;
        this.grammar = grammar;
        this.spelling = spelling;
        this.articles = articles;
        this.search = search;
        this.searchGeneration = searchGeneration;
        this.searchIndex = searchIndex;
        this.cache = cache;
        this.rateLimiter = rateLimiter;
        this.metrics = metrics;
        this.properties = properties;
    }

    public List<ToolCatalogView> catalog() {
        return ToolCatalog.ALL.stream()
                .map(item -> new ToolCatalogView(item.code().name(), item.name(), item.description(), item.route(), item.status(), item.inputKind()))
                .toList();
    }

    public AdminTools adminStatus() {
        return new AdminTools(catalog(), metrics.snapshot());
    }

    public ToolEnvelope root(String raw, String client) {
        return run(ToolCode.ROOT, raw, client, true, () -> rootResult(ToolTexts.word(ToolCode.ROOT, raw), client));
    }

    public ToolEnvelope derivations(String raw, String client) {
        return run(ToolCode.DERIVATIONS, raw, client, true, () -> derivationResult(ToolTexts.word(ToolCode.DERIVATIONS, raw)));
    }

    public ToolEnvelope patterns(String raw, String client) {
        Prepared prepared = raw == null || raw.isBlank() ? new Prepared("", "") : ToolTexts.word(ToolCode.PATTERNS, raw);
        return run(ToolCode.PATTERNS, prepared.display(), client, false, () -> patternResult(prepared, client));
    }

    public ToolEnvelope word(String raw, String client) {
        return run(ToolCode.WORD_ANALYSIS, raw, client, true, () -> wordResult(ToolTexts.word(ToolCode.WORD_ANALYSIS, raw)));
    }

    public ToolEnvelope compare(String left, String right, String client) {
        Prepared first = ToolTexts.word(ToolCode.COMPARE, left);
        Prepared second = ToolTexts.word(ToolCode.COMPARE, right);
        String key = first.normalized() + "\n" + second.normalized();
        return runKeyed(ToolCode.COMPARE, first.display() + " / " + second.display(), key, client, () -> compareResult(first, second));
    }

    public ToolEnvelope relations(String raw, String client) {
        return run(ToolCode.RELATIONS, raw, client, true, () -> relationResult(ToolTexts.word(ToolCode.RELATIONS, raw)));
    }

    public ToolEnvelope spelling(String raw, String client) {
        return run(ToolCode.SPELLING_CHECK, raw, client, true, () -> spellingResult(ToolTexts.phrase(ToolCode.SPELLING_CHECK, raw)));
    }

    public ToolEnvelope grammar(String raw, String client) {
        return run(ToolCode.GRAMMAR, raw, client, true, () -> grammarResult(ToolTexts.phrase(ToolCode.GRAMMAR, raw)));
    }

    public ToolEnvelope explore(String raw, String client) {
        return run(ToolCode.EXPLORE, raw, client, true, () -> graphResult(ToolTexts.word(ToolCode.EXPLORE, raw)));
    }

    private ToolEnvelope run(ToolCode code, String raw, String client, boolean requireText, Work work) {
        rateLimiter.acquire(client == null ? "public" : client);
        Timer.Sample sample = metrics.start();
        try {
            Prepared prepared;
            if (code == ToolCode.PATTERNS && (raw == null || raw.isBlank())) {
                prepared = new Prepared("", "");
            } else if (code == ToolCode.SPELLING_CHECK || code == ToolCode.GRAMMAR) {
                prepared = ToolTexts.phrase(code, raw);
            } else if (requireText) {
                prepared = ToolTexts.word(code, raw);
            } else {
                prepared = ToolTexts.word(code, raw);
            }
            return finish(code, prepared.display(), prepared.normalized(), client, sample, work);
        } catch (RuntimeException exception) {
            metrics.failure(code.name(), sample);
            throw exception;
        }
    }

    private ToolEnvelope finish(ToolCode code, String display, String normalized, String client, Timer.Sample sample, Work work) {
        String key = ToolCacheKey.of(code.name(), normalized, dictionary.contentStamp(), morphology.ruleGeneration(), searchGeneration.current(), searchIndex.state().indexVersion());
        ToolEnvelope cached = cache.read("tools:" + key);
        if (cached != null) {
            metrics.success(code.name(), "EMPTY".equals(cached.status()), sample);
            return cached;
        }
        Built built = work.build();
        ToolEnvelope envelope = new ToolEnvelope(display, normalized, code.name(), built.empty ? "EMPTY" : "OK", built.result, built.provenance, built.limitations);
        cache.write("tools:" + key, envelope);
        metrics.success(code.name(), built.empty, sample);
        return envelope;
    }

    private ToolEnvelope runKeyed(ToolCode code, String display, String normalized, String client, Work work) {
        rateLimiter.acquire(client == null ? "public" : client);
        Timer.Sample sample = metrics.start();
        try {
            return finish(code, display, normalized, client, sample, work);
        } catch (RuntimeException exception) {
            metrics.failure(code.name(), sample);
            throw exception;
        }
    }

    private Built rootResult(Prepared prepared, String client) {
        PublishedRoot exact = dictionary.root(prepared.normalized()).orElse(null);
        List<PublishedLemma> lemmas = dictionary.lemmas(prepared.normalized());
        if (exact != null) {
            return confirmedRoot(exact, "جذر موثّق", ProvenanceKind.EXACT_DICTIONARY, List.of());
        }
        PublishedRoot fromEntry = lemmas.stream().map(PublishedLemma::rootNormalized).filter(value -> value != null && !value.isBlank()).findFirst()
                .flatMap(dictionary::root).orElse(null);
        if (fromEntry != null) {
            return confirmedRoot(fromEntry, "جذر موثّق", ProvenanceKind.EXACT_DICTIONARY, List.of());
        }
        AnalysisReport report = morphology.analyze(prepared.display(), client);
        AnalysisCandidate possible = report.analyses().stream().filter(item -> item.root() != null && !item.root().isBlank()).findFirst().orElse(null);
        if (possible != null && possible.provenance() != AnalysisProvenance.RULE_DERIVED) {
            PublishedRoot recorded = possible.rootId() == null ? dictionary.root(ToolTexts.normalize(possible.root())).orElse(null) : null;
            if (recorded != null) {
                ProvenanceKind kind = possible.provenance() == AnalysisProvenance.MANUAL_VERIFIED ? ProvenanceKind.MANUAL_VERIFIED : ProvenanceKind.EXACT_DICTIONARY;
                return confirmedRoot(recorded, "جذر موثّق", kind, List.of());
            }
        }
        if (possible != null) {
            List<String> limitations = new ArrayList<>();
            limitations.add("الجذر المعروض محتمل لأنه مستنتج بقاعدة، وليس حقيقة معجمية وحيدة.");
            if (report.analyses().isEmpty()) {
                limitations.add("التحليل الصرفي لهذه الكلمة غير مدعوم بالكامل.");
            }
            RootResult result = new RootResult("جذر محتمل", possible.root(), null, null, List.of(), List.of());
            return new Built(result, List.of(note(ProvenanceKind.RULE_DERIVED)), limitations, false);
        }
        return new Built(new RootResult("لا يوجد جذر موثّق", null, null, null, List.of(), List.of()), List.of(note(ProvenanceKind.PUBLISHED_REFERENCE)), List.of("لا يوجد جذر موثّق لهذه الكلمة في البيانات المنشورة."), true);
    }

    private Built confirmedRoot(PublishedRoot root, String heading, ProvenanceKind kind, List<String> extra) {
        List<EntryLink> linked = dictionary.entriesForRoot(root.id()).stream().limit(properties.getCandidateCap()).map(this::link).toList();
        List<EntryLink> patterns = new ArrayList<>();
        try {
            for (RecordedPattern pattern : morphology.rootMorphology(root.slug()).patterns()) {
                if (patterns.size() >= properties.getCandidateCap()) {
                    break;
                }
                patterns.add(new EntryLink(pattern.lemma(), pattern.entrySlug(), ArabicLabels.pattern(pattern.code()), "/word/" + pattern.entrySlug(), pattern.original()));
            }
        } catch (ResourceNotFoundException exception) {
            patterns = List.of();
        }
        return new Built(new RootResult(heading, root.original(), root.slug(), "/root/" + root.slug(), linked, patterns), List.of(note(kind)), extra, false);
    }

    private Built derivationResult(Prepared prepared) {
        PublishedRoot root = dictionary.root(prepared.normalized()).orElse(null);
        List<PublishedLemma> lemmas = dictionary.lemmas(prepared.normalized());
        if (root == null) {
            String rootKey = lemmas.stream().map(PublishedLemma::rootNormalized).filter(value -> value != null && !value.isBlank()).findFirst().orElse(null);
            root = rootKey == null ? null : dictionary.root(rootKey).orElse(null);
        }
        if (root == null) {
            return new Built(new DerivationResult(null, null, List.of(), List.of()), List.of(note(ProvenanceKind.PUBLISHED_REFERENCE)), List.of("لا توجد مشتقات منشورة لهذه الكلمة."), true);
        }
        Map<String, List<EntryLink>> groups = new LinkedHashMap<>();
        for (PublishedLemma lemma : dictionary.entriesForRoot(root.id())) {
            if (groups.values().stream().mapToInt(List::size).sum() >= 40) {
                break;
            }
            groups.computeIfAbsent(ArabicLabels.speech(lemma.partOfSpeech()), key -> new ArrayList<>()).add(link(lemma));
        }
        List<DerivationGroup> grouped = groups.entrySet().stream().map(entry -> new DerivationGroup(entry.getKey(), entry.getValue())).toList();
        List<EntryLink> recorded = new ArrayList<>();
        try {
            for (RecordedPattern pattern : morphology.rootMorphology(root.slug()).patterns()) {
                if (recorded.size() >= properties.getCandidateCap()) {
                    break;
                }
                recorded.add(new EntryLink(pattern.original(), pattern.entrySlug(), pattern.lemma(), "/word/" + pattern.entrySlug(), "تحليل منشور"));
            }
        } catch (ResourceNotFoundException ignored) {
            recorded = List.of();
        }
        boolean empty = grouped.isEmpty() && recorded.isEmpty();
        return new Built(new DerivationResult(root.original(), "/root/" + root.slug(), grouped, recorded), List.of(note(ProvenanceKind.PUBLISHED_REFERENCE)), empty ? List.of("لا توجد مشتقات منشورة تحت هذا الجذر.") : List.of(), empty);
    }

    private Built patternResult(Prepared prepared, String client) {
        List<PatternView> active = morphology.activePatterns();
        if (prepared.normalized().isBlank()) {
            List<PatternCard> cards = active.stream().limit(40).map(pattern -> card(pattern, List.of())).toList();
            return new Built(new PatternResult(cards), List.of(note(ProvenanceKind.PUBLISHED_REFERENCE)), List.of("اختر وزنًا أو كلمة لعرض الأمثلة المنشورة."), cards.isEmpty());
        }
        List<PatternView> matched = active.stream()
                .filter(pattern -> prepared.normalized().equals(pattern.patternNormalized()) || prepared.display().equals(pattern.patternOriginal()))
                .limit(properties.getCandidateCap())
                .toList();
        List<String> limitations = new ArrayList<>();
        ProvenanceKind kind = ProvenanceKind.PUBLISHED_REFERENCE;
        if (matched.isEmpty()) {
            AnalysisReport report = morphology.analyze(prepared.display(), client);
            LinkedHashSet<String> codes = new LinkedHashSet<>();
            boolean ruled = false;
            for (AnalysisCandidate candidate : report.analyses()) {
                if (candidate.patternCode() != null) {
                    codes.add(candidate.patternCode());
                    if (candidate.provenance() == AnalysisProvenance.RULE_DERIVED) {
                        ruled = true;
                    }
                }
            }
            matched = active.stream().filter(pattern -> codes.contains(pattern.code())).limit(properties.getCandidateCap()).toList();
            if (ruled) {
                kind = ProvenanceKind.RULE_DERIVED;
                limitations.add("الوزن المعروض محتمل لأنه مستنتج بقاعدة، ولا يعني أن كل كلمة على هذا الوزن موثّقة.");
            }
            if (matched.isEmpty()) {
                limitations.add("لا يوجد وزن منشور مطابق لهذه الكلمة في التغطية الحالية.");
            }
        }
        if (matched.size() > 1) {
            limitations.add("أكثر من وزن يطابق هذا الشكل بعد حذف الحركات.");
        }
        List<PatternCard> cards = new ArrayList<>();
        for (PatternView pattern : matched) {
            List<EntryLink> examples = morphology.examplesForPattern(pattern.id(), properties.getCandidateCap()).stream()
                    .map(example -> new EntryLink(example.lemma(), example.entrySlug(), ArabicLabels.pattern(example.code()), "/word/" + example.entrySlug(), example.original()))
                    .toList();
            cards.add(card(pattern, examples));
        }
        return new Built(new PatternResult(cards), List.of(note(kind)), limitations, cards.isEmpty());
    }

    private PatternCard card(PatternView pattern, List<EntryLink> examples) {
        return new PatternCard(pattern.code(), pattern.patternOriginal(), ArabicLabels.pattern(pattern.category().name()), pattern.radicalCount(), pattern.description(), examples, examples.isEmpty() ? "لا توجد أمثلة منشورة لهذا الوزن." : "الأمثلة من تحليلات منشورة فقط.");
    }

    private Built wordResult(Prepared prepared) {
        List<WordCard> cards = cards(prepared);
        String notice = cards.size() > 1 ? "وجدنا أكثر من مدخل للكلمة" : null;
        List<String> limitations = new ArrayList<>();
        if (cards.isEmpty()) {
            limitations.add(ToolTexts.MISSING);
        }
        return new Built(new WordAnalysis(notice, cards), List.of(note(ProvenanceKind.PUBLISHED_REFERENCE)), limitations, cards.isEmpty());
    }

    private Built relationResult(Prepared prepared) {
        List<WordCard> cards = cards(prepared);
        if (cards.isEmpty()) {
            return new Built(new RelationResult(null, List.of(), List.of()), List.of(note(ProvenanceKind.PUBLISHED_REFERENCE)), List.of(ToolTexts.MISSING), true);
        }
        String notice = cards.size() > 1 ? "وجدنا أكثر من مدخل للكلمة" : null;
        List<SenseView> senses = new ArrayList<>();
        List<EntryLink> entryLevel = new ArrayList<>();
        for (WordCard card : cards) {
            senses.addAll(card.senses());
        }
        return new Built(new RelationResult(notice, senses, entryLevel), List.of(note(ProvenanceKind.EXACT_DICTIONARY)), List.of(), senses.isEmpty());
    }

    private Built compareResult(Prepared left, Prepared right) {
        boolean same = left.normalized().equals(right.normalized());
        List<WordCard> leftCards = cards(left);
        List<WordCard> rightCards = same ? leftCards : cards(right);
        WordCard leftCard = leftCards.isEmpty() ? null : leftCards.get(0);
        WordCard rightCard = rightCards.isEmpty() ? null : rightCards.get(0);
        boolean missing = leftCard == null || rightCard == null;
        String difference = semantic(leftCard, rightCard, same);
        List<String> limitations = new ArrayList<>();
        if (leftCards.size() > 1 || rightCards.size() > 1) {
            limitations.add("وجدنا أكثر من مدخل للكلمة. المقارنة تعرض المدخل الأول لكل طرف.");
        }
        if (missing) {
            limitations.add(ToolTexts.MISSING);
        }
        if (ToolTexts.INSUFFICIENT.equals(difference)) {
            limitations.add(ToolTexts.INSUFFICIENT);
        }
        return new Built(new Comparison(same, missing, leftCard, rightCard, difference), List.of(note(ProvenanceKind.PUBLISHED_REFERENCE)), limitations, leftCard == null && rightCard == null);
    }

    private String semantic(WordCard left, WordCard right, boolean same) {
        if (same && left != null) {
            return "الكلمتان متطابقتان في الشكل المطبّع.";
        }
        if (left == null || right == null) {
            return ToolTexts.INSUFFICIENT;
        }
        for (SenseView sense : left.senses()) {
            if (sense.antonyms().stream().anyMatch(link -> right.slug().equals(link.slug()))) {
                return "تربطهما علاقة ضد موثّقة في المعجم.";
            }
            if (sense.synonyms().stream().anyMatch(link -> right.slug().equals(link.slug()))) {
                return "تربطهما علاقة مرادف موثّقة في المعجم.";
            }
        }
        return ToolTexts.INSUFFICIENT;
    }

    private Built spellingResult(Prepared prepared) {
        List<PublishedLemma> lemmas = new ArrayList<>(dictionary.lemmas(prepared.normalized()));
        lemmas.addAll(dictionary.entriesForForm(prepared.normalized()));
        List<SpellingMatch> matches = spelling.matches(prepared.normalized());
        List<SpellingEvidence> evidence = new ArrayList<>();
        boolean mistake = false;
        boolean documented = false;
        for (SpellingMatch match : matches) {
            boolean common = "COMMON_MISTAKE".equals(match.kind()) && prepared.normalized().equals(ToolTexts.normalize(match.commonForm()));
            boolean incorrect = "CONTRAST".equals(match.kind()) && prepared.normalized().equals(ToolTexts.normalize(match.incorrectForm()));
            boolean correct = prepared.normalized().equals(ToolTexts.normalize(match.correctForm()));
            if (common || incorrect) {
                mistake = true;
                documented = true;
                evidence.add(new SpellingEvidence(common ? "خطأ شائع مسجّل" : "مقابلة موثّقة", match.correctForm(), match.reason() == null ? match.explanation() : match.reason(), match.contextNote(), "/spelling/rules/" + match.ruleSlug()));
            } else if (correct) {
                documented = true;
                evidence.add(new SpellingEvidence("صيغة موثّقة", match.correctForm(), match.explanation(), match.contextNote(), "/spelling/rules/" + match.ruleSlug()));
            }
        }
        for (PublishedLemma lemma : lemmas) {
            if (evidence.size() >= properties.getCandidateCap()) {
                break;
            }
            evidence.add(new SpellingEvidence("موجودة في المعجم", lemma.lemmaOriginal(), null, null, "/word/" + lemma.slug()));
        }
        String verdict;
        if (mistake) {
            verdict = "وردت كخطأ شائع";
        } else if (!lemmas.isEmpty()) {
            verdict = "موجودة في المعجم";
        } else if (documented) {
            verdict = "صيغة موثّقة";
        } else {
            verdict = ToolTexts.UNKNOWN_SPELLING;
        }
        List<Suggestion> suggestions = verdict.equals(ToolTexts.UNKNOWN_SPELLING) ? suggestions(prepared.normalized()) : List.of();
        List<String> limitations = new ArrayList<>();
        limitations.add("هذه مطابقة لبيانات منشورة، وليست تصحيحًا إملائيًا آليًا.");
        if (!suggestions.isEmpty()) {
            limitations.add("الاقتراحات نتائج قريبة وليست تصحيحًا مؤكدًا.");
        }
        boolean empty = evidence.isEmpty() && suggestions.isEmpty();
        List<ProvenanceNote> notes = new ArrayList<>();
        if (!lemmas.isEmpty()) {
            notes.add(note(ProvenanceKind.EXACT_DICTIONARY));
        }
        if (documented) {
            notes.add(note(ProvenanceKind.PUBLISHED_REFERENCE));
        }
        if (!suggestions.isEmpty()) {
            notes.add(note(ProvenanceKind.SEARCH_SUGGESTION));
        }
        return new Built(new SpellingCheck(verdict, suggestions.isEmpty() ? null : "هل تقصد؟", evidence, suggestions), notes, limitations, empty);
    }

    private List<Suggestion> suggestions(String normalized) {
        if (normalized.codePointCount(0, normalized.length()) < SearchTuning.FUZZY_MIN_CODE_POINTS) {
            return List.of();
        }
        try {
            String key = com.mrsoft.arabicreference.linguistics.domain.text.ArabicSearchNormalizer.key(normalized);
            List<Suggestion> suggestions = new ArrayList<>();
            for (SearchCandidate candidate : search.fuzzy(key, SearchTuning.FUZZY_THRESHOLD, SearchTuning.FUZZY_CAP)) {
                if (suggestions.size() >= 5 || normalized.equals(candidate.titleNormalized())) {
                    continue;
                }
                suggestions.add(new Suggestion(candidate.titleOriginal(), candidate.urlPath()));
            }
            return suggestions;
        } catch (RuntimeException exception) {
            return List.of();
        }
    }

    private Built grammarResult(Prepared prepared) {
        List<String> limitations = new ArrayList<>();
        limitations.add("هذه الأداة تبحث في القواعد والمصطلحات المنشورة ولا تعرب الجمل.");
        List<SearchHit> hits;
        try {
            hits = grammar.search(prepared.display(), 0, properties.getCandidateCap()).items();
        } catch (ValidationException exception) {
            return new Built(new GrammarResult(List.of()), List.of(note(ProvenanceKind.PUBLISHED_REFERENCE)), limitations, true);
        }
        List<GrammarItem> items = new ArrayList<>();
        for (SearchHit hit : hits) {
            if (items.size() >= properties.getCandidateCap()) {
                break;
            }
            items.add(grammarItem(hit));
        }
        return new Built(new GrammarResult(items), List.of(note(ProvenanceKind.PUBLISHED_REFERENCE)), limitations, items.isEmpty());
    }

    private GrammarItem grammarItem(SearchHit hit) {
        try {
            return switch (hit.kind()) {
                case "TOPIC" -> {
                    PublicTopic topic = grammar.topic(hit.slug());
                    yield new GrammarItem("موضوع", topic.title(), "/grammar/" + topic.slug(), topic.summary(), List.of(), links(topic.prerequisites(), "/grammar/"));
                }
                case "RULE" -> {
                    PublicRule rule = grammar.rule(hit.slug());
                    List<String> examples = rule.examples().stream().limit(3).map(example -> example.textOriginal()).toList();
                    List<SourceLink> related = new ArrayList<>();
                    rule.relations().stream().limit(6).forEach(relation -> related.add(new SourceLink(relation.title(), "/grammar/rules/" + relation.slug())));
                    yield new GrammarItem("قاعدة", rule.title(), "/grammar/rules/" + rule.slug(), rule.summary(), examples, related);
                }
                default -> {
                    PublicConcept concept = grammar.concept(hit.slug());
                    yield new GrammarItem("مفهوم", concept.term(), "/grammar/concepts/" + concept.slug(), concept.shortDefinition(), concept.aliases(), links(concept.rules(), "/grammar/rules/"));
                }
            };
        } catch (ResourceNotFoundException exception) {
            return new GrammarItem(hit.kind(), hit.title(), "/grammar", hit.summary(), List.of(), List.of());
        }
    }

    private List<SourceLink> links(List<com.mrsoft.arabicreference.grammar.application.GrammarViews.Link> values, String prefix) {
        if (values == null) {
            return List.of();
        }
        return values.stream().limit(6).map(link -> new SourceLink(link.title(), prefix + link.slug())).toList();
    }

    private Built graphResult(Prepared prepared) {
        List<WordCard> cards = cards(prepared);
        Map<String, GraphNode> nodes = new LinkedHashMap<>();
        List<GraphEdge> edges = new ArrayList<>();
        int cap = properties.getGraphNodes();
        for (WordCard card : cards) {
            if (nodes.size() >= cap) {
                break;
            }
            addNode(nodes, "entry:" + card.slug(), card.lemma(), card.href(), "مدخل");
            if (card.root() != null && card.rootHref() != null) {
                addNode(nodes, "root:" + card.rootHref(), card.root(), card.rootHref(), "جذر");
                addEdge(edges, "entry:" + card.slug(), "root:" + card.rootHref(), "جذر");
            }
            for (SenseView sense : card.senses()) {
                linkRelations(nodes, edges, card.slug(), sense.synonyms(), "مرادف", cap);
                linkRelations(nodes, edges, card.slug(), sense.antonyms(), "ضد", cap);
                linkRelations(nodes, edges, card.slug(), sense.related(), "علاقة", cap);
            }
            for (SourceLink link : card.grammar()) {
                addNode(nodes, "grammar:" + link.href(), link.label(), link.href(), "نحو");
                addEdge(edges, "entry:" + card.slug(), "grammar:" + link.href(), "نحو");
            }
            for (SourceLink link : card.spelling()) {
                addNode(nodes, "spelling:" + link.href(), link.label(), link.href(), "إملاء");
                addEdge(edges, "entry:" + card.slug(), "spelling:" + link.href(), "إملاء");
            }
            for (SourceLink link : card.articles()) {
                addNode(nodes, "article:" + link.href(), link.label(), link.href(), "مقالة");
                addEdge(edges, "entry:" + card.slug(), "article:" + link.href(), "مقالة");
            }
        }
        if (properties.getGraphDepth() >= 2) {
            expandOnce(nodes, edges, cap);
        }
        List<GraphNode> list = List.copyOf(nodes.values());
        return new Built(new GraphResult(list, edges, list), List.of(note(ProvenanceKind.PUBLISHED_REFERENCE)), List.of("الرسم محدود العمق والعدد. القائمة تعرض العقد نفسها."), list.isEmpty());
    }

    private void expandOnce(Map<String, GraphNode> nodes, List<GraphEdge> edges, int cap) {
        List<String> slugs = nodes.values().stream().filter(node -> "مدخل".equals(node.kindLabel())).map(node -> node.id().substring("entry:".length())).toList();
        for (String slug : slugs) {
            if (nodes.size() >= cap) {
                return;
            }
            PublicEntry entry;
            try {
                entry = entries.publishedBySlug(slug);
            } catch (ResourceNotFoundException exception) {
                continue;
            }
            for (PublishedRelation relation : dictionary.publishedRelations(entry.id())) {
                if (nodes.size() >= cap) {
                    return;
                }
                addNode(nodes, "entry:" + relation.otherSlug(), relation.otherLemma(), "/word/" + relation.otherSlug(), "مدخل");
                addEdge(edges, "entry:" + slug, "entry:" + relation.otherSlug(), ArabicLabels.relation(relation.relationType()));
            }
        }
    }

    private void linkRelations(Map<String, GraphNode> nodes, List<GraphEdge> edges, String slug, List<EntryLink> links, String label, int cap) {
        for (EntryLink link : links) {
            if (nodes.size() >= cap) {
                return;
            }
            addNode(nodes, "entry:" + link.slug(), link.lemma(), link.href(), "مدخل");
            addEdge(edges, "entry:" + slug, "entry:" + link.slug(), label);
        }
    }

    private void addNode(Map<String, GraphNode> nodes, String id, String label, String href, String kind) {
        nodes.putIfAbsent(id, new GraphNode(id, label, href, kind));
    }

    private void addEdge(List<GraphEdge> edges, String from, String to, String label) {
        if (from.equals(to) || edges.size() >= properties.getGraphNodes()) {
            return;
        }
        for (GraphEdge edge : edges) {
            if (edge.from().equals(from) && edge.to().equals(to) && edge.relationLabel().equals(label)) {
                return;
            }
        }
        edges.add(new GraphEdge(from, to, label));
    }

    private List<WordCard> cards(Prepared prepared) {
        LinkedHashMap<UUID, PublishedLemma> found = new LinkedHashMap<>();
        for (PublishedLemma lemma : dictionary.lemmas(prepared.normalized())) {
            found.putIfAbsent(lemma.id(), lemma);
        }
        for (PublishedLemma lemma : dictionary.entriesForForm(prepared.normalized())) {
            found.putIfAbsent(lemma.id(), lemma);
        }
        List<WordCard> cards = new ArrayList<>();
        for (PublishedLemma lemma : found.values()) {
            if (cards.size() >= properties.getCandidateCap()) {
                break;
            }
            cards.add(card(lemma));
        }
        return cards;
    }

    private WordCard card(PublishedLemma lemma) {
        PublicEntry entry = entries.publishedById(lemma.id());
        List<PublishedSense> senses = dictionary.publishedSenses(lemma.id());
        List<PublishedRelation> relations = dictionary.publishedRelations(lemma.id());
        List<SenseView> senseViews = new ArrayList<>();
        if (!senses.isEmpty()) {
            for (PublishedSense sense : senses) {
                senseViews.add(new SenseView(
                        sense.definition(),
                        ArabicLabels.usage(sense.usageLabel()),
                        ArabicLabels.domain(sense.domainLabel()),
                        related(relations, sense.id(), "SYNONYM"),
                        related(relations, sense.id(), "ANTONYM"),
                        related(relations, sense.id(), "RELATED")));
            }
        } else {
            for (PublicSense sense : entry.senses()) {
                senseViews.add(new SenseView(sense.definition(), null, null, List.of(), List.of(), List.of()));
            }
        }
        List<String> forms = entry.forms().stream().map(PublicForm::originalForm).toList();
        List<MorphLine> morphLines = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        try {
            for (Reading reading : morphology.entryMorphology(lemma.id()).readings()) {
                morphLines.add(new MorphLine(reading.patternOriginal(), ArabicLabels.pattern(reading.patternCategory() == null ? null : reading.patternCategory().name()), ArabicLabels.provenance(ProvenanceKind.MANUAL_VERIFIED)));
            }
        } catch (ResourceNotFoundException exception) {
            morphLines = List.of();
        }
        if (morphLines.isEmpty()) {
            missing.add("الصرف " + ToolTexts.MISSING);
        }
        if (senseViews.isEmpty()) {
            missing.add("المعاني " + ToolTexts.MISSING);
        }
        List<SourceLink> grammarLinks = new ArrayList<>();
        try {
            for (SearchHit hit : grammar.search(entry.lemmaOriginal(), 0, 5).items()) {
                grammarLinks.add(new SourceLink(hit.title(), grammarHref(hit)));
            }
        } catch (ValidationException ignored) {
            grammarLinks = List.of();
        }
        List<SourceLink> spellingLinks = spelling.matches(lemma.lemmaNormalized()).stream()
                .limit(5)
                .map(match -> new SourceLink(match.ruleTitle(), "/spelling/rules/" + match.ruleSlug()))
                .toList();
        List<SourceLink> articleLinks = articles.relatedTo(KnowledgeTargetType.DICTIONARY_ENTRY, lemma.id()).stream()
                .map(article -> new SourceLink(article.title(), "/articles/" + article.slug()))
                .toList();
        String root = entry.root() == null ? null : entry.root().original();
        String rootHref = entry.root() == null ? null : "/root/" + entry.root().slug();
        return new WordCard(entry.lemmaOriginal(), entry.slug(), "/word/" + entry.slug(), ArabicLabels.speech(entry.partOfSpeech()), root, root == null ? ToolTexts.MISSING : "جذر موثّق", rootHref, senseViews, forms, morphLines, grammarLinks, spellingLinks, articleLinks, missing);
    }

    private List<EntryLink> related(List<PublishedRelation> relations, UUID senseId, String type) {
        List<EntryLink> links = new ArrayList<>();
        for (PublishedRelation relation : relations) {
            if (type.equals(relation.relationType()) && senseId.equals(relation.senseId()) && links.size() < properties.getCandidateCap()) {
                links.add(new EntryLink(relation.otherLemma(), relation.otherSlug(), ArabicLabels.relation(type), "/word/" + relation.otherSlug(), null));
            }
        }
        return links;
    }

    private String grammarHref(SearchHit hit) {
        return switch (hit.kind()) {
            case "TOPIC" -> "/grammar/" + hit.slug();
            case "RULE" -> "/grammar/rules/" + hit.slug();
            default -> "/grammar/concepts/" + hit.slug();
        };
    }

    private EntryLink link(PublishedLemma lemma) {
        return new EntryLink(lemma.lemmaOriginal(), lemma.slug(), ArabicLabels.speech(lemma.partOfSpeech()), "/word/" + lemma.slug(), null);
    }

    private static ProvenanceNote note(ProvenanceKind kind) {
        return new ProvenanceNote(kind.name(), ArabicLabels.provenance(kind));
    }

    @FunctionalInterface
    private interface Work {
        Built build();
    }

    private record Built(Object result, List<ProvenanceNote> provenance, List<String> limitations, boolean empty) {
    }
}
