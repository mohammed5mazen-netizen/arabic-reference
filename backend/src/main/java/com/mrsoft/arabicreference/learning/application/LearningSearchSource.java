package com.mrsoft.arabicreference.learning.application;

import com.mrsoft.arabicreference.search.domain.PublishedSearchSource;
import com.mrsoft.arabicreference.search.domain.SearchDocument;
import com.mrsoft.arabicreference.search.domain.SearchEntityType;
import com.mrsoft.arabicreference.search.domain.SearchIndex;
import com.mrsoft.arabicreference.search.domain.SearchText;
import com.mrsoft.arabicreference.search.domain.SearchTuning;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Component
public class LearningSearchSource implements PublishedSearchSource {

    private final JdbcTemplate jdbc;
    private final SearchIndex index;
    private final JsonMapper json;

    public LearningSearchSource(JdbcTemplate jdbc, SearchIndex index, JsonMapper json) {
        this.jdbc = jdbc;
        this.index = index;
        this.json = json;
    }

    public void sync(UUID pathId, String previousSnapshot, Instant publishedAt) {
        index.exclusiveLock();
        String current = jdbc.query("select published_snapshot::text from learning_path where id = ?", (row, indexNumber) -> row.getString(1), pathId)
                .stream().findFirst().orElse(null);
        if (current == null) {
            removeSnapshot(previousSnapshot, pathId);
            return;
        }
        List<SearchDocument> documents = documents(pathId, json.readTree(current), publishedAt);
        for (SearchDocument document : documents) {
            index.upsert(document);
        }
        if (previousSnapshot != null) {
            for (UUID lessonId : lessonIds(json.readTree(previousSnapshot))) {
                if (documents.stream().noneMatch(document -> document.entityId().equals(lessonId))) {
                    index.remove(SearchEntityType.LESSON, lessonId);
                }
            }
        }
    }

    public void remove(UUID pathId, String snapshot) {
        index.exclusiveLock();
        removeSnapshot(snapshot, pathId);
    }

    @Override
    public List<SearchDocument> publishedDocuments() {
        List<SearchDocument> documents = new ArrayList<>();
        jdbc.query("select id, published_snapshot::text, updated_at from learning_path where published_snapshot is not null and status <> 'ARCHIVED'", row -> {
            String raw = row.getString(2);
            if (raw != null) {
                documents.addAll(documents(row.getObject(1, UUID.class), json.readTree(raw), row.getTimestamp(3).toInstant()));
            }
        });
        return documents;
    }

    private void removeSnapshot(String snapshot, UUID pathId) {
        index.remove(SearchEntityType.LEARNING_PATH, pathId);
        if (snapshot == null) {
            return;
        }
        for (UUID lessonId : lessonIds(json.readTree(snapshot))) {
            index.remove(SearchEntityType.LESSON, lessonId);
        }
    }

    private List<SearchDocument> documents(UUID pathId, JsonNode snapshot, Instant publishedAt) {
        List<SearchDocument> documents = new ArrayList<>();
        String pathSlug = snapshot.path("slug").asString();
        documents.add(document(SearchEntityType.LEARNING_PATH, pathId, snapshot.path("title").asString(), snapshot.path("summary").asString(), "/learn/" + pathSlug, publishedAt));
        for (JsonNode unit : snapshot.path("units")) {
            for (JsonNode lesson : unit.path("lessons")) {
                documents.add(document(
                        SearchEntityType.LESSON,
                        UUID.fromString(lesson.path("id").asString()),
                        lesson.path("title").asString(),
                        lesson.path("summary").asString(),
                        "/learn/" + pathSlug + "/" + lesson.path("slug").asString(),
                        publishedAt));
            }
        }
        return documents;
    }

    private static List<UUID> lessonIds(JsonNode snapshot) {
        List<UUID> ids = new ArrayList<>();
        for (JsonNode unit : snapshot.path("units")) {
            for (JsonNode lesson : unit.path("lessons")) {
                ids.add(UUID.fromString(lesson.path("id").asString()));
            }
        }
        return ids;
    }

    private static SearchDocument document(SearchEntityType type, UUID id, String title, String summary, String url, Instant publishedAt) {
        return new SearchDocument(type, id, title, SearchText.normalized(title), SearchText.key(title), SearchText.blob(title, summary), null, null, List.of(), null, "LEARNING", null, SearchText.snippet(summary), url, publishedAt, 0, 0, 0, SearchTuning.INDEX_VERSION);
    }
}
