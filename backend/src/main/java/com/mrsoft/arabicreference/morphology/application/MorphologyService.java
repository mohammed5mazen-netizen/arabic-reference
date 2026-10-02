package com.mrsoft.arabicreference.morphology.application;

import com.mrsoft.arabicreference.dictionary.application.PublishedDictionaryQuery;
import com.mrsoft.arabicreference.dictionary.application.PublishedDictionaryQuery.EditableLemma;
import com.mrsoft.arabicreference.dictionary.application.PublishedDictionaryQuery.PublishedLemma;
import com.mrsoft.arabicreference.dictionary.application.PublishedDictionaryQuery.PublishedRoot;
import com.mrsoft.arabicreference.identity.application.AuditRecorder;
import com.mrsoft.arabicreference.identity.application.AuthorizationService;
import com.mrsoft.arabicreference.identity.domain.AuditEventType;
import com.mrsoft.arabicreference.identity.domain.PermissionCatalog;
import com.mrsoft.arabicreference.linguistics.application.ArabicTextNormalizationService;
import com.mrsoft.arabicreference.linguistics.application.ContentRevisionRecorder;
import com.mrsoft.arabicreference.linguistics.domain.editorial.EditorialGuards;
import com.mrsoft.arabicreference.linguistics.domain.editorial.EditorialWorkflow;
import com.mrsoft.arabicreference.linguistics.domain.editorial.PublicationStatus;
import com.mrsoft.arabicreference.linguistics.domain.text.ArabicText;
import com.mrsoft.arabicreference.morphology.application.MorphologyViews.AnalysisDraft;
import com.mrsoft.arabicreference.morphology.application.MorphologyViews.AnalysisView;
import com.mrsoft.arabicreference.morphology.application.MorphologyViews.ConjugatedForm;
import com.mrsoft.arabicreference.morphology.application.MorphologyViews.ConjugationView;
import com.mrsoft.arabicreference.morphology.application.MorphologyViews.CoverageRow;
import com.mrsoft.arabicreference.morphology.application.MorphologyViews.CoverageView;
import com.mrsoft.arabicreference.morphology.application.MorphologyViews.EntryMorphology;
import com.mrsoft.arabicreference.morphology.application.MorphologyViews.PageResult;
import com.mrsoft.arabicreference.morphology.application.MorphologyViews.PatternDraft;
import com.mrsoft.arabicreference.morphology.application.MorphologyViews.PatternUpdate;
import com.mrsoft.arabicreference.morphology.application.MorphologyViews.PatternView;
import com.mrsoft.arabicreference.morphology.application.MorphologyViews.Reading;
import com.mrsoft.arabicreference.morphology.application.MorphologyViews.RecordedPattern;
import com.mrsoft.arabicreference.morphology.application.MorphologyViews.RootMorphology;
import com.mrsoft.arabicreference.morphology.application.MorphologyViews.RuleView;
import com.mrsoft.arabicreference.morphology.domain.AnalysisReport;
import com.mrsoft.arabicreference.morphology.domain.ArabicLetters;
import com.mrsoft.arabicreference.morphology.domain.ConjugationPlanner;
import com.mrsoft.arabicreference.morphology.domain.CoverageStatus;
import com.mrsoft.arabicreference.morphology.domain.DerivationKind;
import com.mrsoft.arabicreference.morphology.domain.FormOrigin;
import com.mrsoft.arabicreference.morphology.domain.MorphologicalFeatures;
import com.mrsoft.arabicreference.morphology.domain.MorphologyAnalyzer;
import com.mrsoft.arabicreference.morphology.domain.MorphologyAnalyzer.ManualReading;
import com.mrsoft.arabicreference.morphology.domain.PatternCategory;
import com.mrsoft.arabicreference.morphology.domain.PatternCompatibility;
import com.mrsoft.arabicreference.morphology.domain.PublishedLexicon;
import com.mrsoft.arabicreference.morphology.domain.RuleSet;
import com.mrsoft.arabicreference.morphology.domain.Segmentation;
import com.mrsoft.arabicreference.morphology.domain.StemVowel;
import com.mrsoft.arabicreference.morphology.domain.VerbClass;
import com.mrsoft.arabicreference.morphology.infrastructure.MorphologyProperties;
import com.mrsoft.arabicreference.morphology.infrastructure.MorphologyCache;
import com.mrsoft.arabicreference.morphology.infrastructure.MorphologyRateLimiter;
import com.mrsoft.arabicreference.morphology.infrastructure.persistence.MorphologicalPatternEntity;
import com.mrsoft.arabicreference.morphology.infrastructure.persistence.MorphologicalPatternRepository;
import com.mrsoft.arabicreference.morphology.infrastructure.persistence.MorphologyAnalysisCitationEntity;
import com.mrsoft.arabicreference.morphology.infrastructure.persistence.MorphologyAnalysisCitationRepository;
import com.mrsoft.arabicreference.morphology.infrastructure.persistence.MorphologyAnalysisEntity;
import com.mrsoft.arabicreference.morphology.infrastructure.persistence.MorphologyAnalysisRepository;
import com.mrsoft.arabicreference.morphology.infrastructure.persistence.MorphologyRuleEntity;
import com.mrsoft.arabicreference.morphology.infrastructure.persistence.MorphologyRuleRepository;
import com.mrsoft.arabicreference.morphology.infrastructure.persistence.MorphologyStateEntity;
import com.mrsoft.arabicreference.morphology.infrastructure.persistence.MorphologyStateRepository;
import com.mrsoft.arabicreference.shared.kernel.exception.ConflictException;
import com.mrsoft.arabicreference.shared.kernel.exception.FieldErrorDetail;
import com.mrsoft.arabicreference.shared.kernel.exception.ForbiddenOperationException;
import com.mrsoft.arabicreference.shared.kernel.exception.ResourceNotFoundException;
import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import com.mrsoft.arabicreference.shared.kernel.id.Ids;
import com.mrsoft.arabicreference.shared.kernel.security.AuthenticatedAccess;
import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import com.mrsoft.arabicreference.source.application.SourceAdminService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MorphologyService {

    private static final Logger log = LoggerFactory.getLogger(MorphologyService.class);
    private static final String TARGET = "morphology_analysis";

    private final PublishedDictionaryQuery dictionary;
    private final MorphologicalPatternRepository patterns;
    private final MorphologyAnalysisRepository analyses;
    private final MorphologyAnalysisCitationRepository citations;
    private final MorphologyRuleRepository rules;
    private final MorphologyStateRepository states;
    private final ArabicTextNormalizationService text;
    private final AuthorizationService authorization;
    private final AuditRecorder audit;
    private final ContentRevisionRecorder revisions;
    private final SourceAdminService sources;
    private final TimeProvider timeProvider;
    private final MorphologyProperties properties;
    private final MorphologyCache cache;
    private final MorphologyRateLimiter rateLimiter;
    private final MeterRegistry meters;
    private final EntityManager entityManager;

    public MorphologyService(
            PublishedDictionaryQuery dictionary,
            MorphologicalPatternRepository patterns,
            MorphologyAnalysisRepository analyses,
            MorphologyAnalysisCitationRepository citations,
            MorphologyRuleRepository rules,
            MorphologyStateRepository states,
            ArabicTextNormalizationService text,
            AuthorizationService authorization,
            AuditRecorder audit,
            ContentRevisionRecorder revisions,
            SourceAdminService sources,
            TimeProvider timeProvider,
            MorphologyProperties properties,
            MorphologyCache cache,
            MorphologyRateLimiter rateLimiter,
            MeterRegistry meters,
            EntityManager entityManager) {
        this.dictionary = dictionary;
        this.patterns = patterns;
        this.analyses = analyses;
        this.citations = citations;
        this.rules = rules;
        this.states = states;
        this.text = text;
        this.authorization = authorization;
        this.audit = audit;
        this.revisions = revisions;
        this.sources = sources;
        this.timeProvider = timeProvider;
        this.properties = properties;
        this.cache = cache;
        this.rateLimiter = rateLimiter;
        this.meters = meters;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public AnalysisReport analyze(String word, String bucket) {
        rateLimiter.acquire(bucket == null ? "public" : bucket);
        Timer.Sample sample = Timer.start(meters);
        meters.counter("morphology.analysis.requests").increment();
        try {
            String normalized = validated(word);
            String key = "morphology:analysis:" + RuleSet.VERSION + ":" + generation() + ":" + dictionary.contentStamp() + ":" + normalized;
            AnalysisReport cached = cache.read(key);
            AnalysisReport report = cached != null ? cached : analyzeUncached(word.trim(), normalized);
            if (cached == null) {
                cache.write(key, report);
            }
            if (report.analyses().isEmpty()) {
                meters.counter("morphology.analysis.empty").increment();
            }
            meters.counter("morphology.analysis.candidates").increment(report.analyses().size());
            log.info("morphology analysis completed candidates={} truncated={} durationMs={}",
                    report.analyses().size(), report.truncated(), sample.stop(meters.timer("morphology.analysis.duration")) / 1_000_000);
            return report;
        } catch (RuntimeException exception) {
            sample.stop(meters.timer("morphology.analysis.duration"));
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public EntryMorphology entryMorphology(UUID entryId) {
        PublishedLemma entry = dictionary.lemma(entryId).orElseThrow(() -> new ResourceNotFoundException("Published entry was not found."));
        List<MorphologyAnalysisEntity> published = analyses.findPublishedByEntries(List.of(entry.id()));
        return new EntryMorphology(entry.id(), readings(published), dictionary.recordedPlurals(entry.id()));
    }

    @Transactional(readOnly = true)
    public RootMorphology rootMorphology(String slug) {
        PublishedRoot root = dictionary.rootBySlug(slug).orElseThrow(() -> new ResourceNotFoundException("Published root was not found."));
        List<PublishedLemma> entries = dictionary.entriesForRoot(root.id());
        Map<UUID, PublishedLemma> byId = entries.stream().collect(Collectors.toMap(PublishedLemma::id, entry -> entry, (left, right) -> left, LinkedHashMap::new));
        List<RecordedPattern> recorded = byId.isEmpty() ? List.of() : analyses.findPublishedByEntries(byId.keySet()).stream()
                .map(analysis -> recordedPattern(analysis, byId.get(analysis.getLexicalEntryId())))
                .filter(pattern -> pattern.code() != null)
                .sorted(Comparator.comparing(RecordedPattern::code).thenComparing(RecordedPattern::lemma))
                .toList();
        return new RootMorphology(root.original(), root.slug(), recorded);
    }

    @Transactional(readOnly = true)
    public ConjugationView conjugate(UUID entryId) {
        PublishedLemma entry = dictionary.lemma(entryId).orElseThrow(() -> new ResourceNotFoundException("Published entry was not found."));
        List<MorphologyAnalysisEntity> published = analyses.findPublishedByEntries(List.of(entry.id()));
        Optional<MorphologyAnalysisEntity> chosen = published.stream()
                .filter(analysis -> snapshot(analysis).get("verbClass") != null)
                .min(Comparator.comparing(MorphologyAnalysisEntity::getId));
        if (chosen.isEmpty() || entry.rootNormalized() == null) {
            return unsupported(entry.id(), chosen.map(analysis -> text(snapshot(analysis), "verbClass")).orElse(null), "NO_RECORDED_CONJUGATION");
        }
        Map<String, Object> shot = snapshot(chosen.get());
        String patternCode = text(shot, "patternCode");
        VerbClass verbClass = enumOf(text(shot, "verbClass"), VerbClass.class);
        StemVowel vowel = enumOf(text(shot, "imperfectVowel"), StemVowel.class);
        if (verbClass == null || patternCode == null) {
            return unsupported(entry.id(), text(shot, "verbClass"), "NO_RECORDED_CONJUGATION");
        }
        ConjugationPlanner.ConjugationReport planned = ConjugationPlanner.plan(verbClass, patternCode, vowel, entry.rootNormalized());
        List<ConjugatedForm> forms = planned.forms().stream()
                .map(cell -> new ConjugatedForm(cell.surface(), cell.features(), FormOrigin.RULE_GENERATED))
                .toList();
        return new ConjugationView(entry.id(), planned.coverage(), verbClass.name(), patternCode, RuleSet.VERSION, planned.reason(), forms);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.MORPHOLOGY_VIEW + "')")
    public CoverageView coverage() {
        return new CoverageView(RuleSet.VERSION, List.of(
                new CoverageRow("SOUND_FA3ALA_PERFECT", "SUPPORTED", "ماضٍ لفعل صحيح سالم على فَعَلَ عند تسجيل الباب"),
                new CoverageRow("SOUND_FA3ALA_IMPERFECT", "PARTIAL", "مضارع وأمر فقط إذا سُجّلت حركة العين"),
                new CoverageRow("FA33ALA_AND_DERIVED_VERBS", "UNSUPPORTED", "المزيد لا يُصرَّف في هذه النسخة"),
                new CoverageRow("WEAK_VERBS", "UNSUPPORTED", "المثال والأجوف والناقص واللفيف والمهموز لا تُولَّد"),
                new CoverageRow("BROKEN_PLURAL", "UNSUPPORTED", "جمع التكسير يُعرض فقط إذا كان شكلًا معجميًا منشورًا"),
                new CoverageRow("ACTIVE_PARTICIPLE_SHAPE", "PARTIAL", "فَاعِل ومَفْعُول إذا وُجد جذر منشور وفعل منشور"),
                new CoverageRow("CLITICS", "PARTIAL", "و ف ب ل ك وال وضمائر متصلة محدودة")));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.MORPHOLOGY_VIEW + "')")
    public PageResult<PatternView> patterns(int page, int size) {
        page(page, size);
        var result = patterns.findAllByOrderByCodeAsc(PageRequest.of(page, size));
        return new PageResult<>(result.map(this::patternView).toList(), page, size, result.getTotalElements());
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.MORPHOLOGY_PATTERN_MANAGE + "')")
    public PatternView createPattern(PatternDraft draft) {
        AuthenticatedAccess actor = authorization.requireAccess();
        MorphologicalPatternEntity pattern = new MorphologicalPatternEntity();
        pattern.setId(Ids.random());
        applyPattern(pattern, draft.code(), draft.patternOriginal(), draft.category(), draft.radicalCount(), draft.description(), true);
        InstantNow(pattern, true);
        patterns.saveAndFlush(pattern);
        bump();
        audit.record(actor.userId(), AuditEventType.MORPHOLOGY_PATTERN_CREATED, "morphological_pattern", pattern.getId().toString(), Map.of("code", pattern.getCode()));
        return patternView(pattern);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.MORPHOLOGY_PATTERN_MANAGE + "')")
    public PatternView updatePattern(UUID id, PatternUpdate update) {
        AuthenticatedAccess actor = authorization.requireAccess();
        MorphologicalPatternEntity pattern = lockedPattern(id, update.version());
        applyPattern(pattern, pattern.getCode(), update.patternOriginal(), update.category(), update.radicalCount(), update.description(), false);
        pattern.setUpdatedAt(timeProvider.now());
        patterns.saveAndFlush(pattern);
        bump();
        audit.record(actor.userId(), AuditEventType.MORPHOLOGY_PATTERN_UPDATED, "morphological_pattern", pattern.getId().toString(), Map.of("code", pattern.getCode()));
        return patternView(pattern);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.MORPHOLOGY_PATTERN_MANAGE + "')")
    public PatternView setPatternStatus(UUID id, long version, boolean active) {
        AuthenticatedAccess actor = authorization.requireAccess();
        MorphologicalPatternEntity pattern = lockedPattern(id, version);
        pattern.setStatus(active ? "ACTIVE" : "INACTIVE");
        pattern.setUpdatedAt(timeProvider.now());
        patterns.saveAndFlush(pattern);
        bump();
        audit.record(actor.userId(), AuditEventType.MORPHOLOGY_PATTERN_UPDATED, "morphological_pattern", pattern.getId().toString(), Map.of("status", pattern.getStatus()));
        return patternView(pattern);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.MORPHOLOGY_VIEW + "')")
    public PageResult<AnalysisView> analyses(int page, int size) {
        page(page, size);
        var result = analyses.findAllByOrderByUpdatedAtDesc(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt")));
        Map<UUID, List<UUID>> links = citationIds(result.map(MorphologyAnalysisEntity::getId).toList());
        return new PageResult<>(result.map(analysis -> analysisView(analysis, links.getOrDefault(analysis.getId(), List.of()))).toList(), page, size, result.getTotalElements());
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.MORPHOLOGY_ANALYSIS_CREATE + "')")
    public AnalysisView createAnalysis(AnalysisDraft draft) {
        AuthenticatedAccess actor = authorization.requireAccess();
        MorphologyAnalysisEntity analysis = new MorphologyAnalysisEntity();
        analysis.setId(Ids.random());
        analysis.setStatus(PublicationStatus.DRAFT);
        applyAnalysis(analysis, draft);
        stamp(analysis, actor.userId(), true);
        analyses.saveAndFlush(analysis);
        revisions.record(TARGET, analysis.getId(), snapshot(analysis), actor.userId(), null);
        bump();
        audit.record(actor.userId(), AuditEventType.MORPHOLOGY_ANALYSIS_CREATED, TARGET, analysis.getId().toString(), Map.of("entryId", analysis.getLexicalEntryId().toString()));
        return analysisView(analysis, List.of());
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.MORPHOLOGY_ANALYSIS_EDIT + "')")
    public AnalysisView updateAnalysis(UUID id, long version, AnalysisDraft draft) {
        AuthenticatedAccess actor = authorization.requireAccess();
        MorphologyAnalysisEntity analysis = lockedAnalysis(id, version);
        if (!EditorialWorkflow.editable(analysis.getStatus())) {
            throw new ConflictException("This analysis cannot be edited while it is " + analysis.getStatus() + ".");
        }
        if (analysis.getStatus() == PublicationStatus.PUBLISHED) {
            analysis.setStatus(PublicationStatus.DRAFT);
            analysis.setReviewedBy(null);
        }
        applyAnalysis(analysis, draft);
        stamp(analysis, actor.userId(), false);
        analyses.saveAndFlush(analysis);
        revisions.record(TARGET, analysis.getId(), snapshot(analysis), actor.userId(), null);
        bump();
        audit.record(actor.userId(), AuditEventType.MORPHOLOGY_ANALYSIS_UPDATED, TARGET, analysis.getId().toString(), Map.of("status", analysis.getStatus().name()));
        return analysisView(analysis, citationIds(List.of(id)).getOrDefault(id, List.of()));
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.MORPHOLOGY_ANALYSIS_EDIT + "')")
    public AnalysisView submit(UUID id, long version) {
        return move(id, version, EditorialWorkflow::submit, AuditEventType.MORPHOLOGY_ANALYSIS_UPDATED, false);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.MORPHOLOGY_ANALYSIS_REVIEW + "')")
    public AnalysisView requestChanges(UUID id, long version, String reason) {
        if (reason == null || reason.isBlank()) {
            throw invalid("reason", "Explain what must change.");
        }
        MorphologyAnalysisEntity analysis = lockedAnalysis(id, version);
        EditorialGuards.requireDifferentPerson(analysis.getCreatedBy(), authorization.requireAccess().userId(), "The creator cannot review their own analysis.");
        analysis.setStatus(EditorialWorkflow.requestChanges(analysis.getStatus()));
        analysis.setChangeReason(reason.trim());
        return finish(analysis, AuditEventType.MORPHOLOGY_ANALYSIS_UPDATED);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.MORPHOLOGY_ANALYSIS_REVIEW + "')")
    public AnalysisView verify(UUID id, long version) {
        MorphologyAnalysisEntity analysis = lockedAnalysis(id, version);
        UUID actor = authorization.requireAccess().userId();
        EditorialGuards.requireDifferentPerson(analysis.getCreatedBy(), actor, "The creator cannot review their own analysis.");
        analysis.setStatus(EditorialWorkflow.verify(analysis.getStatus()));
        analysis.setReviewedBy(actor);
        return finish(analysis, AuditEventType.MORPHOLOGY_ANALYSIS_VERIFIED);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.MORPHOLOGY_ANALYSIS_PUBLISH + "')")
    public AnalysisView publish(UUID id, long version) {
        MorphologyAnalysisEntity analysis = lockedAnalysis(id, version);
        UUID actor = authorization.requireAccess().userId();
        EditorialGuards.requireDifferentPerson(analysis.getCreatedBy(), actor, "The creator cannot publish their own analysis.");
        EditorialGuards.requireDifferentPerson(analysis.getReviewedBy(), actor, "The reviewer cannot publish the same analysis.");
        analysis.setStatus(EditorialWorkflow.publish(analysis.getStatus()));
        analysis.setPublishedSnapshot(snapshot(analysis));
        return finish(analysis, AuditEventType.MORPHOLOGY_ANALYSIS_PUBLISHED);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.MORPHOLOGY_ANALYSIS_PUBLISH + "')")
    public AnalysisView archive(UUID id, long version) {
        MorphologyAnalysisEntity analysis = lockedAnalysis(id, version);
        analysis.setStatus(EditorialWorkflow.archive(analysis.getStatus()));
        analysis.setPublishedSnapshot(null);
        return finish(analysis, AuditEventType.MORPHOLOGY_ANALYSIS_UPDATED);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.CITATION_MANAGE + "')")
    public AnalysisView attachCitation(UUID id, long version, UUID citationId) {
        lockedAnalysis(id, version);
        if (sources.citations(List.of(citationId)).isEmpty()) {
            throw new ResourceNotFoundException("Citation was not found.");
        }
        if (!citations.existsByOwnerIdAndCitationId(id, citationId)) {
            citations.save(new MorphologyAnalysisCitationEntity(id, citationId));
        }
        bump();
        audit.record(authorization.requireAccess().userId(), AuditEventType.CITATION_ADDED, TARGET, id.toString(), Map.of("citationId", citationId.toString()));
        MorphologyAnalysisEntity analysis = analyses.findById(id).orElseThrow(this::missingAnalysis);
        return analysisView(analysis, citationIds(List.of(id)).getOrDefault(id, List.of()));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.MORPHOLOGY_RULE_VIEW + "')")
    public List<RuleView> rules() {
        return rules.findAll().stream()
                .sorted(Comparator.comparing(MorphologyRuleEntity::getCode))
                .map(this::ruleView)
                .toList();
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.MORPHOLOGY_RULE_MANAGE + "')")
    public RuleView setRuleEnabled(String code, boolean enabled, long version) {
        MorphologyRuleEntity rule = rules.lockByCode(code).orElseThrow(() -> new ResourceNotFoundException("Morphology rule was not found."));
        if (rule.getVersion() != version) {
            throw new ConflictException("The record was updated by someone else. Reload and try again.");
        }
        rule.setEnabled(enabled);
        rules.saveAndFlush(rule);
        bump();
        audit.record(authorization.requireAccess().userId(), AuditEventType.MORPHOLOGY_RULE_CHANGED, "morphology_rule", code, Map.of("enabled", Boolean.toString(enabled)));
        return ruleView(rule);
    }

    private AnalysisReport analyzeUncached(String original, String normalized) {
        Set<String> enabled = rules.findAll().stream().filter(MorphologyRuleEntity::isEnabled).map(MorphologyRuleEntity::getCode).collect(Collectors.toSet());
        PublishedLexicon lexicon = new DictionaryLexicon();
        List<PublishedLemma> hits = new ArrayList<>();
        for (String stem : stems(normalized)) {
            hits.addAll(dictionary.lemmas(stem));
        }
        List<UUID> ids = hits.stream().map(PublishedLemma::id).distinct().toList();
        List<ManualReading> manuals = ids.isEmpty() ? List.of() : analyses.findPublishedByEntries(ids).stream().map(this::manual).toList();
        int limit = Math.max(1, Math.min(properties.getMaxCandidates(), 25));
        return MorphologyAnalyzer.analyze(original, normalized, lexicon, manuals, enabled, limit);
    }

    private ManualReading manual(MorphologyAnalysisEntity analysis) {
        Map<String, Object> shot = snapshot(analysis);
        return new ManualReading(
                analysis.getLexicalEntryId(),
                text(shot, "lemma"),
                text(shot, "partOfSpeech"),
                uuid(shot.get("rootId")),
                text(shot, "root"),
                text(shot, "patternCode"),
                text(shot, "patternOriginal"),
                enumOf(text(shot, "patternCategory"), PatternCategory.class),
                enumOf(text(shot, "derivation"), DerivationKind.class),
                featuresOf(map(shot.get("features"))),
                segmentationOf(map(shot.get("segmentation"))),
                List.of("MANUAL_RECORD"));
    }

    private List<Reading> readings(List<MorphologyAnalysisEntity> published) {
        Map<UUID, List<UUID>> links = citationIds(published.stream().map(MorphologyAnalysisEntity::getId).toList());
        return published.stream().map(analysis -> {
            Map<String, Object> shot = snapshot(analysis);
            return new Reading(
                    analysis.getId(),
                    text(shot, "patternCode"),
                    text(shot, "patternOriginal"),
                    enumOf(text(shot, "patternCategory"), PatternCategory.class),
                    enumOf(text(shot, "derivation"), DerivationKind.class),
                    enumOf(text(shot, "verbClass"), VerbClass.class),
                    enumOf(text(shot, "imperfectVowel"), StemVowel.class),
                    text(shot, "notes"),
                    featuresOf(map(shot.get("features"))),
                    links.getOrDefault(analysis.getId(), List.of()));
        }).toList();
    }

    private RecordedPattern recordedPattern(MorphologyAnalysisEntity analysis, PublishedLemma entry) {
        Map<String, Object> shot = snapshot(analysis);
        return new RecordedPattern(text(shot, "patternCode"), text(shot, "patternOriginal"), entry.lemmaOriginal(), entry.slug(), text(shot, "derivation"));
    }

    private void applyAnalysis(MorphologyAnalysisEntity analysis, AnalysisDraft draft) {
        if (draft == null || draft.lexicalEntryId() == null) {
            throw invalid("lexicalEntryId", "A lexical entry is required.");
        }
        EditableLemma entry = dictionary.editableEntry(draft.lexicalEntryId()).orElseThrow(() -> new ResourceNotFoundException("Lexical entry was not found."));
        MorphologicalPatternEntity pattern = null;
        if (draft.patternId() != null) {
            pattern = patterns.findById(draft.patternId()).orElseThrow(() -> new ResourceNotFoundException("Pattern was not found."));
            if (!"ACTIVE".equals(pattern.getStatus())) {
                throw invalid("patternId", "Choose an active pattern.");
            }
            if (entry.radicalCount() != null) {
                PatternCompatibility.require(pattern.getRadicalCount(), entry.radicalCount());
            }
        }
        analysis.setLexicalEntryId(entry.id());
        analysis.setPatternId(pattern == null ? null : pattern.getId());
        analysis.setDerivation(draft.derivation());
        analysis.setVerbClass(draft.verbClass());
        analysis.setImperfectVowel(draft.imperfectVowel());
        analysis.setNotes(cleanNotes(draft.notes()));
        analysis.setFeatures(featureMap(draft.features() == null ? MorphologicalFeatures.empty() : draft.features()));
        analysis.setSegmentation(segmentationMap(Segmentation.identity(entry.lemmaNormalized())));
    }

    private Map<String, Object> snapshot(MorphologyAnalysisEntity analysis) {
        if (analysis.getPublishedSnapshot() != null && analysis.getStatus() != PublicationStatus.PUBLISHED) {
            return analysis.getPublishedSnapshot();
        }
        EditableLemma entry = dictionary.editableEntry(analysis.getLexicalEntryId()).orElse(null);
        MorphologicalPatternEntity pattern = analysis.getPatternId() == null ? null : patterns.findById(analysis.getPatternId()).orElse(null);
        Map<String, Object> shot = new LinkedHashMap<>();
        shot.put("lemma", entry == null ? null : entry.lemmaOriginal());
        shot.put("partOfSpeech", entry == null ? null : entry.partOfSpeech());
        shot.put("rootId", entry == null ? null : entry.rootId());
        shot.put("root", entry == null ? null : entry.rootOriginal());
        shot.put("patternCode", pattern == null ? null : pattern.getCode());
        shot.put("patternOriginal", pattern == null ? null : pattern.getPatternOriginal());
        shot.put("patternCategory", pattern == null ? null : pattern.getCategory().name());
        shot.put("derivation", analysis.getDerivation() == null ? null : analysis.getDerivation().name());
        shot.put("verbClass", analysis.getVerbClass() == null ? null : analysis.getVerbClass().name());
        shot.put("imperfectVowel", analysis.getImperfectVowel() == null ? null : analysis.getImperfectVowel().name());
        shot.put("notes", analysis.getNotes());
        shot.put("features", analysis.getFeatures() == null ? Map.of() : new LinkedHashMap<>(analysis.getFeatures()));
        shot.put("segmentation", analysis.getSegmentation() == null ? Map.of() : new LinkedHashMap<>(analysis.getSegmentation()));
        return shot;
    }

    private void applyPattern(MorphologicalPatternEntity pattern, String code, String original, PatternCategory category, int radicals, String description, boolean creating) {
        if (creating) {
            if (code == null || !code.matches("[A-Z0-9_]{2,40}")) {
                throw invalid("code", "Use a stable uppercase code.");
            }
            if (patterns.findByCode(code).isPresent()) {
                throw new ConflictException("A pattern with this code already exists.");
            }
            pattern.setCode(code);
            pattern.setStatus("ACTIVE");
        }
        if (original == null || original.isBlank()) {
            throw invalid("patternOriginal", "The Arabic pattern is required.");
        }
        ArabicText normalized = text.normalize(original.trim());
        if (!ArabicLetters.words(normalized.normalizedText()) || radicals < 2 || radicals > 5 || category == null) {
            throw invalid("patternOriginal", "The pattern must be Arabic and declare its radical count.");
        }
        pattern.setPatternOriginal(original.trim());
        pattern.setPatternNormalized(normalized.normalizedText());
        pattern.setCategory(category);
        pattern.setRadicalCount((short) radicals);
        pattern.setDescription(description == null || description.isBlank() ? null : description.trim());
    }

    private AnalysisView finish(MorphologyAnalysisEntity analysis, AuditEventType event) {
        stamp(analysis, authorization.requireAccess().userId(), false);
        analyses.saveAndFlush(analysis);
        entityManager.refresh(analysis);
        bump();
        audit.record(authorization.requireAccess().userId(), event, TARGET, analysis.getId().toString(), Map.of("status", analysis.getStatus().name()));
        return analysisView(analysis, citationIds(List.of(analysis.getId())).getOrDefault(analysis.getId(), List.of()));
    }

    private AnalysisView move(UUID id, long version, java.util.function.Function<PublicationStatus, PublicationStatus> transition, AuditEventType event, boolean review) {
        MorphologyAnalysisEntity analysis = lockedAnalysis(id, version);
        if (review) {
            EditorialGuards.requireDifferentPerson(analysis.getCreatedBy(), authorization.requireAccess().userId(), "The creator cannot review their own analysis.");
        }
        analysis.setStatus(transition.apply(analysis.getStatus()));
        return finish(analysis, event);
    }

    private String validated(String word) {
        if (word == null || word.isBlank()) {
            throw invalid("word", "Enter an Arabic word.");
        }
        if (word.codePointCount(0, word.length()) > properties.getMaxInputLength()) {
            throw invalid("word", "This word is too long.");
        }
        ArabicText normalized = text.normalize(word.trim());
        if (!ArabicLetters.words(normalized.normalizedText())) {
            throw invalid("word", "Enter Arabic letters.");
        }
        return normalized.normalizedText();
    }

    private List<String> stems(String normalized) {
        return com.mrsoft.arabicreference.morphology.domain.CliticSegmenter.segment(normalized).stream().map(Segmentation::stem).distinct().toList();
    }

    private long generation() {
        return states.findById((short) 1).map(MorphologyStateEntity::getGeneration).orElse(1L);
    }

    private void bump() {
        MorphologyStateEntity state = states.lock().orElseThrow();
        state.setGeneration(state.getGeneration() + 1);
        states.saveAndFlush(state);
    }

    private MorphologicalPatternEntity lockedPattern(UUID id, long version) {
        MorphologicalPatternEntity pattern = patterns.lockById(id).orElseThrow(() -> new ResourceNotFoundException("Pattern was not found."));
        if (pattern.getVersion() != version) {
            throw new ConflictException("The record was updated by someone else. Reload and try again.");
        }
        return pattern;
    }

    private MorphologyAnalysisEntity lockedAnalysis(UUID id, long version) {
        MorphologyAnalysisEntity analysis = analyses.lockById(id).orElseThrow(this::missingAnalysis);
        if (analysis.getVersion() != version) {
            throw new ConflictException("The record was updated by someone else. Reload and try again.");
        }
        return analysis;
    }

    private void stamp(MorphologyAnalysisEntity analysis, UUID actor, boolean creating) {
        var now = timeProvider.now();
        if (creating) {
            analysis.setCreatedAt(now);
            analysis.setCreatedBy(actor);
        }
        analysis.setUpdatedAt(now);
        analysis.setUpdatedBy(actor);
    }

    private void InstantNow(MorphologicalPatternEntity pattern, boolean creating) {
        var now = timeProvider.now();
        if (creating) {
            pattern.setCreatedAt(now);
        }
        pattern.setUpdatedAt(now);
    }

    private PatternView patternView(MorphologicalPatternEntity pattern) {
        return new PatternView(pattern.getId(), pattern.getCode(), pattern.getPatternOriginal(), pattern.getPatternNormalized(), pattern.getCategory(), pattern.getRadicalCount(), pattern.getDescription(), pattern.getStatus(), pattern.getVersion());
    }

    private AnalysisView analysisView(MorphologyAnalysisEntity analysis, List<UUID> citationIds) {
        EditableLemma entry = dictionary.editableEntry(analysis.getLexicalEntryId()).orElse(null);
        MorphologicalPatternEntity pattern = analysis.getPatternId() == null ? null : patterns.findById(analysis.getPatternId()).orElse(null);
        return new AnalysisView(
                analysis.getId(),
                analysis.getLexicalEntryId(),
                entry == null ? null : entry.lemmaOriginal(),
                analysis.getPatternId(),
                pattern == null ? null : pattern.getCode(),
                pattern == null ? null : pattern.getPatternOriginal(),
                pattern == null ? null : pattern.getCategory(),
                analysis.getDerivation(),
                analysis.getVerbClass(),
                analysis.getImperfectVowel(),
                analysis.getNotes(),
                featuresOf(analysis.getFeatures()),
                analysis.getStatus(),
                citationIds,
                analysis.getVersion());
    }

    private RuleView ruleView(MorphologyRuleEntity rule) {
        return new RuleView(rule.getCode(), rule.getExplanationCode(), rule.getDescription(), rule.isEnabled(), rule.getRuleSetVersion(), rule.getVersion());
    }

    private Map<UUID, List<UUID>> citationIds(List<UUID> ids) {
        Map<UUID, List<UUID>> grouped = new LinkedHashMap<>();
        if (ids.isEmpty()) {
            return grouped;
        }
        for (MorphologyAnalysisCitationEntity link : citations.findByOwnerIdIn(ids)) {
            grouped.computeIfAbsent(link.getOwnerId(), ignored -> new ArrayList<>()).add(link.getCitationId());
        }
        return grouped;
    }

    private ConjugationView unsupported(UUID entryId, String verbClass, String reason) {
        return new ConjugationView(entryId, CoverageStatus.UNSUPPORTED, verbClass, null, RuleSet.VERSION, reason, List.of());
    }

    private static void page(int page, int size) {
        if (page < 0 || size < 1 || size > 50) {
            throw invalid("page", "Page size must be from 1 to 50.");
        }
    }

    private static String cleanNotes(String notes) {
        if (notes == null || notes.isBlank()) {
            return null;
        }
        if (notes.codePointCount(0, notes.length()) > 500) {
            throw invalid("notes", "Notes are too long.");
        }
        return notes.trim();
    }

    private static Map<String, Object> featureMap(MorphologicalFeatures features) {
        Map<String, Object> map = new LinkedHashMap<>();
        put(map, "person", features.person());
        put(map, "number", features.number());
        put(map, "gender", features.gender());
        put(map, "aspect", features.aspect());
        put(map, "mood", features.mood());
        put(map, "voice", features.voice());
        put(map, "grammaticalCase", features.grammaticalCase());
        put(map, "definiteness", features.definiteness());
        return map;
    }

    private static Map<String, Object> segmentationMap(Segmentation segmentation) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("clitics", segmentation.clitics());
        map.put("prefixes", segmentation.prefixes());
        map.put("stem", segmentation.stem());
        map.put("suffixes", segmentation.suffixes());
        return map;
    }

    private static MorphologicalFeatures featuresOf(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return MorphologicalFeatures.empty();
        }
        return new MorphologicalFeatures(
                enumOf(text(map, "person"), MorphologicalFeatures.Person.class),
                enumOf(text(map, "number"), MorphologicalFeatures.GrammaticalNumber.class),
                enumOf(text(map, "gender"), MorphologicalFeatures.FeatureGender.class),
                enumOf(text(map, "aspect"), MorphologicalFeatures.VerbAspect.class),
                enumOf(text(map, "mood"), MorphologicalFeatures.VerbMood.class),
                enumOf(text(map, "voice"), MorphologicalFeatures.VerbVoice.class),
                enumOf(text(map, "grammaticalCase"), MorphologicalFeatures.NominalCase.class),
                enumOf(text(map, "definiteness"), MorphologicalFeatures.Definiteness.class));
    }

    private static Segmentation segmentationOf(Map<String, Object> map) {
        if (map == null) {
            return Segmentation.identity("");
        }
        return new Segmentation(strings(map.get("clitics")), strings(map.get("prefixes")), text(map, "stem"), strings(map.get("suffixes")));
    }

    private static void put(Map<String, Object> map, String key, Enum<?> value) {
        if (value != null) {
            map.put(key, value.name());
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) {
        if (value instanceof Map<?, ?> raw) {
            Map<String, Object> copy = new LinkedHashMap<>();
            raw.forEach((key, item) -> copy.put(String.valueOf(key), item));
            return copy;
        }
        return Map.of();
    }

    private static List<String> strings(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        return list.stream().map(String::valueOf).toList();
    }

    private static String text(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private static UUID uuid(Object value) {
        if (value == null) {
            return null;
        }
        return value instanceof UUID id ? id : UUID.fromString(String.valueOf(value));
    }

    private static <E extends Enum<E>> E enumOf(String value, Class<E> type) {
        if (value == null || value.isBlank() || "null".equals(value)) {
            return null;
        }
        try {
            return Enum.valueOf(type, value);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static ValidationException invalid(String field, String message) {
        return new ValidationException(message, List.of(new FieldErrorDetail(field, message)));
    }

    private ResourceNotFoundException missingAnalysis() {
        return new ResourceNotFoundException("Morphological analysis was not found.");
    }

    private final class DictionaryLexicon implements PublishedLexicon {
        @Override
        public List<LexiconEntry> lemmas(String normalizedLemma) {
            return dictionary.lemmas(normalizedLemma).stream().map(MorphologyService.this::lexiconEntry).toList();
        }

        @Override
        public Optional<LexiconRoot> root(String normalizedRoot) {
            return dictionary.root(normalizedRoot).map(root -> new LexiconRoot(root.id(), root.original(), root.normalized(), root.radicalCount()));
        }

        @Override
        public List<LexiconEntry> entriesForRoot(UUID rootId) {
            return dictionary.entriesForRoot(rootId).stream().map(MorphologyService.this::lexiconEntry).toList();
        }

        @Override
        public Optional<LexiconEntry> entry(UUID id) {
            return dictionary.lemma(id).map(MorphologyService.this::lexiconEntry);
        }
    }

    private PublishedLexicon.LexiconEntry lexiconEntry(PublishedLemma entry) {
        return new PublishedLexicon.LexiconEntry(
                entry.id(),
                entry.lemmaOriginal(),
                entry.lemmaNormalized(),
                entry.vocalizedForm(),
                entry.rootId(),
                entry.rootOriginal(),
                entry.rootNormalized(),
                entry.radicalCount(),
                entry.partOfSpeech(),
                entry.slug());
    }
}
