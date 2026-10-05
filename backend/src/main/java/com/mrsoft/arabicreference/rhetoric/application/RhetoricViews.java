package com.mrsoft.arabicreference.rhetoric.application;

import com.mrsoft.arabicreference.rhetoric.domain.RhetoricCategory;
import com.mrsoft.arabicreference.rhetoric.domain.RhetoricComponentKind;
import com.mrsoft.arabicreference.rhetoric.domain.RhetoricExampleKind;
import com.mrsoft.arabicreference.rhetoric.domain.RhetoricRelationKind;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class RhetoricViews {
    private RhetoricViews() {}

    public record PageResult<T>(List<T> items, int page, int size, long total) {}
    public record TopicDraft(RhetoricCategory category, String title, String summary, Integer displayOrder) {}
    public record TopicSummary(UUID id, String title, String slug, String category, String status, long version) {}
    public record TopicAdmin(UUID id, String category, String title, String slug, String summary, String status, long version, List<UUID> citationIds) {}
    public record DeviceDraft(UUID topicId, String name, String shortDefinition, String detailedExplanation) {}
    public record ComponentDraft(long version, RhetoricComponentKind kind, String heading, String body) {}
    public record ComponentView(UUID id, String kind, String heading, String body) {}
    public record ExampleDraft(long version, RhetoricExampleKind kind, String text, String explanation, String highlightedSegment, String interpretation, String scholarlyNote, String alternativeInterpretation, UUID citationId) {}
    public record ExampleView(UUID id, String kind, String text, String explanation, String alternativeInterpretation, UUID citationId) {}
    public record RelationDraft(long version, UUID targetDeviceId, RhetoricRelationKind kind) {}
    public record RelationView(String kind, UUID targetDeviceId) {}
    public record DeviceAdmin(UUID id, UUID topicId, String name, String slug, String shortDefinition, String detailedExplanation, String status, long version, List<ComponentView> components, List<ExampleView> examples, List<RelationView> relations, List<UUID> citationIds) {}
    public record VersionRequest(long version) {}
    public record ReasonRequest(long version, String reason) {}
    public record CitationRequest(long version, UUID citationId) {}
    public record ReviewItem(String kind, UUID id, String title, String status, long version) {}
    public record PublicLink(String title, String slug, String summary) {}
    public record PublicTopic(String title, String slug, String summary, String category, String categoryLabel, List<PublicLink> devices, List<Map<String, Object>> sources) {}
    public record PublicDevice(String name, String slug, String shortDefinition, String detailedExplanation, Map<String, Object> topic, List<Map<String, Object>> components, List<Map<String, Object>> examples, List<Map<String, Object>> relations, List<Map<String, Object>> sources) {}
}
