package com.mrsoft.arabicreference.dictionary.application;

import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.LexicalEntryEntity;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.LexicalEntryRepository;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.LinguisticRootEntity;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.LinguisticRootRepository;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class DictionarySearchIndexer implements PublishedSearchSource {

    private final LexicalEntryRepository entries;
    private final LinguisticRootRepository roots;
    private final SearchIndex index;

    public DictionarySearchIndexer(LexicalEntryRepository entries, LinguisticRootRepository roots, SearchIndex index) {
        this.entries = entries;
        this.roots = roots;
        this.index = index;
    }

    public void lock() {
        index.exclusiveLock();
    }

    public void onEntryPublished(LexicalEntryEntity entry) {
        SearchDocument document = entryDocument(entry);
        if (document != null) {
            index.upsert(document);
        }
        refreshRoot(entry.getRootId());
    }

    public void onEntryArchived(LexicalEntryEntity entry) {
        index.remove(SearchEntityType.DICTIONARY_ENTRY, entry.getId());
        refreshRoot(entry.getRootId());
    }

    public void onRootPublished(LinguisticRootEntity root) {
        SearchDocument document = rootDocument(root);
        if (document != null) {
            index.upsert(document);
        }
    }

    public void onRootArchived(UUID rootId) {
        index.remove(SearchEntityType.ROOT, rootId);
    }

    @Override
    public List<SearchDocument> publishedDocuments() {
        List<SearchDocument> documents = new ArrayList<>();
        for (LexicalEntryEntity entry : entries.visibleToPublic(PublicationStatus.ARCHIVED)) {
            SearchDocument document = entryDocument(entry);
            if (document != null) {
                documents.add(document);
            }
        }
        for (LinguisticRootEntity root : roots.visibleToPublic(PublicationStatus.ARCHIVED)) {
            SearchDocument document = rootDocument(root);
            if (document != null) {
                documents.add(document);
            }
        }
        return documents;
    }

    private void refreshRoot(UUID rootId) {
        if (rootId == null) {
            return;
        }
        roots.findById(rootId).ifPresent(root -> {
            SearchDocument document = rootDocument(root);
            if (document == null) {
                index.remove(SearchEntityType.ROOT, rootId);
            } else {
                index.upsert(document);
            }
        });
    }

    private SearchDocument entryDocument(LexicalEntryEntity entry) {
        Map<String, Object> snapshot = entry.getPublishedSnapshot();
        if (snapshot == null || entry.getStatus() == PublicationStatus.ARCHIVED) {
            return null;
        }
        String title = text(snapshot.get("lemmaOriginal"));
        if (title.isBlank()) {
            return null;
        }
        String vocalized = text(snapshot.get("vocalizedForm"));
        String partOfSpeech = text(snapshot.get("partOfSpeech"));
        Map<String, Object> root = map(snapshot.get("root"));
        String rootLabel = text(root.get("original"));
        String rootKey = SearchText.key(text(root.get("normalized")).isBlank() ? rootLabel : text(root.get("normalized")));
        List<String> definitions = new ArrayList<>();
        List<SearchToken> tokens = new ArrayList<>();
        for (Map<String, Object> sense : maps(snapshot.get("senses"))) {
            definitions.add(text(sense.get("shortDefinition")));
            definitions.add(text(sense.get("definition")));
        }
        for (Map<String, Object> form : maps(snapshot.get("forms"))) {
            String original = text(form.get("originalForm"));
            if (!original.isBlank()) {
                tokens.add(new SearchToken(original, SearchText.key(original), SearchTokenKind.FORM));
            }
        }
        return new SearchDocument(
                SearchEntityType.DICTIONARY_ENTRY,
                entry.getId(),
                title,
                SearchText.normalized(title),
                SearchText.key(title),
                SearchText.blob(definitions.toArray(String[]::new)),
                rootKey.isBlank() ? null : rootKey,
                rootLabel.isBlank() ? null : rootLabel,
                tokens,
                partOfSpeech.isBlank() ? null : partOfSpeech,
                null,
                vocalized.isBlank() ? null : vocalized,
                SearchText.snippet(definitions.toArray(String[]::new)),
                "/word/" + text(snapshot.get("slug")),
                entry.getUpdatedAt(),
                0,
                0,
                0,
                SearchTuning.INDEX_VERSION);
    }

    private SearchDocument rootDocument(LinguisticRootEntity root) {
        Map<String, Object> snapshot = root.getPublishedSnapshot();
        if (snapshot == null || root.getStatus() == PublicationStatus.ARCHIVED) {
            return null;
        }
        String title = text(snapshot.get("original"));
        if (title.isBlank()) {
            return null;
        }
        String normalized = text(snapshot.get("normalized"));
        List<LexicalEntryEntity> linked = entries.findPublishedByRoot(root.getId(), PublicationStatus.ARCHIVED);
        List<String> lemmas = new ArrayList<>();
        for (LexicalEntryEntity entry : linked) {
            if (lemmas.size() == 3) {
                break;
            }
            String lemma = entry.getPublishedSnapshot() == null ? "" : text(entry.getPublishedSnapshot().get("lemmaOriginal"));
            if (!lemma.isBlank()) {
                lemmas.add(lemma);
            }
        }
        return new SearchDocument(
                SearchEntityType.ROOT,
                root.getId(),
                title,
                SearchText.normalized(normalized.isBlank() ? title : normalized),
                SearchText.key(normalized.isBlank() ? title : normalized),
                SearchText.blob(title),
                SearchText.key(normalized.isBlank() ? title : normalized),
                title,
                List.of(),
                null,
                null,
                lemmas.isEmpty() ? null : String.join(" · ", lemmas),
                SearchText.snippet(String.join(" · ", lemmas)),
                "/root/" + text(snapshot.get("slug")),
                root.getUpdatedAt(),
                linked.size(),
                0,
                0,
                SearchTuning.INDEX_VERSION);
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private static Map<String, Object> map(Object value) {
        if (!(value instanceof Map<?, ?> source)) {
            return Map.of();
        }
        Map<String, Object> copy = new LinkedHashMap<>();
        source.forEach((key, item) -> copy.put(String.valueOf(key), item));
        return copy;
    }

    private static List<Map<String, Object>> maps(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : list) {
            Map<String, Object> mapped = map(item);
            if (!mapped.isEmpty()) {
                result.add(mapped);
            }
        }
        return result;
    }
}
