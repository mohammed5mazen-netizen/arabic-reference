package com.mrsoft.arabicreference.spelling.application;

import com.mrsoft.arabicreference.spelling.domain.SpellingClauseKind;
import com.mrsoft.arabicreference.spelling.domain.SpellingDifficulty;
import com.mrsoft.arabicreference.spelling.domain.SpellingExampleKind;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class SpellingViews {

    private SpellingViews() {
    }

    public record PageResult<T>(List<T> items, int page, int size, long total) {
    }

    public record TopicDraft(String title, String summary, Integer displayOrder) {
    }

    public record TopicUpdate(long version, String title, String summary, Integer displayOrder) {
    }

    public record RuleDraft(UUID topicId, String title, String summary, String coreRule, SpellingDifficulty difficulty) {
    }

    public record RuleUpdate(long version, String title, String summary, String coreRule, SpellingDifficulty difficulty) {
    }

    public record ClauseDraft(long version, SpellingClauseKind kind, String heading, String body) {
    }

    public record ExampleDraft(
            long version,
            SpellingExampleKind kind,
            String correctForm,
            String incorrectForm,
            String explanation,
            String contextNote,
            String commonForm,
            String reason,
            UUID citationId) {
    }

    public record VersionRequest(long version) {
    }

    public record ReasonRequest(long version, String reason) {
    }

    public record CitationRequest(long version, UUID citationId) {
    }

    public record TopicSummary(UUID id, String title, String slug, String status, int displayOrder, long version) {
    }

    public record ClauseView(UUID id, String kind, String heading, String body, int displayOrder) {
    }

    public record ExampleView(
            UUID id,
            String kind,
            String correctForm,
            String incorrectForm,
            String explanation,
            String contextNote,
            String commonForm,
            String reason,
            UUID citationId,
            int displayOrder) {
    }

    public record TopicAdmin(UUID id, String title, String slug, String summary, int displayOrder, String status, long version, List<UUID> citationIds) {
    }

    public record RuleAdmin(
            UUID id,
            UUID topicId,
            String title,
            String slug,
            String summary,
            String coreRule,
            String difficulty,
            String status,
            long version,
            List<ClauseView> clauses,
            List<ExampleView> examples,
            List<UUID> citationIds) {
    }

    public record ReviewItem(String kind, UUID id, String title, String status, long version) {
    }

    public record PublicLink(String title, String slug, String summary) {
    }

    public record PublicTopic(String title, String slug, String summary, List<PublicLink> rules, List<Map<String, Object>> sources) {
    }

    public record PublicRule(
            String title,
            String slug,
            String summary,
            String coreRule,
            String difficulty,
            String difficultyLabel,
            PublicLink topic,
            List<Map<String, Object>> clauses,
            List<Map<String, Object>> examples,
            List<Map<String, Object>> sources) {
    }
}
