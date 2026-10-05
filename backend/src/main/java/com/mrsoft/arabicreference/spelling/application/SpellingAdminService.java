package com.mrsoft.arabicreference.spelling.application;

import com.mrsoft.arabicreference.identity.application.AuthorizationService;
import com.mrsoft.arabicreference.identity.domain.AuditEventType;
import com.mrsoft.arabicreference.identity.domain.PermissionCatalog;
import com.mrsoft.arabicreference.linguistics.application.EditorialStore;
import com.mrsoft.arabicreference.linguistics.domain.editorial.PublicationStatus;
import com.mrsoft.arabicreference.linguistics.domain.text.ContentSlugs;
import com.mrsoft.arabicreference.linguistics.domain.text.KnowledgeText;
import com.mrsoft.arabicreference.shared.infrastructure.persistence.OwnerCitationEntity;
import com.mrsoft.arabicreference.shared.kernel.exception.ConflictException;
import com.mrsoft.arabicreference.shared.kernel.exception.FieldErrorDetail;
import com.mrsoft.arabicreference.shared.kernel.exception.ResourceNotFoundException;
import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import com.mrsoft.arabicreference.shared.kernel.id.Ids;
import com.mrsoft.arabicreference.source.application.CitationChecks;
import com.mrsoft.arabicreference.source.application.SourceLines;
import com.mrsoft.arabicreference.spelling.application.SpellingViews.ClauseDraft;
import com.mrsoft.arabicreference.spelling.application.SpellingViews.ClauseView;
import com.mrsoft.arabicreference.spelling.application.SpellingViews.ExampleDraft;
import com.mrsoft.arabicreference.spelling.application.SpellingViews.ExampleView;
import com.mrsoft.arabicreference.spelling.application.SpellingViews.PageResult;
import com.mrsoft.arabicreference.spelling.application.SpellingViews.ReviewItem;
import com.mrsoft.arabicreference.spelling.application.SpellingViews.RuleAdmin;
import com.mrsoft.arabicreference.spelling.application.SpellingViews.RuleDraft;
import com.mrsoft.arabicreference.spelling.application.SpellingViews.RuleUpdate;
import com.mrsoft.arabicreference.spelling.application.SpellingViews.TopicAdmin;
import com.mrsoft.arabicreference.spelling.application.SpellingViews.TopicDraft;
import com.mrsoft.arabicreference.spelling.application.SpellingViews.TopicSummary;
import com.mrsoft.arabicreference.spelling.application.SpellingViews.TopicUpdate;
import com.mrsoft.arabicreference.spelling.domain.SpellingExampleRules;
import com.mrsoft.arabicreference.spelling.infrastructure.persistence.SpellingClauseEntity;
import com.mrsoft.arabicreference.spelling.infrastructure.persistence.SpellingClauseRepository;
import com.mrsoft.arabicreference.spelling.infrastructure.persistence.SpellingExampleEntity;
import com.mrsoft.arabicreference.spelling.infrastructure.persistence.SpellingExampleRepository;
import com.mrsoft.arabicreference.spelling.infrastructure.persistence.SpellingRuleCitationEntity;
import com.mrsoft.arabicreference.spelling.infrastructure.persistence.SpellingRuleCitationRepository;
import com.mrsoft.arabicreference.spelling.infrastructure.persistence.SpellingRuleEntity;
import com.mrsoft.arabicreference.spelling.infrastructure.persistence.SpellingRuleRepository;
import com.mrsoft.arabicreference.spelling.infrastructure.persistence.SpellingTopicCitationEntity;
import com.mrsoft.arabicreference.spelling.infrastructure.persistence.SpellingTopicCitationRepository;
import com.mrsoft.arabicreference.spelling.infrastructure.persistence.SpellingTopicEntity;
import com.mrsoft.arabicreference.spelling.infrastructure.persistence.SpellingTopicRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SpellingAdminService {

    static final String TOPIC = "spelling_topic";
    static final String RULE = "spelling_rule";

    private final SpellingTopicRepository topics;
    private final SpellingRuleRepository rules;
    private final SpellingClauseRepository clauses;
    private final SpellingExampleRepository examples;
    private final SpellingTopicCitationRepository topicCitations;
    private final SpellingRuleCitationRepository ruleCitations;
    private final EditorialStore editorial;
    private final AuthorizationService authorization;
    private final CitationChecks citations;
    private final SpellingSearchIndexer search;

    public SpellingAdminService(
            SpellingTopicRepository topics,
            SpellingRuleRepository rules,
            SpellingClauseRepository clauses,
            SpellingExampleRepository examples,
            SpellingTopicCitationRepository topicCitations,
            SpellingRuleCitationRepository ruleCitations,
            EditorialStore editorial,
            AuthorizationService authorization,
            CitationChecks citations,
            SpellingSearchIndexer search) {
        this.topics = topics;
        this.rules = rules;
        this.clauses = clauses;
        this.examples = examples;
        this.topicCitations = topicCitations;
        this.ruleCitations = ruleCitations;
        this.editorial = editorial;
        this.authorization = authorization;
        this.citations = citations;
        this.search = search;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.SPELLING_TOPIC_VIEW + "')")
    public PageResult<TopicSummary> topics(int page, int size) {
        var result = topics.findAllByOrderByUpdatedAtDesc(page(page, size));
        return new PageResult<>(result.map(topic -> new TopicSummary(topic.getId(), topic.getTitleOriginal(), topic.getSlug(), topic.getStatus().name(), topic.getDisplayOrder(), topic.getVersion())).toList(), page, size, result.getTotalElements());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.SPELLING_TOPIC_VIEW + "')")
    public TopicAdmin topic(UUID id) {
        return topicAdmin(topics.findById(id).orElseThrow(() -> missing("Topic")));
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.SPELLING_TOPIC_MANAGE + "')")
    public TopicAdmin createTopic(TopicDraft draft) {
        UUID actor = authorization.requireAccess().userId();
        SpellingTopicEntity topic = new SpellingTopicEntity();
        editorial.prepareNew(topic, actor);
        applyTopic(topic, draft.title(), draft.summary(), draft.displayOrder());
        topic.setSlug(ContentSlugs.of(topic.getTitleNormalized(), topic.getId()));
        editorial.persist(topics, topic, actor, AuditEventType.SPELLING_TOPIC_CREATED, TOPIC);
        return topicAdmin(topic);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.SPELLING_TOPIC_MANAGE + "')")
    public TopicAdmin updateTopic(UUID id, TopicUpdate update) {
        SpellingTopicEntity topic = lockedTopic(id, update.version());
        UUID actor = authorization.requireAccess().userId();
        editorial.open(topic, actor, "topic update", TOPIC);
        applyTopic(topic, update.title(), update.summary(), update.displayOrder());
        editorial.persist(topics, topic, actor, AuditEventType.SPELLING_TOPIC_UPDATED, TOPIC);
        return topicAdmin(topic);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.CITATION_MANAGE + "')")
    public TopicAdmin citeTopic(UUID id, long version, UUID citationId) {
        citations.requireExisting(citationId);
        SpellingTopicEntity topic = lockedTopic(id, version);
        UUID actor = authorization.requireAccess().userId();
        editorial.open(topic, actor, "citation linked", TOPIC);
        if (!topicCitations.existsByOwnerIdAndCitationId(id, citationId)) {
            topicCitations.save(new SpellingTopicCitationEntity(id, citationId));
        }
        editorial.persist(topics, topic, actor, AuditEventType.CITATION_ADDED, TOPIC);
        return topicAdmin(topic);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.SPELLING_RULE_EDIT + "')")
    public TopicAdmin submitTopic(UUID id, long version) {
        return moveTopic(id, version, "submit");
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.SPELLING_RULE_REVIEW + "')")
    public TopicAdmin verifyTopic(UUID id, long version) {
        return moveTopic(id, version, "verify");
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.SPELLING_RULE_REVIEW + "')")
    public TopicAdmin requestTopicChanges(UUID id, long version, String reason) {
        SpellingTopicEntity topic = lockedTopic(id, version);
        UUID actor = authorization.requireAccess().userId();
        editorial.requestChanges(topic, actor, KnowledgeText.required(reason, "reason", 500));
        editorial.persist(topics, topic, actor, AuditEventType.SPELLING_CONTENT_CHANGES_REQUESTED, TOPIC);
        return topicAdmin(topic);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.SPELLING_RULE_PUBLISH + "')")
    public TopicAdmin publishTopic(UUID id, long version) {
        search.lock();
        SpellingTopicEntity topic = lockedTopic(id, version);
        UUID actor = authorization.requireAccess().userId();
        List<UUID> citationIds = ids(topicCitations.findByOwnerId(id));
        var lines = SourceLines.of(citations.requirePublishable(citationIds));
        editorial.publish(topic, actor);
        topic.setPublishedSnapshot(topicSnapshot(topic, lines));
        editorial.persist(topics, topic, actor, AuditEventType.SPELLING_TOPIC_PUBLISHED, TOPIC);
        search.onTopicPublished(topic);
        return topicAdmin(topic);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.SPELLING_RULE_PUBLISH + "')")
    public TopicAdmin archiveTopic(UUID id, long version) {
        search.lock();
        SpellingTopicEntity topic = lockedTopic(id, version);
        UUID actor = authorization.requireAccess().userId();
        editorial.archive(topic);
        editorial.persist(topics, topic, actor, AuditEventType.SPELLING_CONTENT_ARCHIVED, TOPIC);
        search.onTopicArchived(id);
        return topicAdmin(topic);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.SPELLING_TOPIC_VIEW + "')")
    public RuleAdmin rule(UUID id) {
        return ruleAdmin(rules.findById(id).orElseThrow(() -> missing("Rule")));
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.SPELLING_RULE_CREATE + "')")
    public RuleAdmin createRule(RuleDraft draft) {
        if (topics.findById(draft.topicId()).isEmpty()) {
            throw missing("Topic");
        }
        UUID actor = authorization.requireAccess().userId();
        SpellingRuleEntity rule = new SpellingRuleEntity();
        editorial.prepareNew(rule, actor);
        rule.setTopicId(draft.topicId());
        applyRule(rule, draft.title(), draft.summary(), draft.coreRule(), draft.difficulty());
        rule.setSlug(ContentSlugs.of(rule.getTitleNormalized(), rule.getId()));
        editorial.persist(rules, rule, actor, AuditEventType.SPELLING_RULE_CREATED, RULE);
        return ruleAdmin(rule);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.SPELLING_RULE_EDIT + "')")
    public RuleAdmin updateRule(UUID id, RuleUpdate update) {
        SpellingRuleEntity rule = lockedRule(id, update.version());
        UUID actor = authorization.requireAccess().userId();
        editorial.open(rule, actor, "rule update", RULE);
        applyRule(rule, update.title(), update.summary(), update.coreRule(), update.difficulty());
        editorial.persist(rules, rule, actor, AuditEventType.SPELLING_RULE_UPDATED, RULE);
        return ruleAdmin(rule);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.SPELLING_RULE_EDIT + "')")
    public RuleAdmin addClause(UUID id, ClauseDraft draft) {
        SpellingRuleEntity rule = lockedRule(id, draft.version());
        if (draft.kind() == null) {
            throw invalid("kind", "Choose a clause kind.");
        }
        UUID actor = authorization.requireAccess().userId();
        editorial.open(rule, actor, "clause added", RULE);
        SpellingClauseEntity clause = new SpellingClauseEntity();
        clause.setId(Ids.random());
        clause.setRuleId(id);
        clause.setKind(draft.kind());
        clause.setHeading(KnowledgeText.required(draft.heading(), "heading", 160));
        clause.setBody(KnowledgeText.required(draft.body(), "body", 4000));
        clause.setDisplayOrder(clauses.findByRuleIdOrderByDisplayOrderAsc(id).size());
        clauses.save(clause);
        editorial.persist(rules, rule, actor, AuditEventType.SPELLING_RULE_UPDATED, RULE);
        return ruleAdmin(rule);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.SPELLING_RULE_EDIT + "')")
    public RuleAdmin addExample(UUID id, ExampleDraft draft) {
        String correct = KnowledgeText.optional(draft.correctForm(), "correctForm", 300);
        String incorrect = KnowledgeText.optional(draft.incorrectForm(), "incorrectForm", 300);
        String explanation = KnowledgeText.optional(draft.explanation(), "explanation", 1000);
        String context = KnowledgeText.optional(draft.contextNote(), "contextNote", 500);
        String common = KnowledgeText.optional(draft.commonForm(), "commonForm", 300);
        String reason = KnowledgeText.optional(draft.reason(), "reason", 1000);
        SpellingExampleRules.check(draft.kind(), correct, incorrect, explanation, context, common, reason, draft.citationId());
        if (draft.citationId() != null) {
            citations.requireExisting(draft.citationId());
        }
        SpellingRuleEntity rule = lockedRule(id, draft.version());
        UUID actor = authorization.requireAccess().userId();
        editorial.open(rule, actor, "example added", RULE);
        SpellingExampleEntity example = new SpellingExampleEntity();
        example.setId(Ids.random());
        example.setRuleId(id);
        example.setKind(draft.kind());
        example.setCorrectForm(correct);
        example.setIncorrectForm(incorrect);
        example.setExplanation(explanation);
        example.setContextNote(context);
        example.setCommonForm(common);
        example.setReason(reason);
        example.setCitationId(draft.citationId());
        example.setDisplayOrder(examples.findByRuleIdOrderByDisplayOrderAsc(id).size());
        examples.save(example);
        editorial.persist(rules, rule, actor, AuditEventType.SPELLING_RULE_UPDATED, RULE);
        return ruleAdmin(rule);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.CITATION_MANAGE + "')")
    public RuleAdmin citeRule(UUID id, long version, UUID citationId) {
        citations.requireExisting(citationId);
        SpellingRuleEntity rule = lockedRule(id, version);
        UUID actor = authorization.requireAccess().userId();
        editorial.open(rule, actor, "citation linked", RULE);
        if (!ruleCitations.existsByOwnerIdAndCitationId(id, citationId)) {
            ruleCitations.save(new SpellingRuleCitationEntity(id, citationId));
        }
        editorial.persist(rules, rule, actor, AuditEventType.CITATION_ADDED, RULE);
        return ruleAdmin(rule);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.SPELLING_RULE_EDIT + "')")
    public RuleAdmin submitRule(UUID id, long version) {
        return moveRule(id, version, "submit");
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.SPELLING_RULE_REVIEW + "')")
    public RuleAdmin verifyRule(UUID id, long version) {
        return moveRule(id, version, "verify");
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.SPELLING_RULE_REVIEW + "')")
    public RuleAdmin requestRuleChanges(UUID id, long version, String reason) {
        SpellingRuleEntity rule = lockedRule(id, version);
        UUID actor = authorization.requireAccess().userId();
        editorial.requestChanges(rule, actor, KnowledgeText.required(reason, "reason", 500));
        editorial.persist(rules, rule, actor, AuditEventType.SPELLING_CONTENT_CHANGES_REQUESTED, RULE);
        return ruleAdmin(rule);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.SPELLING_RULE_PUBLISH + "')")
    public RuleAdmin publishRule(UUID id, long version) {
        search.lock();
        SpellingRuleEntity rule = lockedRule(id, version);
        UUID actor = authorization.requireAccess().userId();
        List<SpellingClauseEntity> parts = clauses.findByRuleIdOrderByDisplayOrderAsc(id);
        if (parts.isEmpty()) {
            throw new ConflictException("A published spelling rule needs at least one structured clause.");
        }
        List<SpellingExampleEntity> samples = examples.findByRuleIdOrderByDisplayOrderAsc(id);
        List<UUID> citationIds = new ArrayList<>(ids(ruleCitations.findByOwnerId(id)));
        for (SpellingExampleEntity example : samples) {
            if (example.getCitationId() != null) {
                citationIds.add(example.getCitationId());
            }
        }
        if (ids(ruleCitations.findByOwnerId(id)).isEmpty()) {
            throw new ConflictException("A published spelling rule needs at least one citation.");
        }
        var lines = SourceLines.of(citations.requirePublishable(citationIds));
        SpellingTopicEntity topic = topics.findById(rule.getTopicId()).orElseThrow(() -> missing("Topic"));
        editorial.publish(rule, actor);
        rule.setPublishedSnapshot(ruleSnapshot(rule, topic, parts, samples, lines));
        editorial.persist(rules, rule, actor, AuditEventType.SPELLING_RULE_PUBLISHED, RULE);
        search.onRulePublished(rule);
        return ruleAdmin(rule);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.SPELLING_RULE_PUBLISH + "')")
    public RuleAdmin archiveRule(UUID id, long version) {
        search.lock();
        SpellingRuleEntity rule = lockedRule(id, version);
        UUID actor = authorization.requireAccess().userId();
        editorial.archive(rule);
        editorial.persist(rules, rule, actor, AuditEventType.SPELLING_CONTENT_ARCHIVED, RULE);
        search.onRuleArchived(id);
        return ruleAdmin(rule);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.SPELLING_RULE_REVIEW + "')")
    public List<ReviewItem> reviewQueue() {
        List<ReviewItem> items = new ArrayList<>();
        for (SpellingTopicEntity topic : topics.findByStatusOrderByUpdatedAtDesc(PublicationStatus.IN_REVIEW)) {
            items.add(new ReviewItem("TOPIC", topic.getId(), topic.getTitleOriginal(), topic.getStatus().name(), topic.getVersion()));
        }
        for (SpellingRuleEntity rule : rules.findByStatusOrderByUpdatedAtDesc(PublicationStatus.IN_REVIEW)) {
            items.add(new ReviewItem("RULE", rule.getId(), rule.getTitleOriginal(), rule.getStatus().name(), rule.getVersion()));
        }
        return items;
    }

    private TopicAdmin moveTopic(UUID id, long version, String action) {
        SpellingTopicEntity topic = lockedTopic(id, version);
        UUID actor = authorization.requireAccess().userId();
        AuditEventType event;
        if ("submit".equals(action)) {
            editorial.submit(topic);
            event = AuditEventType.SPELLING_CONTENT_SUBMITTED;
        } else {
            editorial.review(topic, actor);
            event = AuditEventType.SPELLING_CONTENT_VERIFIED;
        }
        editorial.persist(topics, topic, actor, event, TOPIC);
        return topicAdmin(topic);
    }

    private RuleAdmin moveRule(UUID id, long version, String action) {
        SpellingRuleEntity rule = lockedRule(id, version);
        UUID actor = authorization.requireAccess().userId();
        AuditEventType event;
        if ("submit".equals(action)) {
            editorial.submit(rule);
            event = AuditEventType.SPELLING_CONTENT_SUBMITTED;
        } else {
            editorial.review(rule, actor);
            event = AuditEventType.SPELLING_CONTENT_VERIFIED;
        }
        editorial.persist(rules, rule, actor, event, RULE);
        return ruleAdmin(rule);
    }

    private Map<String, Object> topicSnapshot(SpellingTopicEntity topic, List<Map<String, Object>> sources) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("title", topic.getTitleOriginal());
        snapshot.put("slug", topic.getSlug());
        snapshot.put("summary", topic.getSummary());
        snapshot.put("sources", sources);
        return snapshot;
    }

    private Map<String, Object> ruleSnapshot(
            SpellingRuleEntity rule,
            SpellingTopicEntity topic,
            List<SpellingClauseEntity> parts,
            List<SpellingExampleEntity> samples,
            List<Map<String, Object>> sources) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("title", rule.getTitleOriginal());
        snapshot.put("slug", rule.getSlug());
        snapshot.put("summary", rule.getSummary());
        snapshot.put("coreRule", rule.getCoreRule());
        snapshot.put("difficulty", rule.getDifficulty() == null ? null : rule.getDifficulty().name());
        snapshot.put("difficultyLabel", rule.getDifficulty() == null ? null : rule.getDifficulty().arabicLabel());
        snapshot.put("topicId", topic.getId().toString());
        Map<String, Object> topicLink = new LinkedHashMap<>();
        topicLink.put("title", topic.getTitleOriginal());
        topicLink.put("slug", topic.getSlug());
        topicLink.put("summary", topic.getSummary());
        snapshot.put("topic", topicLink);
        List<Map<String, Object>> clauseMaps = new ArrayList<>();
        for (SpellingClauseEntity clause : parts) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("kind", clause.getKind().name());
            map.put("heading", clause.getHeading());
            map.put("body", clause.getBody());
            clauseMaps.add(map);
        }
        snapshot.put("clauses", clauseMaps);
        List<Map<String, Object>> exampleMaps = new ArrayList<>();
        for (SpellingExampleEntity example : samples) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("kind", example.getKind().name());
            map.put("correctForm", example.getCorrectForm());
            map.put("incorrectForm", example.getIncorrectForm());
            map.put("explanation", example.getExplanation());
            map.put("contextNote", example.getContextNote());
            map.put("commonForm", example.getCommonForm());
            map.put("reason", example.getReason());
            map.put("cited", example.getCitationId() != null);
            exampleMaps.add(map);
        }
        snapshot.put("examples", exampleMaps);
        snapshot.put("sources", sources);
        return snapshot;
    }

    private void applyTopic(SpellingTopicEntity topic, String title, String summary, Integer displayOrder) {
        String original = KnowledgeText.required(title, "title", 160);
        topic.setTitleOriginal(original);
        topic.setTitleNormalized(KnowledgeText.normalized(original));
        topic.setSummary(KnowledgeText.optional(summary, "summary", 1000));
        topic.setDisplayOrder(order(displayOrder));
    }

    private void applyRule(SpellingRuleEntity rule, String title, String summary, String coreRule, com.mrsoft.arabicreference.spelling.domain.SpellingDifficulty difficulty) {
        String original = KnowledgeText.required(title, "title", 160);
        rule.setTitleOriginal(original);
        rule.setTitleNormalized(KnowledgeText.normalized(original));
        rule.setSummary(KnowledgeText.optional(summary, "summary", 1000));
        rule.setCoreRule(KnowledgeText.required(coreRule, "coreRule", 4000));
        rule.setDifficulty(difficulty);
    }

    private SpellingTopicEntity lockedTopic(UUID id, long version) {
        SpellingTopicEntity topic = topics.lockById(id).orElseThrow(() -> missing("Topic"));
        editorial.requireVersion(topic, version);
        return topic;
    }

    private SpellingRuleEntity lockedRule(UUID id, long version) {
        SpellingRuleEntity rule = rules.lockById(id).orElseThrow(() -> missing("Rule"));
        editorial.requireVersion(rule, version);
        return rule;
    }

    private TopicAdmin topicAdmin(SpellingTopicEntity topic) {
        return new TopicAdmin(topic.getId(), topic.getTitleOriginal(), topic.getSlug(), topic.getSummary(), topic.getDisplayOrder(), topic.getStatus().name(), topic.getVersion(), ids(topicCitations.findByOwnerId(topic.getId())));
    }

    private RuleAdmin ruleAdmin(SpellingRuleEntity rule) {
        List<ClauseView> clauseViews = clauses.findByRuleIdOrderByDisplayOrderAsc(rule.getId()).stream()
                .map(clause -> new ClauseView(clause.getId(), clause.getKind().name(), clause.getHeading(), clause.getBody(), clause.getDisplayOrder()))
                .toList();
        List<ExampleView> exampleViews = examples.findByRuleIdOrderByDisplayOrderAsc(rule.getId()).stream()
                .map(example -> new ExampleView(example.getId(), example.getKind().name(), example.getCorrectForm(), example.getIncorrectForm(), example.getExplanation(), example.getContextNote(), example.getCommonForm(), example.getReason(), example.getCitationId(), example.getDisplayOrder()))
                .toList();
        return new RuleAdmin(rule.getId(), rule.getTopicId(), rule.getTitleOriginal(), rule.getSlug(), rule.getSummary(), rule.getCoreRule(), rule.getDifficulty() == null ? null : rule.getDifficulty().name(), rule.getStatus().name(), rule.getVersion(), clauseViews, exampleViews, ids(ruleCitations.findByOwnerId(rule.getId())));
    }

    private static List<UUID> ids(List<? extends OwnerCitationEntity> links) {
        return links.stream().map(OwnerCitationEntity::getCitationId).toList();
    }

    private static int order(Integer displayOrder) {
        if (displayOrder == null) {
            return 0;
        }
        if (displayOrder < 0) {
            throw invalid("displayOrder", "Display order cannot be negative.");
        }
        return displayOrder;
    }

    private static Pageable page(int page, int size) {
        if (page < 0 || size < 1 || size > 50) {
            throw invalid("size", "Page size must be from 1 to 50.");
        }
        return PageRequest.of(page, size);
    }

    private static ResourceNotFoundException missing(String name) {
        return new ResourceNotFoundException(name + " was not found.");
    }

    private static ValidationException invalid(String field, String message) {
        return new ValidationException(message, List.of(new FieldErrorDetail(field, message)));
    }
}
