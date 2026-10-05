package com.mrsoft.arabicreference.grammar.application;

import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarConceptEntity;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarConceptRepository;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarRuleEntity;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarRuleRepository;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarTopicEntity;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarTopicRepository;
import com.mrsoft.arabicreference.linguistics.domain.editorial.PublicationStatus;
import com.mrsoft.arabicreference.search.domain.PublishedSearchSource;
import com.mrsoft.arabicreference.search.domain.SearchDocument;
import com.mrsoft.arabicreference.search.domain.SearchEntityType;
import com.mrsoft.arabicreference.search.domain.SearchIndex;
import com.mrsoft.arabicreference.search.domain.SearchText;
import com.mrsoft.arabicreference.search.domain.SearchToken;
import com.mrsoft.arabicreference.search.domain.SearchTokenKind;
import com.mrsoft.arabicreference.search.domain.SearchTuning;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class GrammarSearchIndexer implements PublishedSearchSource {

    private final GrammarTopicRepository topics;
    private final GrammarRuleRepository rules;
    private final GrammarConceptRepository concepts;
    private final SearchIndex index;

    public GrammarSearchIndexer(
            GrammarTopicRepository topics,
            GrammarRuleRepository rules,
            GrammarConceptRepository concepts,
            SearchIndex index) {
        this.topics = topics;
        this.rules = rules;
        this.concepts = concepts;
        this.index = index;
    }

    public void lock() {
        index.exclusiveLock();
    }

    public void onTopicPublished(GrammarTopicEntity topic) {
        SearchDocument document = topicDocument(topic);
        if (document != null) {
            index.upsert(document);
        }
    }

    public void onTopicArchived(UUID id) {
        index.remove(SearchEntityType.GRAMMAR_TOPIC, id);
    }

    public void onRulePublished(GrammarRuleEntity rule) {
        SearchDocument document = ruleDocument(rule);
        if (document != null) {
            index.upsert(document);
        }
    }

    public void onRuleArchived(UUID id) {
        index.remove(SearchEntityType.GRAMMAR_RULE, id);
    }

    public void onConceptPublished(GrammarConceptEntity concept) {
        SearchDocument document = conceptDocument(concept);
        if (document != null) {
            index.upsert(document);
        }
    }

    public void onConceptArchived(UUID id) {
        index.remove(SearchEntityType.GRAMMAR_CONCEPT, id);
    }

    @Override
    public List<SearchDocument> publishedDocuments() {
        List<SearchDocument> documents = new ArrayList<>();
        for (GrammarTopicEntity topic : topics.visibleToPublic(PublicationStatus.ARCHIVED)) {
            SearchDocument document = topicDocument(topic);
            if (document != null) {
                documents.add(document);
            }
        }
        for (GrammarRuleEntity rule : rules.visibleToPublic(PublicationStatus.ARCHIVED)) {
            SearchDocument document = ruleDocument(rule);
            if (document != null) {
                documents.add(document);
            }
        }
        for (GrammarConceptEntity concept : concepts.visibleToPublic(PublicationStatus.ARCHIVED)) {
            SearchDocument document = conceptDocument(concept);
            if (document != null) {
                documents.add(document);
            }
        }
        return documents;
    }

    private SearchDocument topicDocument(GrammarTopicEntity topic) {
        Map<String, Object> snapshot = topic.getPublishedSnapshot();
        if (snapshot == null || topic.getStatus() == PublicationStatus.ARCHIVED) {
            return null;
        }
        String title = text(snapshot.get("title"));
        String summary = text(snapshot.get("summary"));
        String category = text(snapshot.get("category"));
        return document(SearchEntityType.GRAMMAR_TOPIC, topic.getId(), title, summary, category, "/grammar/" + text(snapshot.get("slug")), topic.getUpdatedAt(), List.of(), SearchText.blob(title, summary));
    }

    private SearchDocument ruleDocument(GrammarRuleEntity rule) {
        Map<String, Object> snapshot = rule.getPublishedSnapshot();
        if (snapshot == null || rule.getStatus() == PublicationStatus.ARCHIVED) {
            return null;
        }
        String title = text(snapshot.get("title"));
        String summary = text(snapshot.get("summary"));
        String ruleText = text(snapshot.get("ruleText"));
        List<String> parts = new ArrayList<>();
        parts.add(summary);
        parts.add(ruleText);
        for (Object component : list(snapshot.get("components"))) {
            if (component instanceof Map<?, ?> map) {
                parts.add(text(map.get("heading")));
                parts.add(text(map.get("body")));
            }
        }
        return document(SearchEntityType.GRAMMAR_RULE, rule.getId(), title, SearchText.snippet(parts.toArray(String[]::new)), null, "/grammar/rules/" + text(snapshot.get("slug")), rule.getUpdatedAt(), List.of(), SearchText.blob(parts.toArray(String[]::new)));
    }

    private SearchDocument conceptDocument(GrammarConceptEntity concept) {
        Map<String, Object> snapshot = concept.getPublishedSnapshot();
        if (snapshot == null || concept.getStatus() == PublicationStatus.ARCHIVED) {
            return null;
        }
        String title = text(snapshot.get("term"));
        String summary = text(snapshot.get("shortDefinition"));
        String detailed = text(snapshot.get("detailedDefinition"));
        List<SearchToken> tokens = new ArrayList<>();
        for (Object alias : list(snapshot.get("aliases"))) {
            String original = text(alias);
            if (!original.isBlank()) {
                tokens.add(new SearchToken(original, SearchText.key(original), SearchTokenKind.ALIAS));
            }
        }
        return document(SearchEntityType.GRAMMAR_CONCEPT, concept.getId(), title, SearchText.snippet(summary, detailed), null, "/grammar/concepts/" + text(snapshot.get("slug")), concept.getUpdatedAt(), tokens, SearchText.blob(summary, detailed));
    }

    private SearchDocument document(
            SearchEntityType type,
            UUID id,
            String title,
            String snippet,
            String category,
            String url,
            java.time.Instant publishedAt,
            List<SearchToken> tokens,
            String searchable) {
        if (title.isBlank()) {
            return null;
        }
        String body = searchable == null || searchable.isBlank() ? SearchText.blob(snippet) : searchable;
        return new SearchDocument(
                type,
                id,
                title,
                SearchText.normalized(title),
                SearchText.key(title),
                body == null ? "" : body,
                null,
                null,
                tokens,
                null,
                category == null || category.isBlank() ? null : category,
                null,
                snippet == null ? "" : snippet,
                url,
                publishedAt,
                0,
                0,
                0,
                SearchTuning.INDEX_VERSION);
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private static List<?> list(Object value) {
        return value instanceof List<?> items ? items : List.of();
    }
}
