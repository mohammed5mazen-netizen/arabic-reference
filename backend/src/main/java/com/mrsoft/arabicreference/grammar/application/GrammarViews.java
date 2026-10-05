package com.mrsoft.arabicreference.grammar.application;

import com.mrsoft.arabicreference.grammar.domain.ComponentType;
import com.mrsoft.arabicreference.grammar.domain.DifficultyLevel;
import com.mrsoft.arabicreference.grammar.domain.ExampleType;
import com.mrsoft.arabicreference.grammar.domain.GrammarCategory;
import com.mrsoft.arabicreference.grammar.domain.GrammaticalState;
import com.mrsoft.arabicreference.grammar.domain.RuleRelationType;
import com.mrsoft.arabicreference.grammar.domain.StateKind;
import com.mrsoft.arabicreference.linguistics.domain.editorial.PublicationStatus;
import java.util.List;
import java.util.UUID;

public final class GrammarViews {

    private GrammarViews() {
    }

    public record PageResult<T>(List<T> items, int page, int size, long total) {
    }

    public record VersionRequest(long version) {
    }

    public record ReasonRequest(long version, String reason) {
    }

    public record CitationRequest(long version, UUID citationId) {
    }

    public record TopicDraft(String title, String summary, UUID parentId, GrammarCategory category, DifficultyLevel difficulty, Integer displayOrder) {
    }

    public record TopicUpdate(long version, String title, String summary, GrammarCategory category, DifficultyLevel difficulty, Integer displayOrder) {
    }

    public record ParentChange(long version, UUID parentId) {
    }

    public record PrerequisiteRequest(long version, UUID requiredTopicId) {
    }

    public record TopicSummary(UUID id, UUID parentId, String title, String slug, GrammarCategory category, PublicationStatus status, int displayOrder, long version) {
    }

    public record TopicAdmin(UUID id, UUID parentId, String title, String slug, String summary, GrammarCategory category, DifficultyLevel difficulty, int displayOrder, PublicationStatus status, long version, List<UUID> citationIds, List<UUID> prerequisiteIds) {
    }

    public record RuleDraft(UUID topicId, String title, String summary, String ruleText, DifficultyLevel difficulty, Integer displayOrder) {
    }

    public record RuleUpdate(long version, String title, String summary, String ruleText, DifficultyLevel difficulty, Integer displayOrder) {
    }

    public record ComponentDraft(long version, ComponentType type, String heading, String body, Integer displayOrder) {
    }

    public record ExampleDraft(long version, UUID componentId, String textOriginal, String explanation, ExampleType exampleType, UUID citationId, Integer surah, Integer ayah, String poet, String workTitle, String verseLocator, UUID annotationId, Integer displayOrder) {
    }

    public record RelationDraft(long version, UUID targetRuleId, RuleRelationType type) {
    }

    public record ComponentAdmin(UUID id, ComponentType type, String heading, String body, int displayOrder, List<UUID> citationIds) {
    }

    public record ExampleAdmin(UUID id, UUID componentId, String textOriginal, String explanation, ExampleType exampleType, UUID citationId, Integer surah, Integer ayah, String poet, String workTitle, String verseLocator, UUID annotationId, int displayOrder) {
    }

    public record RelationAdmin(UUID id, UUID targetRuleId, RuleRelationType type) {
    }

    public record RuleSummary(UUID id, UUID topicId, String title, String slug, PublicationStatus status, long version) {
    }

    public record RuleAdmin(UUID id, UUID topicId, String title, String slug, String summary, String ruleText, DifficultyLevel difficulty, int displayOrder, PublicationStatus status, long version, List<ComponentAdmin> components, List<ExampleAdmin> examples, List<RelationAdmin> relations, List<UUID> citationIds, List<UUID> conceptIds) {
    }

    public record ConceptDraft(String term, String shortDefinition, String detailedDefinition) {
    }

    public record ConceptUpdate(long version, String term, String shortDefinition, String detailedDefinition) {
    }

