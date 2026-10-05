package com.mrsoft.arabicreference.rhetoric.application;

import com.mrsoft.arabicreference.linguistics.domain.editorial.PublicationStatus;
import com.mrsoft.arabicreference.rhetoric.infrastructure.persistence.RhetoricDeviceEntity;
import com.mrsoft.arabicreference.rhetoric.infrastructure.persistence.RhetoricDeviceRepository;
import com.mrsoft.arabicreference.rhetoric.infrastructure.persistence.RhetoricTopicEntity;
import com.mrsoft.arabicreference.rhetoric.infrastructure.persistence.RhetoricTopicRepository;
import com.mrsoft.arabicreference.search.domain.PublishedSearchSource;
import com.mrsoft.arabicreference.search.domain.SearchDocument;
import com.mrsoft.arabicreference.search.domain.SearchEntityType;
import com.mrsoft.arabicreference.search.domain.SearchIndex;
import com.mrsoft.arabicreference.search.domain.SearchText;
import com.mrsoft.arabicreference.search.domain.SearchTuning;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class RhetoricSearchIndexer implements PublishedSearchSource {
    private final RhetoricTopicRepository topics;
    private final RhetoricDeviceRepository devices;
    private final SearchIndex index;

    public RhetoricSearchIndexer(RhetoricTopicRepository topics, RhetoricDeviceRepository devices, SearchIndex index) {
        this.topics = topics;
        this.devices = devices;
        this.index = index;
    }

    public void lock() { index.exclusiveLock(); }
    public void onTopicPublished(RhetoricTopicEntity topic) { upsert(topicDocument(topic)); }
    public void onTopicArchived(UUID id) { index.remove(SearchEntityType.RHETORIC_TOPIC, id); }
    public void onDevicePublished(RhetoricDeviceEntity device) { upsert(deviceDocument(device)); }
    public void onDeviceArchived(UUID id) { index.remove(SearchEntityType.RHETORIC_DEVICE, id); }

    @Override
    public List<SearchDocument> publishedDocuments() {
        List<SearchDocument> documents = new ArrayList<>();
        for (RhetoricTopicEntity topic : topics.visibleToPublic(PublicationStatus.ARCHIVED)) {
            SearchDocument document = topicDocument(topic);
            if (document != null) documents.add(document);
        }
        for (RhetoricDeviceEntity device : devices.visibleToPublic(PublicationStatus.ARCHIVED)) {
            SearchDocument document = deviceDocument(device);
            if (document != null) documents.add(document);
        }
        return documents;
    }

    private void upsert(SearchDocument document) {
        if (document != null) index.upsert(document);
    }

    private SearchDocument topicDocument(RhetoricTopicEntity topic) {
        Map<String, Object> snapshot = topic.getPublishedSnapshot();
        if (!topic.visibleToPublic()) return null;
        String title = text(snapshot.get("title"));
        return document(SearchEntityType.RHETORIC_TOPIC, topic.getId(), title, text(snapshot.get("summary")), "/rhetoric/" + text(snapshot.get("slug")), topic.getUpdatedAt(), text(snapshot.get("category")));
    }

    private SearchDocument deviceDocument(RhetoricDeviceEntity device) {
        Map<String, Object> snapshot = device.getPublishedSnapshot();
        if (!device.visibleToPublic()) return null;
        String title = text(snapshot.get("name"));
        String summary = text(snapshot.get("shortDefinition"));
        return document(SearchEntityType.RHETORIC_DEVICE, device.getId(), title, summary, "/rhetoric/devices/" + text(snapshot.get("slug")), device.getUpdatedAt(), "RHETORIC");
    }

    private SearchDocument document(SearchEntityType type, UUID id, String title, String snippet, String url, java.time.Instant publishedAt, String category) {
        if (title.isBlank()) return null;
        return new SearchDocument(type, id, title, SearchText.normalized(title), SearchText.key(title), SearchText.blob(title, snippet), null, null, List.of(), null, category, null, snippet == null ? "" : snippet, url, publishedAt, 0, 0, 0, SearchTuning.INDEX_VERSION);
    }

    private static String text(Object value) { return value == null ? "" : String.valueOf(value).trim(); }
}
