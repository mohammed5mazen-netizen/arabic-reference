package com.mrsoft.arabicreference.grammar.application;

import com.mrsoft.arabicreference.grammar.application.GrammarViews.AliasAdmin;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.AliasDraft;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.AnnotationAdmin;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.AnnotationDraft;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.ComponentAdmin;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.ComponentDraft;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.ConceptAdmin;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.ConceptDraft;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.ConceptRuleRequest;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.ConceptSummary;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.ConceptUpdate;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.DependenciesRequest;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.DependencyAdmin;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.DependencyInput;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.ExampleAdmin;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.ExampleDraft;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.ExampleSummary;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.PageResult;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.ParentChange;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.PrerequisiteRequest;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.RelationAdmin;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.RelationDraft;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.ReviewItem;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.RoleDraft;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.RoleView;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.RuleAdmin;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.RuleDraft;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.RuleSummary;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.RuleUpdate;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.TokenAdmin;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.TokenInput;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.TokensRequest;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.TopicAdmin;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.TopicDraft;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.TopicSummary;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.TopicUpdate;
import com.mrsoft.arabicreference.grammar.domain.AnnotationConstraints;
import com.mrsoft.arabicreference.grammar.domain.ArabicPhrase;
import com.mrsoft.arabicreference.grammar.domain.ExampleConstraints;
import com.mrsoft.arabicreference.grammar.domain.GrammarCrossLinks;
import com.mrsoft.arabicreference.grammar.domain.TopicGraph;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.EditorialRecord;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarAliasRepository;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarAnnotationEntity;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarAnnotationRepository;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarComponentCitationEntity;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarComponentCitationRepository;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarComponentRepository;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarConceptAliasEntity;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarConceptCitationEntity;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarConceptCitationRepository;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarConceptEntity;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarConceptRepository;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarConceptRuleEntity;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarConceptRuleRepository;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarDependencyEntity;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarDependencyRepository;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarExampleEntity;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarExampleRepository;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarPrerequisiteEntity;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarPrerequisiteRepository;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarRelationRepository;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarRoleEntity;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarRoleRepository;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarRuleCitationEntity;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarRuleCitationRepository;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarRuleComponentEntity;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarRuleEntity;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarRuleRepository;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarTokenEntity;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarTokenRepository;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarTopicCitationEntity;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarTopicCitationRepository;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarTopicEntity;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarTopicRepository;
import com.mrsoft.arabicreference.identity.application.AuditRecorder;
import com.mrsoft.arabicreference.identity.application.AuthorizationService;
import com.mrsoft.arabicreference.identity.domain.AuditEventType;
import com.mrsoft.arabicreference.identity.domain.PermissionCatalog;
import com.mrsoft.arabicreference.linguistics.application.ContentRevisionRecorder;
import com.mrsoft.arabicreference.linguistics.domain.editorial.EditorialGuards;
import com.mrsoft.arabicreference.linguistics.domain.editorial.EditorialWorkflow;
import com.mrsoft.arabicreference.linguistics.domain.editorial.PublicationStatus;
import com.mrsoft.arabicreference.linguistics.domain.text.ArabicTextNormalizer;
import com.mrsoft.arabicreference.linguistics.domain.text.ContentSlugs;
import com.mrsoft.arabicreference.shared.kernel.exception.ConflictException;
import com.mrsoft.arabicreference.shared.kernel.exception.FieldErrorDetail;
import com.mrsoft.arabicreference.shared.kernel.exception.ForbiddenOperationException;
import com.mrsoft.arabicreference.shared.kernel.exception.ResourceNotFoundException;
import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import com.mrsoft.arabicreference.shared.kernel.id.Ids;
import com.mrsoft.arabicreference.shared.kernel.security.AuthenticatedAccess;
import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import com.mrsoft.arabicreference.source.application.SourceAdminService;
import com.mrsoft.arabicreference.source.application.SourceViews.CitationView;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GrammarAdminService {

    private static final String TOPIC = "grammar_topic";
    private static final String RULE = "grammar_rule";
    private static final String CONCEPT = "grammar_concept";
    private static final String ANNOTATION = "grammar_annotation";

    private final GrammarTopicRepository topics;
    private final GrammarRuleRepository rules;
    private final GrammarConceptRepository concepts;
    private final GrammarAnnotationRepository annotations;
    private final GrammarComponentRepository components;
    private final GrammarExampleRepository examples;
    private final GrammarAliasRepository aliases;
    private final GrammarRelationRepository relations;
    private final GrammarPrerequisiteRepository prerequisites;
    private final GrammarTopicCitationRepository topicCitations;
    private final GrammarRuleCitationRepository ruleCitations;
    private final GrammarComponentCitationRepository componentCitations;
    private final GrammarConceptCitationRepository conceptCitations;
    private final GrammarConceptRuleRepository conceptRules;
    private final GrammarRoleRepository roles;
    private final GrammarTokenRepository tokens;
    private final GrammarDependencyRepository dependencies;
    private final GrammarCrossLinks links;
    private final SourceAdminService sources;
    private final ContentRevisionRecorder revisions;
    private final AuditRecorder audit;
    private final AuthorizationService authorization;
    private final TimeProvider timeProvider;
    private final EntityManager entityManager;
    private final JdbcTemplate jdbc;
    private final GrammarSearchIndexer searchIndexer;
    private final ArabicTextNormalizer normalizer = new ArabicTextNormalizer();

    public GrammarAdminService(
            GrammarTopicRepository topics,
            GrammarRuleRepository rules,
            GrammarConceptRepository concepts,
            GrammarAnnotationRepository annotations,
            GrammarComponentRepository components,
            GrammarExampleRepository examples,
            GrammarAliasRepository aliases,
            GrammarRelationRepository relations,
            GrammarPrerequisiteRepository prerequisites,
            GrammarTopicCitationRepository topicCitations,
            GrammarRuleCitationRepository ruleCitations,
            GrammarComponentCitationRepository componentCitations,
            GrammarConceptCitationRepository conceptCitations,
            GrammarConceptRuleRepository conceptRules,
            GrammarRoleRepository roles,
            GrammarTokenRepository tokens,
            GrammarDependencyRepository dependencies,
            GrammarCrossLinks links,
            SourceAdminService sources,
            ContentRevisionRecorder revisions,
            AuditRecorder audit,
            AuthorizationService authorization,
            TimeProvider timeProvider,
            EntityManager entityManager,
            JdbcTemplate jdbc,
            GrammarSearchIndexer searchIndexer) {
        this.topics = topics;
        this.rules = rules;
        this.concepts = concepts;
        this.annotations = annotations;
        this.components = components;
        this.examples = examples;
        this.aliases = aliases;
        this.relations = relations;
        this.prerequisites = prerequisites;
        this.topicCitations = topicCitations;
        this.ruleCitations = ruleCitations;
        this.componentCitations = componentCitations;
        this.conceptCitations = conceptCitations;
        this.conceptRules = conceptRules;
        this.roles = roles;
        this.tokens = tokens;
        this.dependencies = dependencies;
        this.links = links;
        this.sources = sources;
        this.revisions = revisions;
        this.audit = audit;
        this.authorization = authorization;
        this.timeProvider = timeProvider;
        this.entityManager = entityManager;
        this.jdbc = jdbc;
        this.searchIndexer = searchIndexer;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_TOPIC_VIEW + "')")
    public PageResult<TopicSummary> topics(int page, int size) {
        Page<GrammarTopicEntity> result = topics.findAllByOrderByUpdatedAtDesc(page(page, size));
        return new PageResult<>(result.map(this::topicSummary).toList(), page, size, result.getTotalElements());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_TOPIC_VIEW + "')")
    public TopicAdmin topic(UUID id) {
        return topicAdmin(topics.findById(id).orElseThrow(() -> missing("Topic")));
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_TOPIC_MANAGE + "')")
    public TopicAdmin createTopic(TopicDraft draft) {
        if (draft == null) {
            throw invalid("title", "A topic title is required.");
        }
        UUID actor = authorization.requireAccess().userId();
        GrammarTopicEntity topic = new GrammarTopicEntity();
        topic.setId(Ids.random());
        applyTopic(topic, draft.title(), draft.summary(), draft.category(), draft.difficulty(), draft.displayOrder(), true);
        if (draft.parentId() != null && topics.findById(draft.parentId()).isEmpty()) {
            throw missing("Topic");
        }
        topic.setParentId(draft.parentId());
        topic.setSlug(ContentSlugs.of(topic.getTitleNormalized(), topic.getId()));
        topic.setStatus(PublicationStatus.DRAFT);
        stamp(topic, actor, true);
        finish(topics, topic, actor, AuditEventType.GRAMMAR_TOPIC_CREATED, TOPIC);
        return topicAdmin(topic);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_TOPIC_MANAGE + "')")
    public TopicAdmin updateTopic(UUID id, TopicUpdate update) {
        GrammarTopicEntity topic = lockedTopic(id, update.version());
        UUID actor = authorization.requireAccess().userId();
        open(topic, actor, "topic edited", TOPIC);
        applyTopic(topic, update.title(), update.summary(), update.category(), update.difficulty(), update.displayOrder(), false);
        finish(topics, topic, actor, AuditEventType.GRAMMAR_TOPIC_UPDATED, TOPIC);
        return topicAdmin(topic);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_TOPIC_MANAGE + "')")
    public TopicAdmin moveTopic(UUID id, ParentChange change) {
        GrammarTopicEntity topic = lockHierarchy(id, change.version(), change.parentId());
        UUID actor = authorization.requireAccess().userId();
        open(topic, actor, "parent changed", TOPIC);
        topic.setParentId(change.parentId());
        finish(topics, topic, actor, AuditEventType.GRAMMAR_TOPIC_UPDATED, TOPIC);
        return topicAdmin(topic);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_TOPIC_MANAGE + "')")
    public TopicAdmin addPrerequisite(UUID id, PrerequisiteRequest request) {
        GrammarTopicEntity topic = lockedTopic(id, request.version());
        if (request.requiredTopicId() == null || topics.findById(request.requiredTopicId()).isEmpty()) {
            throw missing("Topic");
        }
        TopicGraph.assertNoPrerequisiteCycle(id, request.requiredTopicId(), candidate ->
                prerequisites.findByTopicId(candidate).stream().map(GrammarPrerequisiteEntity::getRequiredTopicId).toList());
        if (prerequisites.existsByTopicIdAndRequiredTopicId(id, request.requiredTopicId())) {
            throw new ConflictException("That prerequisite is already recorded.");
        }
        UUID actor = authorization.requireAccess().userId();
        open(topic, actor, "prerequisite added", TOPIC);
        prerequisites.save(new GrammarPrerequisiteEntity(id, request.requiredTopicId()));
        finish(topics, topic, actor, AuditEventType.GRAMMAR_TOPIC_UPDATED, TOPIC);
        return topicAdmin(topic);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.CITATION_MANAGE + "')")
    public TopicAdmin citeTopic(UUID id, long version, UUID citationId) {
        GrammarTopicEntity topic = lockedTopic(id, version);
        requireCitation(citationId);
        UUID actor = authorization.requireAccess().userId();
        open(topic, actor, "citation linked", TOPIC);
        if (!topicCitations.existsByOwnerIdAndCitationId(id, citationId)) {
            topicCitations.save(new GrammarTopicCitationEntity(id, citationId));
        }
        finish(topics, topic, actor, AuditEventType.CITATION_ADDED, TOPIC);
        return topicAdmin(topic);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_RULE_SUBMIT + "')")
    public TopicAdmin submitTopic(UUID id, long version) {
        return moveTopic(id, version, EditorialWorkflow::submit, AuditEventType.GRAMMAR_CONTENT_SUBMITTED, false, null);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_RULE_REVIEW + "')")
    public TopicAdmin requestTopicChanges(UUID id, long version, String reason) {
        requireReason(reason);
        return moveTopic(id, version, EditorialWorkflow::requestChanges, AuditEventType.GRAMMAR_CONTENT_CHANGES_REQUESTED, true, reason.trim());
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_RULE_REVIEW + "')")
    public TopicAdmin verifyTopic(UUID id, long version) {
        return moveTopic(id, version, EditorialWorkflow::verify, AuditEventType.GRAMMAR_CONTENT_VERIFIED, true, null);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_RULE_PUBLISH + "')")
    public TopicAdmin publishTopic(UUID id, long version) {
        searchIndexer.lock();
        GrammarTopicEntity topic = lockedTopic(id, version);
        UUID actor = authorization.requireAccess().userId();
        separatePublisher(topic, actor);
        if (topic.getSummary() == null || topic.getSummary().isBlank()) {
            throw invalid("summary", "A published topic needs a summary.");
        }
        List<UUID> citationIds = citationIds(topicCitations.findByOwnerId(id));
        assertPublishable(citationIds);
        com.mrsoft.arabicreference.linguistics.application.PublicationChecks.assertNoOpenBlocker("GRAMMAR_TOPIC", topic.getId());
        topic.setStatus(EditorialWorkflow.publish(topic.getStatus()));
        topic.setPublishedTitle(topic.getTitleOriginal());
        topic.setPublishedNormalized(topic.getTitleNormalized());
        topic.setPublishedSummary(topic.getSummary());
        topic.setPublishedParentId(topic.getParentId());
        topic.setPublishedDisplayOrder(topic.getDisplayOrder());
        topic.setPublishedCategory(topic.getCategory());
        topic.setPublishedSnapshot(topicSnapshot(topic, citationIds));
        finish(topics, topic, actor, AuditEventType.GRAMMAR_CONTENT_PUBLISHED, TOPIC);
        searchIndexer.onTopicPublished(topic);
        return topicAdmin(topic);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_RULE_ARCHIVE + "')")
    public TopicAdmin archiveTopic(UUID id, long version) {
        searchIndexer.lock();
        GrammarTopicEntity topic = lockedTopic(id, version);
        topic.setStatus(EditorialWorkflow.archive(topic.getStatus()));
        topic.setPublishedSnapshot(null);
        topic.setPublishedTitle(null);
        topic.setPublishedNormalized(null);
        topic.setPublishedSummary(null);
        topic.setPublishedParentId(null);
        topic.setPublishedDisplayOrder(null);
        topic.setPublishedCategory(null);
        finish(topics, topic, authorization.requireAccess().userId(), AuditEventType.GRAMMAR_CONTENT_ARCHIVED, TOPIC);
        searchIndexer.onTopicArchived(topic.getId());
        return topicAdmin(topic);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_RULE_VIEW + "')")
    public PageResult<RuleSummary> rules(int page, int size) {
        Page<GrammarRuleEntity> result = rules.findAllByOrderByUpdatedAtDesc(page(page, size));
        return new PageResult<>(result.map(rule -> new RuleSummary(rule.getId(), rule.getTopicId(), rule.getTitleOriginal(), rule.getSlug(), rule.getStatus(), rule.getVersion())).toList(), page, size, result.getTotalElements());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_RULE_VIEW + "')")
    public RuleAdmin rule(UUID id) {
        return ruleAdmin(rules.findById(id).orElseThrow(() -> missing("Rule")));
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_RULE_CREATE + "')")
    public RuleAdmin createRule(RuleDraft draft) {
        if (draft == null || draft.topicId() == null || topics.findById(draft.topicId()).isEmpty()) {
            throw missing("Topic");
        }
        UUID actor = authorization.requireAccess().userId();
        GrammarRuleEntity rule = new GrammarRuleEntity();
        rule.setId(Ids.random());
        rule.setTopicId(draft.topicId());
        applyRule(rule, draft.title(), draft.summary(), draft.ruleText(), draft.difficulty(), draft.displayOrder());
        rule.setSlug(ContentSlugs.of(rule.getTitleNormalized(), rule.getId()));
        rule.setStatus(PublicationStatus.DRAFT);
        stamp(rule, actor, true);
        finish(rules, rule, actor, AuditEventType.GRAMMAR_RULE_CREATED, RULE);
        return ruleAdmin(rule);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_RULE_EDIT + "')")
    public RuleAdmin updateRule(UUID id, RuleUpdate update) {
        GrammarRuleEntity rule = lockedRule(id, update.version());
        UUID actor = authorization.requireAccess().userId();
        open(rule, actor, "rule edited", RULE);
        applyRule(rule, update.title(), update.summary(), update.ruleText(), update.difficulty(), update.displayOrder());
        finish(rules, rule, actor, AuditEventType.GRAMMAR_RULE_UPDATED, RULE);
        return ruleAdmin(rule);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_RULE_EDIT + "')")
    public RuleAdmin addComponent(UUID id, ComponentDraft draft) {
        GrammarRuleEntity rule = lockedRule(id, draft.version());
        if (draft.type() == null) {
            throw invalid("type", "Choose a component type.");
        }
        UUID actor = authorization.requireAccess().userId();
        open(rule, actor, "component added", RULE);
        GrammarRuleComponentEntity component = new GrammarRuleComponentEntity();
        component.setId(Ids.random());
        component.setRuleId(id);
        component.setComponentType(draft.type());
        component.setHeading(optionalPhrase(draft.heading(), "heading", 160));
        component.setBody(phrase(draft.body(), "body", 4000));
        component.setDisplayOrder(order(draft.displayOrder()));
        components.save(component);
        finish(rules, rule, actor, AuditEventType.GRAMMAR_RULE_UPDATED, RULE);
        return ruleAdmin(rule);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.CITATION_MANAGE + "')")
    public RuleAdmin citeComponent(UUID ruleId, UUID componentId, long version, UUID citationId) {
        GrammarRuleEntity rule = lockedRule(ruleId, version);
        GrammarRuleComponentEntity component = components.findById(componentId).orElseThrow(() -> missing("Component"));
        if (!ruleId.equals(component.getRuleId())) {
            throw missing("Component");
        }
        requireCitation(citationId);
        UUID actor = authorization.requireAccess().userId();
        open(rule, actor, "exception citation linked", RULE);
        if (!componentCitations.existsByOwnerIdAndCitationId(componentId, citationId)) {
            componentCitations.save(new GrammarComponentCitationEntity(componentId, citationId));
        }
        finish(rules, rule, actor, AuditEventType.CITATION_ADDED, RULE);
        return ruleAdmin(rule);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_EXAMPLE_MANAGE + "')")
    public RuleAdmin addExample(UUID id, ExampleDraft draft) {
        GrammarRuleEntity rule = lockedRule(id, draft.version());
        ExampleConstraints.assertValid(draft.exampleType(), draft.citationId(), draft.surah(), draft.ayah(), draft.poet(), draft.workTitle());
        if (draft.citationId() != null) {
            requireCitation(draft.citationId());
        }
        if (draft.componentId() != null) {
            GrammarRuleComponentEntity component = components.findById(draft.componentId()).orElseThrow(() -> missing("Component"));
            if (!id.equals(component.getRuleId())) {
                throw missing("Component");
            }
        }
        if (draft.annotationId() != null && annotations.findById(draft.annotationId()).isEmpty()) {
            throw missing("Annotation");
        }
        UUID actor = authorization.requireAccess().userId();
        open(rule, actor, "example added", RULE);
        GrammarExampleEntity example = new GrammarExampleEntity();
        example.setId(Ids.random());
        example.setRuleId(id);
        example.setComponentId(draft.componentId());
        example.setTextOriginal(draft.textOriginal() == null ? "" : draft.textOriginal().trim());
        example.setTextNormalized(phrase(draft.textOriginal(), "textOriginal", 1000));
        example.setExplanation(optionalPhrase(draft.explanation(), "explanation", 1000));
        example.setExampleType(draft.exampleType());
        example.setCitationId(draft.citationId());
        example.setSurah(draft.surah());
        example.setAyah(draft.ayah());
        example.setPoet(optionalPhrase(draft.poet(), "poet", 160));
        example.setWorkTitle(optionalPhrase(draft.workTitle(), "workTitle", 160));
        example.setVerseLocator(blank(draft.verseLocator()));
        example.setAnnotationId(draft.annotationId());
        example.setDisplayOrder(order(draft.displayOrder()));
        examples.save(example);
        finish(rules, rule, actor, AuditEventType.GRAMMAR_EXAMPLE_ADDED, RULE);
        return ruleAdmin(rule);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_RULE_EDIT + "')")
    public RuleAdmin addRelation(UUID id, RelationDraft draft) {
        GrammarRuleEntity rule = lockedRule(id, draft.version());
        if (draft.type() == null || draft.targetRuleId() == null) {
            throw invalid("type", "Choose a relation and a target rule.");
        }
        if (id.equals(draft.targetRuleId()) || rules.findById(draft.targetRuleId()).isEmpty()) {
            throw invalid("targetRuleId", "Choose a different existing rule.");
        }
        if (relations.existsBySourceRuleIdAndTargetRuleIdAndRelationType(id, draft.targetRuleId(), draft.type())) {
            throw new ConflictException("That relation is already recorded.");
        }
        UUID actor = authorization.requireAccess().userId();
        open(rule, actor, "relation added", RULE);
        var relation = new com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarRuleRelationEntity();
        relation.setId(Ids.random());
        relation.setSourceRuleId(id);
        relation.setTargetRuleId(draft.targetRuleId());
        relation.setRelationType(draft.type());
        relations.save(relation);
        finish(rules, rule, actor, AuditEventType.GRAMMAR_RULE_UPDATED, RULE);
        return ruleAdmin(rule);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.CITATION_MANAGE + "')")
    public RuleAdmin citeRule(UUID id, long version, UUID citationId) {
        GrammarRuleEntity rule = lockedRule(id, version);
        requireCitation(citationId);
        UUID actor = authorization.requireAccess().userId();
        open(rule, actor, "citation linked", RULE);
        if (!ruleCitations.existsByOwnerIdAndCitationId(id, citationId)) {
            ruleCitations.save(new GrammarRuleCitationEntity(id, citationId));
        }
        finish(rules, rule, actor, AuditEventType.CITATION_ADDED, RULE);
        return ruleAdmin(rule);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_RULE_SUBMIT + "')")
    public RuleAdmin submitRule(UUID id, long version) {
        return moveRule(id, version, EditorialWorkflow::submit, AuditEventType.GRAMMAR_CONTENT_SUBMITTED, false, null);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_RULE_REVIEW + "')")
    public RuleAdmin requestRuleChanges(UUID id, long version, String reason) {
        requireReason(reason);
        return moveRule(id, version, EditorialWorkflow::requestChanges, AuditEventType.GRAMMAR_CONTENT_CHANGES_REQUESTED, true, reason.trim());
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_RULE_REVIEW + "')")
    public RuleAdmin verifyRule(UUID id, long version) {
        return moveRule(id, version, EditorialWorkflow::verify, AuditEventType.GRAMMAR_CONTENT_VERIFIED, true, null);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_RULE_PUBLISH + "')")
    public RuleAdmin publishRule(UUID id, long version) {
        searchIndexer.lock();
        GrammarRuleEntity rule = lockedRule(id, version);
        UUID actor = authorization.requireAccess().userId();
        separatePublisher(rule, actor);
        List<GrammarRuleComponentEntity> parts = components.findByRuleIdOrderByDisplayOrderAsc(id);
        if (parts.isEmpty()) {
            throw new ConflictException("A published rule needs at least one structured component.");
        }
        List<UUID> citationIds = new ArrayList<>(citationIds(ruleCitations.findByOwnerId(id)));
        if (citationIds.isEmpty()) {
            throw new ConflictException("A published rule needs at least one citation.");
        }
        List<UUID> componentIds = parts.stream().map(GrammarRuleComponentEntity::getId).toList();
        citationIds.addAll(componentCitations.findByOwnerIdIn(componentIds).stream().map(GrammarComponentCitationEntity::getCitationId).toList());
        List<GrammarExampleEntity> ruleExamples = examples.findByRuleIdOrderByDisplayOrderAsc(id);
        for (GrammarExampleEntity example : ruleExamples) {
            if (example.getExampleType().citationRequired() && example.getCitationId() == null) {
                throw new ConflictException("A quoted example needs a citation.");
            }
            if (example.getCitationId() != null) {
                citationIds.add(example.getCitationId());
            }
        }
        assertPublishable(citationIds);
        com.mrsoft.arabicreference.linguistics.application.PublicationChecks.assertNoOpenBlocker("GRAMMAR_RULE", rule.getId());
        rule.setStatus(EditorialWorkflow.publish(rule.getStatus()));
        rule.setPublishedTitle(rule.getTitleOriginal());
        rule.setPublishedNormalized(rule.getTitleNormalized());
        rule.setPublishedSummary(rule.getSummary());
        rule.setPublishedTopicId(rule.getTopicId());
        rule.setPublishedDisplayOrder(rule.getDisplayOrder());
        rule.setPublishedSnapshot(ruleSnapshot(rule, parts, ruleExamples));
        finish(rules, rule, actor, AuditEventType.GRAMMAR_CONTENT_PUBLISHED, RULE);
        searchIndexer.onRulePublished(rule);
        return ruleAdmin(rule);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_RULE_ARCHIVE + "')")
    public RuleAdmin archiveRule(UUID id, long version) {
        searchIndexer.lock();
        GrammarRuleEntity rule = lockedRule(id, version);
        rule.setStatus(EditorialWorkflow.archive(rule.getStatus()));
        clearRulePublication(rule);
        finish(rules, rule, authorization.requireAccess().userId(), AuditEventType.GRAMMAR_CONTENT_ARCHIVED, RULE);
        searchIndexer.onRuleArchived(rule.getId());
        return ruleAdmin(rule);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_RULE_VIEW + "')")
    public PageResult<ExampleSummary> examples(int page, int size) {
        Page<GrammarExampleEntity> result = examples.findAllByOrderByDisplayOrderAsc(page(page, size));
        List<UUID> ruleIds = result.map(GrammarExampleEntity::getRuleId).stream().distinct().toList();
        Map<UUID, String> titles = new HashMap<>();
        rules.findAllById(ruleIds).forEach(rule -> titles.put(rule.getId(), rule.getTitleOriginal()));
        return new PageResult<>(result.map(example -> new ExampleSummary(example.getId(), example.getRuleId(), titles.get(example.getRuleId()), example.getTextOriginal(), example.getExampleType())).toList(), page, size, result.getTotalElements());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_CONCEPT_VIEW + "')")
    public PageResult<ConceptSummary> concepts(int page, int size) {
        Page<GrammarConceptEntity> result = concepts.findAllByOrderByUpdatedAtDesc(page(page, size));
        return new PageResult<>(result.map(concept -> new ConceptSummary(concept.getId(), concept.getTermOriginal(), concept.getSlug(), concept.getStatus(), concept.getVersion())).toList(), page, size, result.getTotalElements());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_CONCEPT_VIEW + "')")
    public ConceptAdmin concept(UUID id) {
        return conceptAdmin(concepts.findById(id).orElseThrow(() -> missing("Concept")));
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_CONCEPT_MANAGE + "')")
    public ConceptAdmin createConcept(ConceptDraft draft) {
        UUID actor = authorization.requireAccess().userId();
        GrammarConceptEntity concept = new GrammarConceptEntity();
        concept.setId(Ids.random());
        applyConcept(concept, draft.term(), draft.shortDefinition(), draft.detailedDefinition());
        concept.setSlug(ContentSlugs.of(concept.getTermNormalized(), concept.getId()));
        concept.setStatus(PublicationStatus.DRAFT);
        stamp(concept, actor, true);
        finish(concepts, concept, actor, AuditEventType.GRAMMAR_CONCEPT_CREATED, CONCEPT);
        return conceptAdmin(concept);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_CONCEPT_MANAGE + "')")
    public ConceptAdmin updateConcept(UUID id, ConceptUpdate update) {
        GrammarConceptEntity concept = lockedConcept(id, update.version());
        UUID actor = authorization.requireAccess().userId();
        open(concept, actor, "concept edited", CONCEPT);
        applyConcept(concept, update.term(), update.shortDefinition(), update.detailedDefinition());
        finish(concepts, concept, actor, AuditEventType.GRAMMAR_CONCEPT_CREATED, CONCEPT);
        return conceptAdmin(concept);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_CONCEPT_MANAGE + "')")
    public ConceptAdmin addAlias(UUID id, AliasDraft draft) {
        GrammarConceptEntity concept = lockedConcept(id, draft.version());
        String normalized = phrase(draft.alias(), "alias", 160);
        if (aliases.existsByConceptIdAndAliasNormalized(id, normalized)) {
            throw new ConflictException("That alias is already recorded.");
        }
        UUID actor = authorization.requireAccess().userId();
        open(concept, actor, "alias added", CONCEPT);
        GrammarConceptAliasEntity alias = new GrammarConceptAliasEntity();
        alias.setId(Ids.random());
        alias.setConceptId(id);
        alias.setAliasOriginal(draft.alias().trim());
        alias.setAliasNormalized(normalized);
        aliases.save(alias);
        finish(concepts, concept, actor, AuditEventType.GRAMMAR_CONCEPT_CREATED, CONCEPT);
        return conceptAdmin(concept);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_CONCEPT_MANAGE + "')")
    public ConceptAdmin linkConceptRule(UUID id, ConceptRuleRequest request) {
        GrammarConceptEntity concept = lockedConcept(id, request.version());
        if (request.ruleId() == null || rules.findById(request.ruleId()).isEmpty()) {
            throw missing("Rule");
        }
        if (conceptRules.existsByConceptIdAndRuleId(id, request.ruleId())) {
            throw new ConflictException("That rule is already linked.");
        }
        UUID actor = authorization.requireAccess().userId();
        open(concept, actor, "rule linked", CONCEPT);
        conceptRules.save(new GrammarConceptRuleEntity(id, request.ruleId()));
        finish(concepts, concept, actor, AuditEventType.GRAMMAR_CONCEPT_CREATED, CONCEPT);
        return conceptAdmin(concept);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.CITATION_MANAGE + "')")
    public ConceptAdmin citeConcept(UUID id, long version, UUID citationId) {
        GrammarConceptEntity concept = lockedConcept(id, version);
        requireCitation(citationId);
        UUID actor = authorization.requireAccess().userId();
        open(concept, actor, "citation linked", CONCEPT);
        if (!conceptCitations.existsByOwnerIdAndCitationId(id, citationId)) {
            conceptCitations.save(new GrammarConceptCitationEntity(id, citationId));
        }
        finish(concepts, concept, actor, AuditEventType.CITATION_ADDED, CONCEPT);
        return conceptAdmin(concept);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_RULE_SUBMIT + "')")
    public ConceptAdmin submitConcept(UUID id, long version) {
        return moveConcept(id, version, EditorialWorkflow::submit, AuditEventType.GRAMMAR_CONTENT_SUBMITTED, false, null);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_RULE_REVIEW + "')")
    public ConceptAdmin requestConceptChanges(UUID id, long version, String reason) {
        requireReason(reason);
        return moveConcept(id, version, EditorialWorkflow::requestChanges, AuditEventType.GRAMMAR_CONTENT_CHANGES_REQUESTED, true, reason.trim());
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_RULE_REVIEW + "')")
    public ConceptAdmin verifyConcept(UUID id, long version) {
        return moveConcept(id, version, EditorialWorkflow::verify, AuditEventType.GRAMMAR_CONTENT_VERIFIED, true, null);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_RULE_PUBLISH + "')")
    public ConceptAdmin publishConcept(UUID id, long version) {
        searchIndexer.lock();
        GrammarConceptEntity concept = lockedConcept(id, version);
        UUID actor = authorization.requireAccess().userId();
        separatePublisher(concept, actor);
        if (concept.getShortDefinition() == null || concept.getShortDefinition().isBlank() || concept.getDetailedDefinition() == null || concept.getDetailedDefinition().isBlank()) {
            throw invalid("shortDefinition", "A published concept needs a short definition and a detailed definition.");
        }
        List<UUID> citationIds = citationIds(conceptCitations.findByOwnerId(id));
        if (citationIds.isEmpty()) {
            throw new ConflictException("A published concept needs at least one citation.");
        }
        assertPublishable(citationIds);
        com.mrsoft.arabicreference.linguistics.application.PublicationChecks.assertNoOpenBlocker("GRAMMAR_CONCEPT", concept.getId());
        concept.setStatus(EditorialWorkflow.publish(concept.getStatus()));
        concept.setPublishedTitle(concept.getTermOriginal());
        concept.setPublishedNormalized(concept.getTermNormalized());
        concept.setPublishedSummary(concept.getShortDefinition());
        for (GrammarConceptAliasEntity alias : aliases.findByConceptIdOrderByAliasOriginalAsc(id)) {
            alias.setPublishedNormalized(alias.getAliasNormalized());
        }
        concept.setPublishedSnapshot(conceptSnapshot(concept, citationIds));
        finish(concepts, concept, actor, AuditEventType.GRAMMAR_CONTENT_PUBLISHED, CONCEPT);
        searchIndexer.onConceptPublished(concept);
        return conceptAdmin(concept);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_RULE_ARCHIVE + "')")
    public ConceptAdmin archiveConcept(UUID id, long version) {
        searchIndexer.lock();
        GrammarConceptEntity concept = lockedConcept(id, version);
        concept.setStatus(EditorialWorkflow.archive(concept.getStatus()));
        concept.setPublishedSnapshot(null);
        concept.setPublishedTitle(null);
        concept.setPublishedNormalized(null);
        concept.setPublishedSummary(null);
        for (GrammarConceptAliasEntity alias : aliases.findByConceptIdOrderByAliasOriginalAsc(id)) {
            alias.setPublishedNormalized(null);
        }
        finish(concepts, concept, authorization.requireAccess().userId(), AuditEventType.GRAMMAR_CONTENT_ARCHIVED, CONCEPT);
        searchIndexer.onConceptArchived(concept.getId());
        return conceptAdmin(concept);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_RULE_VIEW + "') or @authz.has('" + PermissionCatalog.GRAMMAR_ANNOTATION_MANAGE + "')")
    public AnnotationAdmin annotation(UUID id) {
        return annotationAdmin(annotations.findById(id).orElseThrow(() -> missing("Annotation")));
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_ANNOTATION_MANAGE + "')")
    public AnnotationAdmin createAnnotation(AnnotationDraft draft) {
        if (draft == null || draft.sentence() == null || draft.sentence().isBlank()) {
            throw invalid("sentence", "A sentence is required.");
        }
        if (draft.citationId() != null) {
            requireCitation(draft.citationId());
        }
        UUID actor = authorization.requireAccess().userId();
        GrammarAnnotationEntity annotation = new GrammarAnnotationEntity();
        annotation.setId(Ids.random());
        annotation.setSentenceOriginal(draft.sentence().trim());
        annotation.setSentenceNormalized(phrase(draft.sentence(), "sentence", 500));
        annotation.setCitationId(draft.citationId());
        annotation.setStatus(PublicationStatus.DRAFT);
        stamp(annotation, actor, true);
        finish(annotations, annotation, actor, AuditEventType.GRAMMAR_ANNOTATION_CREATED, ANNOTATION);
        return annotationAdmin(annotation);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_ANNOTATION_MANAGE + "')")
    public AnnotationAdmin replaceTokens(UUID id, TokensRequest request) {
        GrammarAnnotationEntity annotation = lockedAnnotation(id, request.version());
        List<TokenInput> inputs = request.tokens() == null ? List.of() : request.tokens();
        AnnotationConstraints.assertPositions(inputs.stream().map(TokenInput::position).toList());
        UUID actor = authorization.requireAccess().userId();
        open(annotation, actor, "tokens edited", ANNOTATION);
        tokens.deleteByAnnotationId(id);
        entityManager.flush();
        for (TokenInput input : inputs) {
            GrammarRoleEntity role = null;
            if (input.roleCode() != null && !input.roleCode().isBlank()) {
                role = roles.findByCode(input.roleCode()).filter(GrammarRoleEntity::isActive).orElseThrow(() -> invalid("roleCode", "Choose an active grammatical role."));
                AnnotationConstraints.assertCompatible(role.getStateKind(), input.grammaticalState());
            }
            if (input.lexicalEntryId() != null && !links.knownEntry(input.lexicalEntryId())) {
                throw missing("Lexical entry");
            }
            if (input.morphologyAnalysisId() != null && !links.knownMorphology(input.morphologyAnalysisId())) {
                throw missing("Morphology analysis");
            }
            GrammarTokenEntity token = new GrammarTokenEntity();
            token.setId(Ids.random());
            token.setAnnotationId(id);
            token.setSurface(input.surface() == null ? "" : input.surface().trim());
            token.setNormalized(phrase(input.surface(), "surface", 80));
            token.setPosition(input.position());
            token.setLexicalEntryId(input.lexicalEntryId());
            token.setMorphologyAnalysisId(input.morphologyAnalysisId());
            token.setRoleCode(role == null ? null : role.getCode());
            token.setGrammaticalState(input.grammaticalState());
            token.setExplanation(optionalPhrase(input.explanation(), "explanation", 500));
            tokens.save(token);
        }
        finish(annotations, annotation, actor, AuditEventType.GRAMMAR_ANNOTATION_CREATED, ANNOTATION);
        return annotationAdmin(annotation);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_ANNOTATION_MANAGE + "')")
    public AnnotationAdmin replaceDependencies(UUID id, DependenciesRequest request) {
        GrammarAnnotationEntity annotation = lockedAnnotation(id, request.version());
        List<Integer> positions = tokens.findByAnnotationIdOrderByPositionAsc(id).stream().map(GrammarTokenEntity::getPosition).toList();
        List<DependencyInput> edges = request.edges() == null ? List.of() : request.edges();
        UUID actor = authorization.requireAccess().userId();
        open(annotation, actor, "dependencies edited", ANNOTATION);
        dependencies.deleteByAnnotationId(id);
        entityManager.flush();
        for (DependencyInput edge : edges) {
            if (edge.governorPosition() == null || edge.dependentPosition() == null || edge.governorPosition().equals(edge.dependentPosition()) || edge.governorPosition() < 0 || edge.dependentPosition() < 0) {
                throw invalid("governorPosition", "A dependency connects two different token positions.");
            }
            if (!positions.contains(edge.governorPosition()) || !positions.contains(edge.dependentPosition())) {
                throw invalid("governorPosition", "Both positions must already be tokens of this sentence.");
            }
            if (edge.label() == null || edge.label().isBlank() || edge.label().length() > 80) {
                throw invalid("label", "A dependency needs a short label.");
            }
            GrammarDependencyEntity dependency = new GrammarDependencyEntity();
            dependency.setId(Ids.random());
            dependency.setAnnotationId(id);
            dependency.setGovernorPosition(edge.governorPosition());
            dependency.setDependentPosition(edge.dependentPosition());
            dependency.setRelationLabel(edge.label().trim());
            dependencies.save(dependency);
        }
        finish(annotations, annotation, actor, AuditEventType.GRAMMAR_ANNOTATION_CREATED, ANNOTATION);
        return annotationAdmin(annotation);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_RULE_SUBMIT + "')")
    public AnnotationAdmin submitAnnotation(UUID id, long version) {
        return moveAnnotation(id, version, EditorialWorkflow::submit, AuditEventType.GRAMMAR_CONTENT_SUBMITTED, false, null);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_RULE_REVIEW + "')")
    public AnnotationAdmin requestAnnotationChanges(UUID id, long version, String reason) {
        requireReason(reason);
        return moveAnnotation(id, version, EditorialWorkflow::requestChanges, AuditEventType.GRAMMAR_CONTENT_CHANGES_REQUESTED, true, reason.trim());
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_RULE_REVIEW + "')")
    public AnnotationAdmin verifyAnnotation(UUID id, long version) {
        return moveAnnotation(id, version, EditorialWorkflow::verify, AuditEventType.GRAMMAR_CONTENT_VERIFIED, true, null);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_RULE_PUBLISH + "')")
    public AnnotationAdmin publishAnnotation(UUID id, long version) {
        GrammarAnnotationEntity annotation = lockedAnnotation(id, version);
        UUID actor = authorization.requireAccess().userId();
        separatePublisher(annotation, actor);
        if (annotation.getCitationId() != null) {
            assertPublishable(List.of(annotation.getCitationId()));
        }
        annotation.setStatus(EditorialWorkflow.publish(annotation.getStatus()));
        annotation.setPublishedSnapshot(annotationSnapshot(annotation));
        finish(annotations, annotation, actor, AuditEventType.GRAMMAR_CONTENT_PUBLISHED, ANNOTATION);
        return annotationAdmin(annotation);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_RULE_ARCHIVE + "')")
    public AnnotationAdmin archiveAnnotation(UUID id, long version) {
        GrammarAnnotationEntity annotation = lockedAnnotation(id, version);
        annotation.setStatus(EditorialWorkflow.archive(annotation.getStatus()));
        annotation.setPublishedSnapshot(null);
        finish(annotations, annotation, authorization.requireAccess().userId(), AuditEventType.GRAMMAR_CONTENT_ARCHIVED, ANNOTATION);
        return annotationAdmin(annotation);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_RULE_VIEW + "') or @authz.has('" + PermissionCatalog.GRAMMAR_ANNOTATION_MANAGE + "')")
    public List<RoleView> roles() {
        return roles.findAllByOrderByLabelArAsc().stream().map(role -> new RoleView(role.getId(), role.getCode(), role.getLabelAr(), role.getStateKind(), role.isActive())).toList();
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_ANNOTATION_MANAGE + "')")
    public RoleView createRole(RoleDraft draft) {
        if (draft == null || draft.code() == null || !draft.code().matches("[A-Z0-9_]{2,40}") || draft.stateKind() == null) {
            throw invalid("code", "Use a stable uppercase role code.");
        }
        if (roles.findByCode(draft.code()).isPresent()) {
            throw new ConflictException("A grammatical role with this code already exists.");
        }
        GrammarRoleEntity role = new GrammarRoleEntity();
        role.setId(Ids.random());
        role.setCode(draft.code());
        role.setLabelAr(phrase(draft.labelAr(), "labelAr", 80));
        role.setStateKind(draft.stateKind());
        role.setActive(true);
        roles.save(role);
        return new RoleView(role.getId(), role.getCode(), role.getLabelAr(), role.getStateKind(), true);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.GRAMMAR_RULE_VIEW + "')")
    public PageResult<ReviewItem> review(String status, int page, int size) {
        PublicationStatus wanted;
        try {
            wanted = status == null || status.isBlank() ? PublicationStatus.IN_REVIEW : PublicationStatus.valueOf(status);
        } catch (IllegalArgumentException exception) {
            throw invalid("status", "Choose a workflow status.");
        }
        int bounded = boundedSize(page, size);
        String name = wanted.name();
        Long total = jdbc.queryForObject("""
                select count(*) from (
                    select id from grammar_topic where status = ?
                    union all select id from grammar_rule where status = ?
                    union all select id from grammar_concept where status = ?
                    union all select id from grammar_annotation where status = ?
                ) items
                """, Long.class, name, name, name, name);
        List<ReviewItem> items = jdbc.query("""
                select kind, id, title, status, version from (
                    select 'TOPIC' as kind, id, title_original as title, status, version, updated_at from grammar_topic where status = ?
                    union all
                    select 'RULE', id, title_original, status, version, updated_at from grammar_rule where status = ?
                    union all
                    select 'CONCEPT', id, term_original, status, version, updated_at from grammar_concept where status = ?
                    union all
                    select 'ANNOTATION', id, sentence_original, status, version, updated_at from grammar_annotation where status = ?
                ) items
                order by updated_at desc
                limit ? offset ?
                """, (row, index) -> new ReviewItem(row.getString("kind"), row.getObject("id", UUID.class), row.getString("title"), row.getString("status"), row.getLong("version")), name, name, name, name, bounded, page * bounded);
        return new PageResult<>(items, page, bounded, total == null ? 0 : total);
    }

    private TopicAdmin moveTopic(UUID id, long version, Function<PublicationStatus, PublicationStatus> transition, AuditEventType event, boolean reviewer, String reason) {
        GrammarTopicEntity topic = lockedTopic(id, version);
        applyTransition(topic, transition, reviewer, reason);
        finish(topics, topic, authorization.requireAccess().userId(), event, TOPIC);
        return topicAdmin(topic);
    }

    private RuleAdmin moveRule(UUID id, long version, Function<PublicationStatus, PublicationStatus> transition, AuditEventType event, boolean reviewer, String reason) {
        GrammarRuleEntity rule = lockedRule(id, version);
        applyTransition(rule, transition, reviewer, reason);
        finish(rules, rule, authorization.requireAccess().userId(), event, RULE);
        return ruleAdmin(rule);
    }

    private ConceptAdmin moveConcept(UUID id, long version, Function<PublicationStatus, PublicationStatus> transition, AuditEventType event, boolean reviewer, String reason) {
        GrammarConceptEntity concept = lockedConcept(id, version);
        applyTransition(concept, transition, reviewer, reason);
        finish(concepts, concept, authorization.requireAccess().userId(), event, CONCEPT);
        return conceptAdmin(concept);
    }

    private AnnotationAdmin moveAnnotation(UUID id, long version, Function<PublicationStatus, PublicationStatus> transition, AuditEventType event, boolean reviewer, String reason) {
        GrammarAnnotationEntity annotation = lockedAnnotation(id, version);
        applyTransition(annotation, transition, reviewer, reason);
        finish(annotations, annotation, authorization.requireAccess().userId(), event, ANNOTATION);
        return annotationAdmin(annotation);
    }

    private void applyTransition(EditorialRecord record, Function<PublicationStatus, PublicationStatus> transition, boolean reviewer, String reason) {
        UUID actor = authorization.requireAccess().userId();
        if (reviewer) {
            EditorialGuards.requireDifferentPerson(record.getCreatedBy(), actor, "The creator cannot review their own grammar content.");
            record.setReviewedBy(actor);
            record.setChangeReason(reason);
        }
        record.setStatus(transition.apply(record.getStatus()));
    }

    private void separatePublisher(EditorialRecord record, UUID actor) {
        EditorialGuards.requireDifferentPerson(record.getCreatedBy(), actor, "The creator cannot publish their own grammar content.");
        EditorialGuards.requireDifferentPerson(record.getReviewedBy(), actor, "The reviewer cannot publish the same grammar content.");
    }

    private GrammarTopicEntity lockHierarchy(UUID id, long version, UUID parentId) {
        List<UUID> chain = new ArrayList<>();
        chain.add(id);
        UUID cursor = parentId;
        int guard = 0;
        while (cursor != null && !chain.contains(cursor)) {
            chain.add(cursor);
            cursor = topics.findById(cursor).map(GrammarTopicEntity::getParentId).orElse(null);
            if (++guard > 64) {
                throw new ConflictException("The topic hierarchy is too deep.");
            }
        }
        List<UUID> ordered = new ArrayList<>(chain);
        ordered.sort(Comparator.naturalOrder());
        for (UUID lockId : ordered) {
            topics.lockById(lockId).orElseThrow(() -> missing("Topic"));
        }
        GrammarTopicEntity topic = topics.findById(id).orElseThrow(() -> missing("Topic"));
        if (topic.getVersion() != version) {
            throw stale();
        }
        if (parentId != null && topics.findById(parentId).isEmpty()) {
            throw missing("Topic");
        }
        TopicGraph.assertNoParentCycle(id, parentId, candidate -> topics.findById(candidate).map(GrammarTopicEntity::getParentId));
        return topic;
    }

    private Map<String, Object> topicSnapshot(GrammarTopicEntity topic, List<UUID> citationIds) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("title", topic.getTitleOriginal());
        snapshot.put("slug", topic.getSlug());
        snapshot.put("summary", topic.getSummary());
        snapshot.put("category", topic.getCategory().name());
        snapshot.put("categoryLabel", topic.getCategory().arabicLabel());
        snapshot.put("difficulty", topic.getDifficulty() == null ? null : topic.getDifficulty().name());
        snapshot.put("difficultyLabel", topic.getDifficulty() == null ? null : topic.getDifficulty().arabicLabel());
        List<Map<String, Object>> required = new ArrayList<>();
        for (GrammarPrerequisiteEntity link : prerequisites.findByTopicId(topic.getId())) {
            GrammarTopicEntity other = topics.findById(link.getRequiredTopicId()).orElse(null);
            if (other != null && other.getPublishedSnapshot() != null && other.getStatus() != PublicationStatus.ARCHIVED) {
                required.add(linkMap(other.getPublishedTitle(), other.getSlug(), other.getPublishedSummary()));
            }
        }
        snapshot.put("prerequisites", required);
        snapshot.put("sources", sourceMaps(citationIds));
        return snapshot;
    }

    private Map<String, Object> ruleSnapshot(GrammarRuleEntity rule, List<GrammarRuleComponentEntity> parts, List<GrammarExampleEntity> ruleExamples) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("title", rule.getTitleOriginal());
        snapshot.put("slug", rule.getSlug());
        snapshot.put("summary", rule.getSummary());
        snapshot.put("ruleText", rule.getRuleText());
        snapshot.put("difficulty", rule.getDifficulty() == null ? null : rule.getDifficulty().name());
        snapshot.put("difficultyLabel", rule.getDifficulty() == null ? null : rule.getDifficulty().arabicLabel());
        GrammarTopicEntity topic = topics.findById(rule.getTopicId()).orElse(null);
        if (topic != null && topic.getPublishedSnapshot() != null && topic.getStatus() != PublicationStatus.ARCHIVED) {
            snapshot.put("topic", linkMap(topic.getPublishedTitle(), topic.getSlug(), topic.getPublishedSummary()));
        }
        List<UUID> componentIds = parts.stream().map(GrammarRuleComponentEntity::getId).toList();
        Map<UUID, List<UUID>> componentCitationIds = new HashMap<>();
        if (!componentIds.isEmpty()) {
            for (GrammarComponentCitationEntity link : componentCitations.findByOwnerIdIn(componentIds)) {
                componentCitationIds.computeIfAbsent(link.getOwnerId(), key -> new ArrayList<>()).add(link.getCitationId());
            }
        }
        List<Map<String, Object>> componentMaps = new ArrayList<>();
        for (GrammarRuleComponentEntity component : parts) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("type", component.getComponentType().name());
            map.put("typeLabel", component.getComponentType().arabicLabel());
            map.put("heading", component.getHeading());
            map.put("body", component.getBody());
            map.put("sources", sourceMaps(componentCitationIds.getOrDefault(component.getId(), List.of())));
            componentMaps.add(map);
        }
        snapshot.put("components", componentMaps);
        List<Map<String, Object>> exampleMaps = new ArrayList<>();
        for (GrammarExampleEntity example : ruleExamples) {
            exampleMaps.add(exampleMap(example));
        }
        snapshot.put("examples", exampleMaps);
        List<Map<String, Object>> relationMaps = new ArrayList<>();
        for (var relation : relations.findBySourceRuleId(rule.getId())) {
            GrammarRuleEntity target = rules.findById(relation.getTargetRuleId()).orElse(null);
            if (target != null && target.getPublishedSnapshot() != null && target.getStatus() != PublicationStatus.ARCHIVED) {
                Map<String, Object> map = linkMap(target.getPublishedTitle(), target.getSlug(), target.getPublishedSummary());
                map.put("type", relation.getRelationType().name());
                map.put("typeLabel", relation.getRelationType().arabicLabel());
                relationMaps.add(map);
            }
        }
        snapshot.put("relations", relationMaps);
        List<Map<String, Object>> conceptMaps = new ArrayList<>();
        for (GrammarConceptRuleEntity link : conceptRules.findByRuleId(rule.getId())) {
            GrammarConceptEntity concept = concepts.findById(link.getConceptId()).orElse(null);
            if (concept != null && concept.getPublishedSnapshot() != null && concept.getStatus() != PublicationStatus.ARCHIVED) {
                conceptMaps.add(linkMap(concept.getPublishedTitle(), concept.getSlug(), concept.getShortDefinition()));
            }
        }
        snapshot.put("concepts", conceptMaps);
        snapshot.put("sources", sourceMaps(citationIds(ruleCitations.findByOwnerId(rule.getId()))));
        return snapshot;
    }

    private Map<String, Object> exampleMap(GrammarExampleEntity example) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("textOriginal", example.getTextOriginal());
        map.put("explanation", example.getExplanation());
        map.put("exampleType", example.getExampleType().name());
        map.put("exampleTypeLabel", example.getExampleType().arabicLabel());
        map.put("editorial", example.getExampleType().editorial());
        map.put("editorialNote", example.getExampleType().editorial() ? ExampleConstraints.EDITORIAL_NOTE : null);
        map.put("surah", example.getSurah());
        map.put("ayah", example.getAyah());
        map.put("poet", example.getPoet());
        map.put("workTitle", example.getWorkTitle());
        map.put("verseLocator", example.getVerseLocator());
        map.put("source", example.getCitationId() == null ? null : sourceMaps(List.of(example.getCitationId())).stream().findFirst().orElse(null));
        GrammarAnnotationEntity annotation = example.getAnnotationId() == null ? null : annotations.findById(example.getAnnotationId()).orElse(null);
        if (annotation != null && annotation.getPublishedSnapshot() != null && annotation.getStatus() != PublicationStatus.ARCHIVED) {
            map.put("tokens", annotation.getPublishedSnapshot().get("tokens"));
            map.put("annotationNote", null);
        } else if (example.getAnnotationId() != null) {
            map.put("tokens", List.of());
            map.put("annotationNote", ExampleConstraints.MISSING_ANALYSIS);
        } else {
            map.put("tokens", List.of());
            map.put("annotationNote", null);
        }
        return map;
    }

    private Map<String, Object> conceptSnapshot(GrammarConceptEntity concept, List<UUID> citationIds) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("term", concept.getTermOriginal());
        snapshot.put("slug", concept.getSlug());
        snapshot.put("shortDefinition", concept.getShortDefinition());
        snapshot.put("detailedDefinition", concept.getDetailedDefinition());
        snapshot.put("aliases", aliases.findByConceptIdOrderByAliasOriginalAsc(concept.getId()).stream().map(GrammarConceptAliasEntity::getAliasOriginal).toList());
        List<Map<String, Object>> linked = new ArrayList<>();
        for (GrammarConceptRuleEntity link : conceptRules.findByConceptId(concept.getId())) {
            GrammarRuleEntity rule = rules.findById(link.getRuleId()).orElse(null);
            if (rule != null && rule.getPublishedSnapshot() != null && rule.getStatus() != PublicationStatus.ARCHIVED) {
                linked.add(linkMap(rule.getPublishedTitle(), rule.getSlug(), rule.getPublishedSummary()));
            }
        }
        snapshot.put("rules", linked);
        snapshot.put("sources", sourceMaps(citationIds));
        return snapshot;
    }

    private Map<String, Object> annotationSnapshot(GrammarAnnotationEntity annotation) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("sentence", annotation.getSentenceOriginal());
        snapshot.put("source", annotation.getCitationId() == null ? null : sourceMaps(List.of(annotation.getCitationId())).stream().findFirst().orElse(null));
        List<Map<String, Object>> tokenMaps = new ArrayList<>();
        for (GrammarTokenEntity token : tokens.findByAnnotationIdOrderByPositionAsc(annotation.getId())) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("surface", token.getSurface());
            map.put("position", token.getPosition());
            GrammarRoleEntity role = token.getRoleCode() == null ? null : roles.findByCode(token.getRoleCode()).orElse(null);
            map.put("roleLabel", role == null ? null : role.getLabelAr());
            map.put("stateLabel", token.getGrammaticalState() == null ? null : token.getGrammaticalState().arabicLabel());
            map.put("explanation", token.getExplanation());
            if (token.getLexicalEntryId() != null) {
                links.entry(token.getLexicalEntryId()).ifPresent(entry -> map.put("lexical", linkMap(entry.lemma(), entry.slug(), null)));
            }
            if (token.getMorphologyAnalysisId() != null) {
                links.morphology(token.getMorphologyAnalysisId()).ifPresent(reading -> {
                Map<String, Object> morphology = new LinkedHashMap<>();
                morphology.put("patternOriginal", reading.patternOriginal());
                morphology.put("label", "عرض التحليل الصرفي");
                map.put("morphology", morphology);
            });
            }
            tokenMaps.add(map);
        }
        snapshot.put("tokens", tokenMaps);
        snapshot.put("annotationNote", tokenMaps.isEmpty() ? ExampleConstraints.MISSING_ANALYSIS : null);
        List<Map<String, Object>> edges = new ArrayList<>();
        for (GrammarDependencyEntity edge : dependencies.findByAnnotationIdOrderByGovernorPositionAsc(annotation.getId())) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("governorPosition", edge.getGovernorPosition());
            map.put("dependentPosition", edge.getDependentPosition());
            map.put("label", edge.getRelationLabel());
            edges.add(map);
        }
        snapshot.put("dependencies", edges);
        return snapshot;
    }

    private Map<String, Object> linkMap(String title, String slug, String summary) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("title", title);
        map.put("slug", slug);
        map.put("summary", summary);
        return map;
    }

    private List<Map<String, Object>> sourceMaps(List<UUID> ids) {
        List<Map<String, Object>> maps = new ArrayList<>();
        for (CitationView citation : sources.citations(distinct(ids))) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("title", citation.title());
            map.put("author", citation.author());
            map.put("edition", citation.edition());
            map.put("publicationYear", citation.publicationYear());
            map.put("pageFrom", citation.pageFrom());
            map.put("pageTo", citation.pageTo());
            map.put("attributionText", citation.attributionText());
            maps.add(map);
        }
        return maps;
    }

    private void assertPublishable(List<UUID> ids) {
        List<UUID> distinct = distinct(ids);
        if (distinct.isEmpty()) {
            return;
        }
        List<CitationView> views = sources.citations(distinct);
        if (views.size() != distinct.size()) {
            throw new ResourceNotFoundException("Citation was not found.");
        }
        for (CitationView citation : views) {
            if (!"PUBLISHED".equals(citation.sourceStatus()) || !citation.publishableLicense()) {
                throw new ForbiddenOperationException("A published grammar record cannot cite a restricted, unknown, or unpublished source.");
            }
        }
    }

    private void requireCitation(UUID citationId) {
        if (citationId == null || sources.citations(List.of(citationId)).isEmpty()) {
            throw new ResourceNotFoundException("Citation was not found.");
        }
    }

    private void applyTopic(GrammarTopicEntity topic, String title, String summary, com.mrsoft.arabicreference.grammar.domain.GrammarCategory category, com.mrsoft.arabicreference.grammar.domain.DifficultyLevel difficulty, Integer displayOrder, boolean creating) {
        if (title == null) {
            throw invalid("title", "Enter Arabic text within the allowed length.");
        }
        String normalized = phrase(title, "title", 160);
        topic.setTitleOriginal(title.trim());
        topic.setTitleNormalized(normalized);
        topic.setSummary(optionalPhrase(summary, "summary", 1000));
        if (category == null) {
            throw invalid("category", "Choose a grammar category.");
        }
        topic.setCategory(category);
        topic.setDifficulty(difficulty);
        topic.setDisplayOrder(order(displayOrder));
        if (creating) {
            topic.setSlug(topic.getSlug());
        }
    }

    private void applyRule(GrammarRuleEntity rule, String title, String summary, String ruleText, com.mrsoft.arabicreference.grammar.domain.DifficultyLevel difficulty, Integer displayOrder) {
        if (title == null) {
            throw invalid("title", "Enter Arabic text within the allowed length.");
        }
        rule.setTitleOriginal(title.trim());
        rule.setTitleNormalized(phrase(title, "title", 160));
        rule.setSummary(optionalPhrase(summary, "summary", 1000));
        rule.setRuleText(optionalPhrase(ruleText, "ruleText", 4000));
        rule.setDifficulty(difficulty);
        rule.setDisplayOrder(order(displayOrder));
    }

    private void applyConcept(GrammarConceptEntity concept, String term, String shortDefinition, String detailedDefinition) {
        if (term == null) {
            throw invalid("term", "Enter Arabic text within the allowed length.");
        }
        concept.setTermOriginal(term.trim());
        concept.setTermNormalized(phrase(term, "term", 160));
        concept.setShortDefinition(optionalPhrase(shortDefinition, "shortDefinition", 500));
        concept.setDetailedDefinition(optionalPhrase(detailedDefinition, "detailedDefinition", 4000));
    }

    private void clearRulePublication(GrammarRuleEntity rule) {
        rule.setPublishedSnapshot(null);
        rule.setPublishedTitle(null);
        rule.setPublishedNormalized(null);
        rule.setPublishedSummary(null);
        rule.setPublishedTopicId(null);
        rule.setPublishedDisplayOrder(null);
    }

    private void open(EditorialRecord record, UUID actor, String reason, String type) {
        if (!EditorialWorkflow.editable(record.getStatus())) {
            throw new ConflictException("This record cannot be edited while it is " + record.getStatus() + ".");
        }
        if (record.getStatus() == PublicationStatus.PUBLISHED) {
            Map<String, Object> snapshot = record.getPublishedSnapshot() == null ? Map.of("status", "PUBLISHED") : new LinkedHashMap<>(record.getPublishedSnapshot());
            revisions.record(type, record.getId(), snapshot, actor, reason);
            record.setStatus(PublicationStatus.DRAFT);
            record.setReviewedBy(null);
        }
    }

    private <T extends EditorialRecord> T finish(JpaRepository<T, UUID> repository, T record, UUID actor, AuditEventType event, String target) {
        record.setUpdatedAt(timeProvider.now());
        record.setUpdatedBy(actor);
        T persisted = repository.saveAndFlush(record);
        if (!entityManager.contains(persisted)) {
            @SuppressWarnings("unchecked")
            Class<T> type = (Class<T>) record.getClass();
            persisted = entityManager.find(type, record.getId());
        }
        entityManager.refresh(persisted);
        if (persisted != record) {
            record.setVersion(persisted.getVersion());
            record.setStatus(persisted.getStatus());
        }
        audit.record(actor, event, target, record.getId().toString(), Map.of("status", record.getStatus().name()));
        return record;
    }

    private void stamp(EditorialRecord record, UUID actor, boolean creating) {
        var now = timeProvider.now();
        if (creating) {
            record.setCreatedAt(now);
            record.setCreatedBy(actor);
        }
        record.setUpdatedAt(now);
        record.setUpdatedBy(actor);
    }

    private GrammarTopicEntity lockedTopic(UUID id, long version) {
        GrammarTopicEntity topic = topics.lockById(id).orElseThrow(() -> missing("Topic"));
        if (topic.getVersion() != version) {
            throw stale();
        }
        return topic;
    }

    private GrammarRuleEntity lockedRule(UUID id, long version) {
        GrammarRuleEntity rule = rules.lockById(id).orElseThrow(() -> missing("Rule"));
        if (rule.getVersion() != version) {
            throw stale();
        }
        return rule;
    }

    private GrammarConceptEntity lockedConcept(UUID id, long version) {
        GrammarConceptEntity concept = concepts.lockById(id).orElseThrow(() -> missing("Concept"));
        if (concept.getVersion() != version) {
            throw stale();
        }
        return concept;
    }

    private GrammarAnnotationEntity lockedAnnotation(UUID id, long version) {
        GrammarAnnotationEntity annotation = annotations.lockById(id).orElseThrow(() -> missing("Annotation"));
        if (annotation.getVersion() != version) {
            throw stale();
        }
        return annotation;
    }

    private TopicSummary topicSummary(GrammarTopicEntity topic) {
        return new TopicSummary(topic.getId(), topic.getParentId(), topic.getTitleOriginal(), topic.getSlug(), topic.getCategory(), topic.getStatus(), topic.getDisplayOrder(), topic.getVersion());
    }

    private TopicAdmin topicAdmin(GrammarTopicEntity topic) {
        return new TopicAdmin(topic.getId(), topic.getParentId(), topic.getTitleOriginal(), topic.getSlug(), topic.getSummary(), topic.getCategory(), topic.getDifficulty(), topic.getDisplayOrder(), topic.getStatus(), topic.getVersion(), citationIds(topicCitations.findByOwnerId(topic.getId())), prerequisites.findByTopicId(topic.getId()).stream().map(GrammarPrerequisiteEntity::getRequiredTopicId).toList());
    }

    private RuleAdmin ruleAdmin(GrammarRuleEntity rule) {
        List<UUID> componentIds = components.findByRuleIdOrderByDisplayOrderAsc(rule.getId()).stream().map(GrammarRuleComponentEntity::getId).toList();
        Map<UUID, List<UUID>> citations = new HashMap<>();
        if (!componentIds.isEmpty()) {
            for (GrammarComponentCitationEntity link : componentCitations.findByOwnerIdIn(componentIds)) {
                citations.computeIfAbsent(link.getOwnerId(), key -> new ArrayList<>()).add(link.getCitationId());
            }
        }
        List<ComponentAdmin> componentViews = components.findByRuleIdOrderByDisplayOrderAsc(rule.getId()).stream()
                .map(component -> new ComponentAdmin(component.getId(), component.getComponentType(), component.getHeading(), component.getBody(), component.getDisplayOrder(), citations.getOrDefault(component.getId(), List.of())))
                .toList();
        List<ExampleAdmin> exampleViews = examples.findByRuleIdOrderByDisplayOrderAsc(rule.getId()).stream()
                .map(example -> new ExampleAdmin(example.getId(), example.getComponentId(), example.getTextOriginal(), example.getExplanation(), example.getExampleType(), example.getCitationId(), example.getSurah(), example.getAyah(), example.getPoet(), example.getWorkTitle(), example.getVerseLocator(), example.getAnnotationId(), example.getDisplayOrder()))
                .toList();
        List<RelationAdmin> relationViews = relations.findBySourceRuleId(rule.getId()).stream()
                .map(relation -> new RelationAdmin(relation.getId(), relation.getTargetRuleId(), relation.getRelationType()))
                .toList();
        return new RuleAdmin(rule.getId(), rule.getTopicId(), rule.getTitleOriginal(), rule.getSlug(), rule.getSummary(), rule.getRuleText(), rule.getDifficulty(), rule.getDisplayOrder(), rule.getStatus(), rule.getVersion(), componentViews, exampleViews, relationViews, citationIds(ruleCitations.findByOwnerId(rule.getId())), conceptRules.findByRuleId(rule.getId()).stream().map(GrammarConceptRuleEntity::getConceptId).toList());
    }

    private ConceptAdmin conceptAdmin(GrammarConceptEntity concept) {
        return new ConceptAdmin(
                concept.getId(),
                concept.getTermOriginal(),
                concept.getSlug(),
                concept.getShortDefinition(),
                concept.getDetailedDefinition(),
                concept.getStatus(),
                concept.getVersion(),
                aliases.findByConceptIdOrderByAliasOriginalAsc(concept.getId()).stream().map(alias -> new AliasAdmin(alias.getId(), alias.getAliasOriginal())).toList(),
                citationIds(conceptCitations.findByOwnerId(concept.getId())),
                conceptRules.findByConceptId(concept.getId()).stream().map(GrammarConceptRuleEntity::getRuleId).toList());
    }

    private AnnotationAdmin annotationAdmin(GrammarAnnotationEntity annotation) {
        List<TokenAdmin> tokenViews = tokens.findByAnnotationIdOrderByPositionAsc(annotation.getId()).stream()
                .map(token -> new TokenAdmin(token.getId(), token.getSurface(), token.getPosition(), token.getLexicalEntryId(), token.getMorphologyAnalysisId(), token.getRoleCode(), token.getGrammaticalState(), token.getExplanation()))
                .toList();
        List<DependencyAdmin> edges = dependencies.findByAnnotationIdOrderByGovernorPositionAsc(annotation.getId()).stream()
                .map(edge -> new DependencyAdmin(edge.getId(), edge.getGovernorPosition(), edge.getDependentPosition(), edge.getRelationLabel()))
                .toList();
        return new AnnotationAdmin(annotation.getId(), annotation.getSentenceOriginal(), annotation.getCitationId(), annotation.getStatus(), annotation.getVersion(), tokenViews, edges);
    }

    private List<UUID> citationIds(List<?> links) {
        List<UUID> ids = new ArrayList<>();
        for (Object link : links) {
            if (link instanceof GrammarTopicCitationEntity topicLink) {
                ids.add(topicLink.getCitationId());
            } else if (link instanceof GrammarRuleCitationEntity ruleLink) {
                ids.add(ruleLink.getCitationId());
            } else if (link instanceof GrammarConceptCitationEntity conceptLink) {
                ids.add(conceptLink.getCitationId());
            }
        }
        return ids;
    }

    private String phrase(String original, String field, int max) {
        if (original == null) {
            throw invalid(field, "Enter Arabic text within the allowed length.");
        }
        return ArabicPhrase.require(normalizer.normalize(original).normalizedText(), field, max);
    }

    private String optionalPhrase(String original, String field, int max) {
        if (original == null || original.isBlank()) {
            return null;
        }
        return phrase(original, field, max);
    }

    private static String blank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static int order(Integer value) {
        return value == null ? 0 : value;
    }

    private static List<UUID> distinct(List<UUID> ids) {
        return new ArrayList<>(new HashSet<>(ids));
    }

    private Pageable page(int page, int size) {
        return PageRequest.of(page, boundedSize(page, size));
    }

    private int boundedSize(int page, int size) {
        if (page < 0 || size < 1 || size > 50) {
            throw new ValidationException("Page request is invalid.", List.of(new FieldErrorDetail("page", "Page size must be from 1 to 50.")));
        }
        return size;
    }

    private static void requireReason(String reason) {
        if (reason == null || reason.isBlank()) {
            throw invalid("reason", "Explain what must change.");
        }
    }

    private static ConflictException stale() {
        return new ConflictException("The record was updated by someone else. Reload and try again.");
    }

    private static ResourceNotFoundException missing(String name) {
        return new ResourceNotFoundException(name + " was not found.");
    }

    private static ValidationException invalid(String field, String message) {
        return new ValidationException(message, List.of(new FieldErrorDetail(field, message)));
    }
}