    public record AliasDraft(long version, String alias) {
    }

    public record ConceptRuleRequest(long version, UUID ruleId) {
    }

    public record AliasAdmin(UUID id, String alias) {
    }

    public record ConceptSummary(UUID id, String term, String slug, PublicationStatus status, long version) {
    }

    public record ConceptAdmin(UUID id, String term, String slug, String shortDefinition, String detailedDefinition, PublicationStatus status, long version, List<AliasAdmin> aliases, List<UUID> citationIds, List<UUID> ruleIds) {
    }

    public record AnnotationDraft(String sentence, UUID citationId) {
    }

    public record TokenInput(String surface, Integer position, UUID lexicalEntryId, UUID morphologyAnalysisId, String roleCode, GrammaticalState grammaticalState, String explanation) {
    }

    public record TokensRequest(long version, List<TokenInput> tokens) {
    }

    public record DependencyInput(Integer governorPosition, Integer dependentPosition, String label) {
    }

    public record DependenciesRequest(long version, List<DependencyInput> edges) {
    }

    public record TokenAdmin(UUID id, String surface, int position, UUID lexicalEntryId, UUID morphologyAnalysisId, String roleCode, GrammaticalState grammaticalState, String explanation) {
    }

    public record DependencyAdmin(UUID id, int governorPosition, int dependentPosition, String label) {
    }

    public record AnnotationAdmin(UUID id, String sentence, UUID citationId, PublicationStatus status, long version, List<TokenAdmin> tokens, List<DependencyAdmin> dependencies) {
    }

    public record ExampleSummary(UUID id, UUID ruleId, String ruleTitle, String textOriginal, ExampleType exampleType) {
    }

    public record ReviewItem(String kind, UUID id, String title, String status, long version) {
    }

    public record RoleDraft(String code, String labelAr, StateKind stateKind) {
    }

    public record RoleView(UUID id, String code, String labelAr, StateKind stateKind, boolean active) {
    }

    public record Link(String title, String slug, String summary, String difficultyLabel) {
    }

    public record SourceLine(String title, String author, String edition, Integer publicationYear, Integer pageFrom, Integer pageTo, String attributionText) {
    }

    public record CategoryCard(String code, String label, long count) {
    }

    public record PublicIndex(String title, String introduction, List<CategoryCard> categories, PageResult<Link> topics) {
    }

    public record PublicTopic(String title, String slug, String summary, String category, String categoryLabel, String difficulty, String difficultyLabel, List<Link> ancestors, List<Link> children, List<Link> rules, List<Link> prerequisites, List<SourceLine> sources) {
    }

    public record PublicComponent(String type, String typeLabel, String heading, String body, List<SourceLine> sources) {
    }

    public record LexicalLink(String lemma, String slug) {
    }

    public record MorphologyLink(String patternOriginal, String label) {
    }

    public record PublicToken(String surface, int position, String roleLabel, String stateLabel, String explanation, LexicalLink lexical, MorphologyLink morphology) {
    }

    public record PublicExample(String textOriginal, String explanation, String exampleType, String exampleTypeLabel, boolean editorial, String editorialNote, Integer surah, Integer ayah, String poet, String workTitle, String verseLocator, SourceLine source, List<PublicToken> tokens, String annotationNote) {
    }

    public record PublicRelation(String type, String typeLabel, String title, String slug) {
    }

    public record PublicRule(String title, String slug, String summary, String ruleText, String difficulty, String difficultyLabel, Link topic, List<PublicComponent> components, List<PublicExample> examples, List<PublicRelation> relations, List<Link> concepts, List<SourceLine> sources) {
    }

    public record PublicConcept(String term, String slug, String shortDefinition, String detailedDefinition, List<String> aliases, List<Link> rules, List<SourceLine> sources) {
    }

    public record PublicAnnotation(String sentence, SourceLine source, List<PublicToken> tokens, String annotationNote) {
    }

    public record SearchHit(String kind, String title, String slug, String summary) {
    }
}
