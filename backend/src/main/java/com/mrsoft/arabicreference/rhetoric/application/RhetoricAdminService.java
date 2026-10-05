package com.mrsoft.arabicreference.rhetoric.application;

import com.mrsoft.arabicreference.identity.application.AuthorizationService;
import com.mrsoft.arabicreference.identity.domain.AuditEventType;
import com.mrsoft.arabicreference.identity.domain.PermissionCatalog;
import com.mrsoft.arabicreference.linguistics.application.EditorialStore;
import com.mrsoft.arabicreference.linguistics.domain.editorial.PublicationStatus;
import com.mrsoft.arabicreference.linguistics.domain.text.ContentSlugs;
import com.mrsoft.arabicreference.linguistics.domain.text.KnowledgeText;
import com.mrsoft.arabicreference.rhetoric.domain.RhetoricCategory;
import com.mrsoft.arabicreference.rhetoric.domain.RhetoricComponentKind;
import com.mrsoft.arabicreference.rhetoric.domain.RhetoricExampleKind;
import com.mrsoft.arabicreference.rhetoric.domain.RhetoricExampleRules;
import com.mrsoft.arabicreference.rhetoric.domain.RhetoricRelationKind;
import com.mrsoft.arabicreference.rhetoric.infrastructure.persistence.RhetoricComponentEntity;
import com.mrsoft.arabicreference.rhetoric.infrastructure.persistence.RhetoricComponentRepository;
import com.mrsoft.arabicreference.rhetoric.infrastructure.persistence.RhetoricDeviceCitationEntity;
import com.mrsoft.arabicreference.rhetoric.infrastructure.persistence.RhetoricDeviceCitationRepository;
import com.mrsoft.arabicreference.rhetoric.infrastructure.persistence.RhetoricDeviceEntity;
import com.mrsoft.arabicreference.rhetoric.infrastructure.persistence.RhetoricDeviceRepository;
import com.mrsoft.arabicreference.rhetoric.infrastructure.persistence.RhetoricExampleEntity;
import com.mrsoft.arabicreference.rhetoric.infrastructure.persistence.RhetoricExampleRepository;
import com.mrsoft.arabicreference.rhetoric.infrastructure.persistence.RhetoricRelationEntity;
import com.mrsoft.arabicreference.rhetoric.infrastructure.persistence.RhetoricRelationRepository;
import com.mrsoft.arabicreference.rhetoric.infrastructure.persistence.RhetoricTopicCitationEntity;
import com.mrsoft.arabicreference.rhetoric.infrastructure.persistence.RhetoricTopicCitationRepository;
import com.mrsoft.arabicreference.rhetoric.infrastructure.persistence.RhetoricTopicEntity;
import com.mrsoft.arabicreference.rhetoric.infrastructure.persistence.RhetoricTopicRepository;
import com.mrsoft.arabicreference.shared.infrastructure.persistence.OwnerCitationEntity;
import com.mrsoft.arabicreference.shared.kernel.exception.ConflictException;
import com.mrsoft.arabicreference.shared.kernel.exception.FieldErrorDetail;
import com.mrsoft.arabicreference.shared.kernel.exception.ResourceNotFoundException;
import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import com.mrsoft.arabicreference.shared.kernel.id.Ids;
import com.mrsoft.arabicreference.source.application.CitationChecks;
import com.mrsoft.arabicreference.source.application.SourceLines;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RhetoricAdminService {

    public static final String TOPIC = "rhetoric_topic";
    public static final String DEVICE = "rhetoric_device";

    private final RhetoricTopicRepository topics;
    private final RhetoricDeviceRepository devices;
    private final RhetoricComponentRepository components;
    private final RhetoricExampleRepository examples;
    private final RhetoricRelationRepository relations;
    private final RhetoricTopicCitationRepository topicCitations;
    private final RhetoricDeviceCitationRepository deviceCitations;
    private final EditorialStore editorial;
    private final AuthorizationService authorization;
    private final CitationChecks citations;
    private final RhetoricSearchIndexer search;

    public RhetoricAdminService(
            RhetoricTopicRepository topics,
            RhetoricDeviceRepository devices,
            RhetoricComponentRepository components,
            RhetoricExampleRepository examples,
            RhetoricRelationRepository relations,
            RhetoricTopicCitationRepository topicCitations,
            RhetoricDeviceCitationRepository deviceCitations,
            EditorialStore editorial,
            AuthorizationService authorization,
            CitationChecks citations,
            RhetoricSearchIndexer search) {
        this.topics = topics;
        this.devices = devices;
        this.components = components;
        this.examples = examples;
        this.relations = relations;
        this.topicCitations = topicCitations;
        this.deviceCitations = deviceCitations;
        this.editorial = editorial;
        this.authorization = authorization;
        this.citations = citations;
        this.search = search;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.RHETORIC_TOPIC_VIEW + "')")
    public RhetoricViews.PageResult<RhetoricViews.TopicSummary> topics(int page, int size) {
        var result = topics.findAllByOrderByUpdatedAtDesc(page(page, size));
        return new RhetoricViews.PageResult<>(result.stream().map(topic -> new RhetoricViews.TopicSummary(topic.getId(), topic.getTitleOriginal(), topic.getSlug(), topic.getCategory().name(), topic.getStatus().name(), topic.getVersion())).toList(), page, size, result.getTotalElements());
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.RHETORIC_TOPIC_MANAGE + "')")
    public RhetoricViews.TopicAdmin createTopic(RhetoricViews.TopicDraft draft) {
        UUID actor = authorization.requireAccess().userId();
        RhetoricTopicEntity topic = new RhetoricTopicEntity();
        editorial.prepareNew(topic, actor);
        applyTopic(topic, draft.category(), draft.title(), draft.summary(), draft.displayOrder());
        topic.setSlug(ContentSlugs.of(topic.getTitleNormalized(), topic.getId()));
        editorial.persist(topics, topic, actor, AuditEventType.RHETORIC_TOPIC_CREATED, TOPIC);
        return topicAdmin(topic);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.CITATION_MANAGE + "')")
    public RhetoricViews.TopicAdmin citeTopic(UUID id, long version, UUID citationId) {
        citations.requireExisting(citationId);
        RhetoricTopicEntity topic = lockedTopic(id, version);
        UUID actor = authorization.requireAccess().userId();
        editorial.open(topic, actor, "citation linked", TOPIC);
        if (!topicCitations.existsByOwnerIdAndCitationId(id, citationId)) {
            topicCitations.save(new RhetoricTopicCitationEntity(id, citationId));
        }
        editorial.persist(topics, topic, actor, AuditEventType.CITATION_ADDED, TOPIC);
        return topicAdmin(topic);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.RHETORIC_DEVICE_EDIT + "')")
    public RhetoricViews.TopicAdmin submitTopic(UUID id, long version) {
        RhetoricTopicEntity topic = lockedTopic(id, version);
        UUID actor = authorization.requireAccess().userId();
        editorial.submit(topic);
        editorial.persist(topics, topic, actor, AuditEventType.RHETORIC_CONTENT_SUBMITTED, TOPIC);
        return topicAdmin(topic);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.RHETORIC_DEVICE_REVIEW + "')")
    public RhetoricViews.TopicAdmin verifyTopic(UUID id, long version) {
        RhetoricTopicEntity topic = lockedTopic(id, version);
        UUID actor = authorization.requireAccess().userId();
        editorial.review(topic, actor);
        editorial.persist(topics, topic, actor, AuditEventType.RHETORIC_CONTENT_VERIFIED, TOPIC);
        return topicAdmin(topic);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.RHETORIC_DEVICE_PUBLISH + "')")
    public RhetoricViews.TopicAdmin publishTopic(UUID id, long version) {
        search.lock();
        RhetoricTopicEntity topic = lockedTopic(id, version);
        UUID actor = authorization.requireAccess().userId();
        var lines = SourceLines.of(citations.requirePublishable(ids(topicCitations.findByOwnerId(id))));
        editorial.publish(topic, actor);
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("title", topic.getTitleOriginal());
        snapshot.put("slug", topic.getSlug());
        snapshot.put("summary", topic.getSummary());
        snapshot.put("category", topic.getCategory().name());
        snapshot.put("categoryLabel", topic.getCategory().arabicLabel());
        snapshot.put("sources", lines);
        topic.setPublishedSnapshot(snapshot);
        editorial.persist(topics, topic, actor, AuditEventType.RHETORIC_TOPIC_PUBLISHED, TOPIC);
        search.onTopicPublished(topic);
        return topicAdmin(topic);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.RHETORIC_DEVICE_PUBLISH + "')")
    public RhetoricViews.TopicAdmin archiveTopic(UUID id, long version) {
        search.lock();
        RhetoricTopicEntity topic = lockedTopic(id, version);
        UUID actor = authorization.requireAccess().userId();
        editorial.archive(topic);
        editorial.persist(topics, topic, actor, AuditEventType.RHETORIC_CONTENT_ARCHIVED, TOPIC);
        search.onTopicArchived(id);
        return topicAdmin(topic);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.RHETORIC_TOPIC_VIEW + "')")
    public RhetoricViews.DeviceAdmin device(UUID id) {
        return deviceAdmin(devices.findById(id).orElseThrow(() -> missing("Device")));
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.RHETORIC_DEVICE_CREATE + "')")
    public RhetoricViews.DeviceAdmin createDevice(RhetoricViews.DeviceDraft draft) {
        if (topics.findById(draft.topicId()).isEmpty()) {
            throw missing("Topic");
        }
        UUID actor = authorization.requireAccess().userId();
        RhetoricDeviceEntity device = new RhetoricDeviceEntity();
        editorial.prepareNew(device, actor);
        device.setTopicId(draft.topicId());
        applyDevice(device, draft.name(), draft.shortDefinition(), draft.detailedExplanation());
        device.setSlug(ContentSlugs.of(device.getNameNormalized(), device.getId()));
        editorial.persist(devices, device, actor, AuditEventType.RHETORIC_DEVICE_CREATED, DEVICE);
        return deviceAdmin(device);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.RHETORIC_DEVICE_EDIT + "')")
    public RhetoricViews.DeviceAdmin addComponent(UUID id, RhetoricViews.ComponentDraft draft) {
        if (draft.kind() == null) {
            throw invalid("kind", "Choose a component kind.");
        }
        RhetoricDeviceEntity device = lockedDevice(id, draft.version());
        UUID actor = authorization.requireAccess().userId();
        editorial.open(device, actor, "component added", DEVICE);
        RhetoricComponentEntity component = new RhetoricComponentEntity();
        component.setId(Ids.random());
        component.setDeviceId(id);
        component.setKind(draft.kind());
        component.setHeading(KnowledgeText.required(draft.heading(), "heading", 160));
        component.setBody(KnowledgeText.required(draft.body(), "body", 4000));
        component.setDisplayOrder(components.findByDeviceIdOrderByDisplayOrderAsc(id).size());
        components.save(component);
        editorial.persist(devices, device, actor, AuditEventType.RHETORIC_DEVICE_UPDATED, DEVICE);
        return deviceAdmin(device);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.RHETORIC_DEVICE_EDIT + "')")
    public RhetoricViews.DeviceAdmin addExample(UUID id, RhetoricViews.ExampleDraft draft) {
        String text = KnowledgeText.required(draft.text(), "text", 2000);
        String explanation = KnowledgeText.required(draft.explanation(), "explanation", 2000);
        RhetoricExampleRules.check(draft.kind(), text, explanation, draft.citationId());
        if (draft.citationId() != null) {
            citations.requireExisting(draft.citationId());
        }
        RhetoricDeviceEntity device = lockedDevice(id, draft.version());
        UUID actor = authorization.requireAccess().userId();
        editorial.open(device, actor, "example added", DEVICE);
        RhetoricExampleEntity example = new RhetoricExampleEntity();
        example.setId(Ids.random());
        example.setDeviceId(id);
        example.setKind(draft.kind());
        example.setExampleText(text);
        example.setExplanation(explanation);
        example.setHighlightedSegment(KnowledgeText.optional(draft.highlightedSegment(), "highlightedSegment", 300));
        example.setInterpretation(KnowledgeText.optional(draft.interpretation(), "interpretation", 2000));
        example.setScholarlyNote(KnowledgeText.optional(draft.scholarlyNote(), "scholarlyNote", 2000));
        example.setAlternativeInterpretation(KnowledgeText.optional(draft.alternativeInterpretation(), "alternativeInterpretation", 2000));
        example.setCitationId(draft.citationId());
        example.setDisplayOrder(examples.findByDeviceIdOrderByDisplayOrderAsc(id).size());
        examples.save(example);
        editorial.persist(devices, device, actor, AuditEventType.RHETORIC_DEVICE_UPDATED, DEVICE);
        return deviceAdmin(device);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.RHETORIC_DEVICE_EDIT + "')")
    public RhetoricViews.DeviceAdmin relate(UUID id, RhetoricViews.RelationDraft draft) {
        if (draft.kind() == null) {
            throw invalid("kind", "Choose a relation.");
        }
        if (id.equals(draft.targetDeviceId())) {
            throw new ConflictException("A device cannot relate to itself.");
        }
        if (devices.findById(draft.targetDeviceId()).isEmpty()) {
            throw missing("Device");
        }
        if (relations.existsBySourceDeviceIdAndTargetDeviceIdAndKind(id, draft.targetDeviceId(), draft.kind())) {
            throw new ConflictException("That relation already exists.");
        }
        RhetoricDeviceEntity device = lockedDevice(id, draft.version());
        UUID actor = authorization.requireAccess().userId();
        editorial.open(device, actor, "relation added", DEVICE);
        RhetoricRelationEntity relation = new RhetoricRelationEntity();
        relation.setId(Ids.random());
        relation.setSourceDeviceId(id);
        relation.setTargetDeviceId(draft.targetDeviceId());
        relation.setKind(draft.kind());
        relations.save(relation);
        editorial.persist(devices, device, actor, AuditEventType.RHETORIC_DEVICE_UPDATED, DEVICE);
        return deviceAdmin(device);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.CITATION_MANAGE + "')")
    public RhetoricViews.DeviceAdmin citeDevice(UUID id, long version, UUID citationId) {
        citations.requireExisting(citationId);
        RhetoricDeviceEntity device = lockedDevice(id, version);
        UUID actor = authorization.requireAccess().userId();
        editorial.open(device, actor, "citation linked", DEVICE);
        if (!deviceCitations.existsByOwnerIdAndCitationId(id, citationId)) {
            deviceCitations.save(new RhetoricDeviceCitationEntity(id, citationId));
        }
        editorial.persist(devices, device, actor, AuditEventType.CITATION_ADDED, DEVICE);
        return deviceAdmin(device);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.RHETORIC_DEVICE_EDIT + "')")
    public RhetoricViews.DeviceAdmin submitDevice(UUID id, long version) {
        RhetoricDeviceEntity device = lockedDevice(id, version);
        UUID actor = authorization.requireAccess().userId();
        editorial.submit(device);
        editorial.persist(devices, device, actor, AuditEventType.RHETORIC_CONTENT_SUBMITTED, DEVICE);
        return deviceAdmin(device);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.RHETORIC_DEVICE_REVIEW + "')")
    public RhetoricViews.DeviceAdmin verifyDevice(UUID id, long version) {
        RhetoricDeviceEntity device = lockedDevice(id, version);
        UUID actor = authorization.requireAccess().userId();
        editorial.review(device, actor);
        editorial.persist(devices, device, actor, AuditEventType.RHETORIC_CONTENT_VERIFIED, DEVICE);
        return deviceAdmin(device);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.RHETORIC_DEVICE_REVIEW + "')")
    public RhetoricViews.DeviceAdmin requestDeviceChanges(UUID id, long version, String reason) {
        RhetoricDeviceEntity device = lockedDevice(id, version);
        UUID actor = authorization.requireAccess().userId();
        editorial.requestChanges(device, actor, KnowledgeText.required(reason, "reason", 500));
        editorial.persist(devices, device, actor, AuditEventType.RHETORIC_CONTENT_CHANGES_REQUESTED, DEVICE);
        return deviceAdmin(device);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.RHETORIC_DEVICE_PUBLISH + "')")
    public RhetoricViews.DeviceAdmin publishDevice(UUID id, long version) {
        search.lock();
        RhetoricDeviceEntity device = lockedDevice(id, version);
        UUID actor = authorization.requireAccess().userId();
        List<RhetoricComponentEntity> parts = components.findByDeviceIdOrderByDisplayOrderAsc(id);
        if (parts.isEmpty()) {
            throw new ConflictException("A published device needs at least one structured component.");
        }
        List<UUID> citationIds = new ArrayList<>(ids(deviceCitations.findByOwnerId(id)));
        if (citationIds.isEmpty()) {
            throw new ConflictException("A published device needs at least one citation.");
        }
        for (RhetoricExampleEntity example : examples.findByDeviceIdOrderByDisplayOrderAsc(id)) {
            if (example.getCitationId() != null) {
                citationIds.add(example.getCitationId());
            }
        }
        var lines = SourceLines.of(citations.requirePublishable(citationIds));
        RhetoricTopicEntity topic = topics.findById(device.getTopicId()).orElseThrow(() -> missing("Topic"));
        editorial.publish(device, actor);
        device.setPublishedSnapshot(deviceSnapshot(device, topic, parts, examples.findByDeviceIdOrderByDisplayOrderAsc(id), lines));
        editorial.persist(devices, device, actor, AuditEventType.RHETORIC_DEVICE_PUBLISHED, DEVICE);
        search.onDevicePublished(device);
        return deviceAdmin(device);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.RHETORIC_DEVICE_PUBLISH + "')")
    public RhetoricViews.DeviceAdmin archiveDevice(UUID id, long version) {
        search.lock();
        RhetoricDeviceEntity device = lockedDevice(id, version);
        UUID actor = authorization.requireAccess().userId();
        editorial.archive(device);
        editorial.persist(devices, device, actor, AuditEventType.RHETORIC_CONTENT_ARCHIVED, DEVICE);
        search.onDeviceArchived(id);
        return deviceAdmin(device);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.RHETORIC_DEVICE_REVIEW + "')")
    public List<RhetoricViews.ReviewItem> reviewQueue() {
        List<RhetoricViews.ReviewItem> items = new ArrayList<>();
        for (RhetoricTopicEntity topic : topics.findByStatusOrderByUpdatedAtDesc(PublicationStatus.IN_REVIEW)) {
            items.add(new RhetoricViews.ReviewItem("TOPIC", topic.getId(), topic.getTitleOriginal(), topic.getStatus().name(), topic.getVersion()));
        }
        for (RhetoricDeviceEntity device : devices.findByStatusOrderByUpdatedAtDesc(PublicationStatus.IN_REVIEW)) {
            items.add(new RhetoricViews.ReviewItem("DEVICE", device.getId(), device.getNameOriginal(), device.getStatus().name(), device.getVersion()));
        }
        return items;
    }

    private Map<String, Object> deviceSnapshot(RhetoricDeviceEntity device, RhetoricTopicEntity topic, List<RhetoricComponentEntity> parts, List<RhetoricExampleEntity> samples, List<Map<String, Object>> sources) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("name", device.getNameOriginal());
        snapshot.put("slug", device.getSlug());
        snapshot.put("shortDefinition", device.getShortDefinition());
        snapshot.put("detailedExplanation", device.getDetailedExplanation());
        snapshot.put("topicId", topic.getId().toString());
        Map<String, Object> topicLink = new LinkedHashMap<>();
        topicLink.put("title", topic.getTitleOriginal());
        topicLink.put("slug", topic.getSlug());
        topicLink.put("category", topic.getCategory().name());
        snapshot.put("topic", topicLink);
        List<Map<String, Object>> componentMaps = new ArrayList<>();
        for (RhetoricComponentEntity component : parts) {
            componentMaps.add(Map.of("kind", component.getKind().name(), "heading", component.getHeading(), "body", component.getBody()));
        }
        snapshot.put("components", componentMaps);
        List<Map<String, Object>> exampleMaps = new ArrayList<>();
        for (RhetoricExampleEntity example : samples) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("kind", example.getKind().name());
            map.put("text", example.getExampleText());
            map.put("explanation", example.getExplanation());
            map.put("highlightedSegment", example.getHighlightedSegment());
            map.put("interpretation", example.getInterpretation());
            map.put("scholarlyNote", example.getScholarlyNote());
            map.put("alternativeInterpretation", example.getAlternativeInterpretation());
            map.put("cited", example.getCitationId() != null);
            exampleMaps.add(map);
        }
        snapshot.put("examples", exampleMaps);
        List<Map<String, Object>> relationMaps = new ArrayList<>();
        for (RhetoricRelationEntity relation : relations.findBySourceDeviceId(device.getId())) {
            RhetoricDeviceEntity target = devices.findById(relation.getTargetDeviceId()).orElse(null);
            if (target == null || !target.visibleToPublic()) {
                continue;
            }
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("kind", relation.getKind().name());
            map.put("title", String.valueOf(target.getPublishedSnapshot().getOrDefault("name", target.getNameOriginal())));
            map.put("slug", target.getSlug());
            relationMaps.add(map);
        }
        snapshot.put("relations", relationMaps);
        snapshot.put("sources", sources);
        return snapshot;
    }

    private void applyTopic(RhetoricTopicEntity topic, RhetoricCategory category, String title, String summary, Integer displayOrder) {
        if (category == null) {
            throw invalid("category", "Choose a rhetoric category.");
        }
        String original = KnowledgeText.required(title, "title", 160);
        topic.setCategory(category);
        topic.setTitleOriginal(original);
        topic.setTitleNormalized(KnowledgeText.normalized(original));
        topic.setSummary(KnowledgeText.optional(summary, "summary", 1000));
        topic.setDisplayOrder(displayOrder == null || displayOrder < 0 ? 0 : displayOrder);
    }

    private void applyDevice(RhetoricDeviceEntity device, String name, String shortDefinition, String detailed) {
        String original = KnowledgeText.required(name, "name", 160);
        device.setNameOriginal(original);
        device.setNameNormalized(KnowledgeText.normalized(original));
        device.setShortDefinition(KnowledgeText.required(shortDefinition, "shortDefinition", 500));
        device.setDetailedExplanation(KnowledgeText.optional(detailed, "detailedExplanation", 4000));
    }

    private RhetoricTopicEntity lockedTopic(UUID id, long version) {
        RhetoricTopicEntity topic = topics.lockById(id).orElseThrow(() -> missing("Topic"));
        editorial.requireVersion(topic, version);
        return topic;
    }

    private RhetoricDeviceEntity lockedDevice(UUID id, long version) {
        RhetoricDeviceEntity device = devices.lockById(id).orElseThrow(() -> missing("Device"));
        editorial.requireVersion(device, version);
        return device;
    }

    private RhetoricViews.TopicAdmin topicAdmin(RhetoricTopicEntity topic) {
        return new RhetoricViews.TopicAdmin(topic.getId(), topic.getCategory().name(), topic.getTitleOriginal(), topic.getSlug(), topic.getSummary(), topic.getStatus().name(), topic.getVersion(), ids(topicCitations.findByOwnerId(topic.getId())));
    }

    private RhetoricViews.DeviceAdmin deviceAdmin(RhetoricDeviceEntity device) {
        return new RhetoricViews.DeviceAdmin(
                device.getId(),
                device.getTopicId(),
                device.getNameOriginal(),
                device.getSlug(),
                device.getShortDefinition(),
                device.getDetailedExplanation(),
                device.getStatus().name(),
                device.getVersion(),
                components.findByDeviceIdOrderByDisplayOrderAsc(device.getId()).stream().map(component -> new RhetoricViews.ComponentView(component.getId(), component.getKind().name(), component.getHeading(), component.getBody())).toList(),
                examples.findByDeviceIdOrderByDisplayOrderAsc(device.getId()).stream().map(example -> new RhetoricViews.ExampleView(example.getId(), example.getKind().name(), example.getExampleText(), example.getExplanation(), example.getAlternativeInterpretation(), example.getCitationId())).toList(),
                relations.findBySourceDeviceId(device.getId()).stream().map(relation -> new RhetoricViews.RelationView(relation.getKind().name(), relation.getTargetDeviceId())).toList(),
                ids(deviceCitations.findByOwnerId(device.getId())));
    }

    private static List<UUID> ids(List<? extends OwnerCitationEntity> links) {
        return links.stream().map(OwnerCitationEntity::getCitationId).toList();
    }

    private static org.springframework.data.domain.Pageable page(int page, int size) {
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
