package com.mrsoft.arabicreference.tools.application;

import java.util.List;

public final class ToolViews {

    private ToolViews() {
    }

    public record ProvenanceNote(String kind, String label) {
    }

    public record SourceLink(String label, String href) {
    }

    public record ToolEnvelope(
            String input,
            String normalizedInput,
            String tool,
            String status,
            Object result,
            List<ProvenanceNote> provenance,
            List<String> limitations) {
    }

    public record EntryLink(String lemma, String slug, String partOfSpeechLabel, String href, String note) {
    }

    public record SenseView(String definition, String usageLabel, String domainLabel, List<EntryLink> synonyms, List<EntryLink> antonyms, List<EntryLink> related) {
    }

    public record MorphLine(String pattern, String categoryLabel, String provenanceLabel) {
    }

    public record WordCard(
            String lemma,
            String slug,
            String href,
            String partOfSpeechLabel,
            String root,
            String rootLabel,
            String rootHref,
            List<SenseView> senses,
            List<String> forms,
            List<MorphLine> morphology,
            List<SourceLink> grammar,
            List<SourceLink> spelling,
            List<SourceLink> articles,
            List<String> missing) {
    }

    public record WordAnalysis(String notice, List<WordCard> entries) {
    }

    public record RootResult(String heading, String root, String slug, String href, List<EntryLink> entries, List<EntryLink> patterns) {
    }

    public record DerivationGroup(String partOfSpeechLabel, List<EntryLink> entries) {
    }

    public record DerivationResult(String root, String href, List<DerivationGroup> groups, List<EntryLink> recordedPatterns) {
    }

    public record PatternCard(
            String code,
            String original,
            String categoryLabel,
            int radicalCount,
            String description,
            List<EntryLink> examples,
            String coverage) {
    }

    public record PatternResult(List<PatternCard> patterns) {
    }

    public record Comparison(boolean sameWord, boolean missingSide, WordCard left, WordCard right, String semanticDifference) {
    }

    public record RelationResult(String notice, List<SenseView> senses, List<EntryLink> entryLevel) {
    }

    public record SpellingEvidence(String kindLabel, String recordedForm, String explanation, String contextNote, String href) {
    }

    public record Suggestion(String title, String href) {
    }

    public record SpellingCheck(String verdict, String suggestionsLabel, List<SpellingEvidence> evidence, List<Suggestion> suggestions) {
    }

    public record GrammarItem(String kindLabel, String title, String href, String summary, List<String> examples, List<SourceLink> related) {
    }

    public record GrammarResult(List<GrammarItem> items) {
    }

    public record GraphNode(String id, String label, String href, String kindLabel) {
    }

    public record GraphEdge(String from, String to, String relationLabel) {
    }

    public record GraphResult(List<GraphNode> nodes, List<GraphEdge> edges, List<GraphNode> list) {
    }

    public record ToolCount(String code, long calls, long emptyResults, long errors) {
    }

    public record AdminTools(List<ToolCatalogView> tools, List<ToolCount> counts) {
    }

    public record ToolCatalogView(String code, String name, String description, String route, String status, String inputKind) {
    }
}
