package com.mrsoft.arabicreference.spelling.application;

import com.mrsoft.arabicreference.linguistics.domain.editorial.PublicationStatus;
import com.mrsoft.arabicreference.search.domain.PublishedSearchSource;
import com.mrsoft.arabicreference.search.domain.SearchDocument;
import com.mrsoft.arabicreference.search.domain.SearchEntityType;
import com.mrsoft.arabicreference.search.domain.SearchIndex;
import com.mrsoft.arabicreference.search.domain.SearchText;
import com.mrsoft.arabicreference.search.domain.SearchTuning;
import com.mrsoft.arabicreference.spelling.infrastructure.persistence.SpellingRuleEntity;
import com.mrsoft.arabicreference.spelling.infrastructure.persistence.SpellingRuleRepository;
import com.mrsoft.arabicreference.spelling.infrastructure.persistence.SpellingTopicEntity;
import com.mrsoft.arabicreference.spelling.infrastructure.persistence.SpellingTopicRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class SpellingSearchIndexer implements PublishedSearchSource {

    private final SpellingTopicRepository topics;
    private final SpellingRuleRepository rules;
    private final SearchIndex index;

    public SpellingSearchIndexer(SpellingTopicRepository topics, SpellingRuleRepository rules, SearchIndex index) {
        this.topics = topics;
        this.rules = rules;
        this.index = index;
    }

    public void lock() {
        index.exclusiveLock();
    }

    public void onTopicPublished(SpellingTopicEntity topic) {
        SearchDocument document = topicDocument(topic);
        if (document != null) {
            index.upsert(document);
        }
    }

    public void onTopicArchived(UUID id) {
        index.remove(SearchEntityType.SPELLING_TOPIC, id);
    }

    public void onRulePublished(SpellingRuleEntity rule) {
        SearchDocument document = ruleDocument(rule);
        if (document != null) {
            index.upsert(document);
        }
    }

    public void onRuleArchived(UUID id) {
        index.remove(SearchEntityType.SPELLING_RULE, id);
    }

    @Override
    public List<SearchDocument> publishedDocuments() {
        List<SearchDocument> documents = new ArrayList<>();
        for (SpellingTopicEntity topic : topics.visibleToPublic(PublicationStatus.ARCHIVED)) {
            SearchDocument document = topicDocument(topic);
            if (document != null) {
                documents.add(document);
            }
        }
        for (SpellingRuleEntity rule : rules.visibleToPublic(PublicationStatus.ARCHIVED)) {
            SearchDocument document = ruleDocument(rule);
            if (document != null) {
                documents.add(document);
            }
        }
        return documents;
    }

    private SearchDocument topicDocument(SpellingTopicEntity topic) {
        Map<String, Object> snapshot = topic.getPublishedSnapshot();
        if (snapshot == null || topic.getStatus() == PublicationStatus.ARCHIVED) {
            return null;
        }
        String title = text(snapshot.get("title"));
        String summary = text(snapshot.get("summary"));
        return document(SearchEntityType.SPELLING_TOPIC, topic.getId(), title, summary, "/spelling/" + text(snapshot.get("slug")), topic.getUpdatedAt(), SearchText.blob(title, summary));
    }

    private SearchDocument ruleDocument(SpellingRuleEntity rule) {
        Map<String, Object> snapshot = rule.getPublishedSnapshot();
        if (snapshot == null || rule.getStatus() == PublicationStatus.ARCHIVED) {
            return null;
        }
        String title = text(snapshot.get("title"));
        String summary = text(snapshot.get("summary"));
        String core = text(snapshot.get("coreRule"));
        return document(SearchEntityType.SPELLING_RULE, rule.getId(), title, SearchText.snippet(summary, core), "/spelling/rules/" + text(snapshot.get("slug")), rule.getUpdatedAt(), SearchText.blob(title, summary, core));
    }

    private SearchDocument document(SearchEntityType type, UUID id, String title, String snippet, String url, java.time.Instant publishedAt, String searchable) {
        if (title.isBlank()) {
            return null;
        }
        return new SearchDocument(type, id, title, SearchText.normalized(title), SearchText.key(title), searchable == null ? "" : searchable, null, null, List.of(), null, "SPELLING", null, snippet == null ? "" : snippet, url, publishedAt, 0, 0, 0, SearchTuning.INDEX_VERSION);
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }
}
