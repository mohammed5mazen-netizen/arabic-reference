package com.mrsoft.arabicreference.search.infrastructure;

import com.mrsoft.arabicreference.search.domain.LinguisticSearchPort;
import com.mrsoft.arabicreference.search.domain.MatchReason;
import com.mrsoft.arabicreference.search.domain.SearchCandidate;
import com.mrsoft.arabicreference.search.domain.SearchDocument;
import com.mrsoft.arabicreference.search.domain.SearchEntityType;
import com.mrsoft.arabicreference.search.domain.SearchGeneration;
import com.mrsoft.arabicreference.search.domain.SearchIndex;
import com.mrsoft.arabicreference.search.domain.SearchSuggestion;
import com.mrsoft.arabicreference.search.domain.SearchToken;
import com.mrsoft.arabicreference.search.domain.SearchTuning;
import com.mrsoft.arabicreference.shared.kernel.id.Ids;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Repository
public class PostgresLinguisticSearchAdapter implements LinguisticSearchPort, SearchIndex, SearchGeneration {

    private static final String COLUMNS = """
            d.entity_type, d.entity_id, d.title_original, d.title_normalized, d.search_key,
            d.snippet, d.subtitle, d.url_path, d.root_normalized, d.root_label,
            d.part_of_speech, d.category, d.related_count
            """;

    private final JdbcTemplate jdbc;
    private final AtomicLong generation = new AtomicLong();

    public PostgresLinguisticSearchAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public long current() {
        return generation.get();
    }

    @Override
    public void exclusiveLock() {
        jdbc.query("select pg_advisory_xact_lock(?)", prepared -> prepared.setLong(1, SearchTuning.ADVISORY_LOCK_KEY), result -> null);
    }

    @Override
    public boolean tryExclusiveLock() {
        Boolean locked = jdbc.queryForObject("select pg_try_advisory_xact_lock(?)", Boolean.class, SearchTuning.ADVISORY_LOCK_KEY);
        return Boolean.TRUE.equals(locked);
    }

    @Override
    public List<SearchCandidate> collect(String displayQuery, String searchKey, String foldedKey) {
        limitRead();
        String prefix = escapeLike(searchKey) + "%";
        String contains = "%" + escapeLike(foldedKey) + "%";
        return jdbc.query("""
                select %s,
                       case
                         when d.entity_type <> 'ROOT' and d.title_original = ? collate "C" then 'EXACT'
                         when d.entity_type <> 'ROOT' and d.search_key = ? then 'NORMALIZED_EXACT'
                         when exists (select 1 from search_document_token t where t.document_id = d.id and t.kind = 'FORM' and t.token_key = ?) then 'WORD_FORM'
                         when exists (select 1 from search_document_token t where t.document_id = d.id and t.kind = 'ALIAS' and t.token_key = ?) then 'ALIAS'
                         when d.entity_type = 'ROOT' and d.search_key = ? then 'ROOT'
                         when d.search_key like ? escape '\\' then 'TITLE_PREFIX'
                         when d.searchable_text like ? escape '\\' then 'DEFINITION'
                         else null
                       end as reason
                from search_document d
                where (
                    (d.entity_type <> 'ROOT' and d.title_original = ? collate "C")
                    or d.search_key = ?
                    or d.search_key like ? escape '\\'
                    or d.searchable_text like ? escape '\\'
                    or exists (select 1 from search_document_token t where t.document_id = d.id and t.token_key = ?)
                )
                limit ?
                """.formatted(COLUMNS), (row, index) -> {
                    String reason = row.getString("reason");
                    return reason == null ? null : candidate(row, MatchReason.valueOf(reason), false);
                },
                displayQuery, searchKey, searchKey, searchKey, searchKey, prefix, contains,
                displayQuery, searchKey, prefix, contains, searchKey, SearchTuning.CANDIDATE_CAP)
                .stream()
                .filter(hit -> hit != null)
                .toList();
    }

    @Override
    public List<SearchCandidate> fuzzy(String searchKey, double threshold, int limit) {
        limitRead();
        return jdbc.query("""
                select %s,
                       similarity(d.search_key, ?) as title_sim,
                       similarity(left(d.searchable_text, 500), ?) as body_sim
                from search_document d
                where similarity(d.search_key, ?) >= ?
                   or similarity(left(d.searchable_text, 500), ?) >= ?
                order by greatest(similarity(d.search_key, ?), similarity(left(d.searchable_text, 500), ?)) desc
                limit ?
                """.formatted(COLUMNS), (row, index) -> candidate(row, MatchReason.FUZZY, row.getDouble("title_sim") < threshold),
                searchKey, searchKey, searchKey, threshold, searchKey, threshold, searchKey, searchKey, limit);
    }

    @Override
    public List<SearchCandidate> dictionaryWithRoot(String rootKey, int limit) {
        limitRead();
        return jdbc.query("""
                select %s
                from search_document d
                where d.entity_type = 'DICTIONARY_ENTRY' and d.root_normalized = ?
                order by d.title_normalized, d.entity_id
                limit ?
                """.formatted(COLUMNS), (row, index) -> candidate(row, MatchReason.ROOT, false), rootKey, limit);
    }

    @Override
    public Optional<SearchCandidate> dictionaryEntry(UUID entityId) {
        limitRead();
        List<SearchCandidate> rows = jdbc.query("""
                select %s
                from search_document d
                where d.entity_type = 'DICTIONARY_ENTRY' and d.entity_id = ?
                """.formatted(COLUMNS), (row, index) -> candidate(row, MatchReason.MORPHOLOGY, false), entityId);
        return rows.stream().findFirst();
    }

    @Override
    public List<SearchSuggestion> suggest(String searchKey, int limit) {
        limitRead();
        String prefix = escapeLike(searchKey) + "%";
        return jdbc.query("""
                select entity_type, title_original, url_path
                from search_document
                where entity_type in ('DICTIONARY_ENTRY', 'ROOT', 'GRAMMAR_CONCEPT', 'GRAMMAR_TOPIC')
                  and (search_key = ? or search_key like ? escape '\\')
                order by case when search_key = ? then 0 else 1 end, char_length(search_key), title_normalized, id
                limit ?
                """, (row, index) -> new SearchSuggestion(
                        SearchEntityType.valueOf(row.getString("entity_type")).suggestionKind(),
                        row.getString("title_original"),
                        row.getString("url_path")),
                searchKey, prefix, searchKey, limit);
    }

    @Override
    public void upsert(SearchDocument document) {
        UUID id = jdbc.queryForObject("""
                insert into search_document (
                    id, entity_type, entity_id, title_original, title_normalized, search_key, searchable_text,
                    root_normalized, root_label, part_of_speech, category, subtitle, snippet, url_path,
                    published_at, related_count, popularity, source_quality, index_version)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                on conflict (entity_type, entity_id) do update set
                    title_original = excluded.title_original,
                    title_normalized = excluded.title_normalized,
                    search_key = excluded.search_key,
                    searchable_text = excluded.searchable_text,
                    root_normalized = excluded.root_normalized,
                    root_label = excluded.root_label,
                    part_of_speech = excluded.part_of_speech,
                    category = excluded.category,
                    subtitle = excluded.subtitle,
                    snippet = excluded.snippet,
                    url_path = excluded.url_path,
                    published_at = excluded.published_at,
                    related_count = excluded.related_count,
                    popularity = excluded.popularity,
                    source_quality = excluded.source_quality,
                    index_version = excluded.index_version
                returning id
                """, UUID.class, documentArguments(Ids.random(), document));
        replaceTokens(id, document.tokens());
        refreshCount();
        generation.incrementAndGet();
    }

    @Override
    public void remove(SearchEntityType type, UUID entityId) {
        jdbc.update("delete from search_document where entity_type = ? and entity_id = ?", type.name(), entityId);
        refreshCount();
        generation.incrementAndGet();
    }

    @Override
    public void replaceAll(List<SearchDocument> documents, UUID actorId) {
        jdbc.update("delete from search_document_token");
        jdbc.update("delete from search_document");
        for (SearchDocument document : documents) {
            UUID id = Ids.random();
            jdbc.update("""
                    insert into search_document (
                        id, entity_type, entity_id, title_original, title_normalized, search_key, searchable_text,
                        root_normalized, root_label, part_of_speech, category, subtitle, snippet, url_path,
                        published_at, related_count, popularity, source_quality, index_version)
                    values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, documentArguments(id, document));
            replaceTokens(id, document.tokens());
        }
        jdbc.update("""
                update search_index_state
                set index_version = ?, document_count = ?, last_rebuild_at = now(), last_rebuild_by = ?
                where id = 1
                """, SearchTuning.INDEX_VERSION, documents.size(), actorId);
        generation.incrementAndGet();
    }

    @Override
    public SearchIndexState state() {
        return jdbc.queryForObject("""
                select index_version, document_count, last_rebuild_at
                from search_index_state
                where id = 1
                """, (row, index) -> new IndexState(row.getInt("index_version"), row.getLong("document_count"), instant(row, "last_rebuild_at")));
    }

    @Override
    public List<StoredDocument> storedDocuments() {
        return jdbc.query("select entity_type, entity_id, index_version from search_document",
                (row, index) -> new StoredDocument(SearchEntityType.valueOf(row.getString("entity_type")), row.getObject("entity_id", UUID.class), row.getInt("index_version")));
    }

    private void replaceTokens(UUID documentId, List<SearchToken> tokens) {
        jdbc.update("delete from search_document_token where document_id = ?", documentId);
        for (SearchToken token : tokens) {
            if (token.original() == null || token.original().isBlank() || token.key() == null || token.key().isBlank()) {
                continue;
            }
            jdbc.update("""
                    insert into search_document_token (id, document_id, token_original, token_key, kind)
                    values (?, ?, ?, ?, ?)
                    """, Ids.random(), documentId, truncate(token.original(), 200), truncate(token.key(), 200), token.kind().name());
        }
    }

    private void refreshCount() {
        jdbc.update("""
                update search_index_state
                set document_count = (select count(*) from search_document), index_version = ?
                where id = 1
                """, SearchTuning.INDEX_VERSION);
    }

    private Object[] documentArguments(UUID id, SearchDocument document) {
        return new Object[] {
                id,
                document.entityType().name(),
                document.entityId(),
                truncate(document.titleOriginal(), 200),
                truncate(document.titleNormalized(), 200),
                truncate(document.searchKey(), 200),
                document.searchableText() == null ? "" : document.searchableText(),
                emptyToNull(document.rootNormalized()),
                emptyToNull(document.rootLabel()),
                emptyToNull(document.partOfSpeech()),
                emptyToNull(document.category()),
                emptyToNull(truncate(document.subtitle(), 300)),
                document.snippet() == null ? "" : document.snippet(),
                truncate(document.urlPath(), 300),
                document.publishedAt() == null ? null : Timestamp.from(document.publishedAt()),
                document.relatedCount(),
                document.popularity(),
                document.sourceQuality(),
                document.indexVersion()
        };
    }

    private SearchCandidate candidate(ResultSet row, MatchReason reason, boolean fuzzyBody) throws SQLException {
        return new SearchCandidate(
                SearchEntityType.valueOf(row.getString("entity_type")),
                row.getObject("entity_id", UUID.class),
                row.getString("title_original"),
                row.getString("title_normalized"),
                row.getString("search_key"),
                row.getString("snippet"),
                row.getString("subtitle"),
                row.getString("url_path"),
                row.getString("root_normalized"),
                row.getString("root_label"),
                row.getString("part_of_speech"),
                row.getString("category"),
                row.getInt("related_count"),
                reason,
                fuzzyBody);
    }

    private void limitRead() {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            jdbc.execute("set local statement_timeout = '5000'");
        }
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private static String truncate(String value, int maxCodePoints) {
        if (value == null) {
            return "";
        }
        if (value.codePointCount(0, value.length()) <= maxCodePoints) {
            return value;
        }
        return value.substring(0, value.offsetByCodePoints(0, maxCodePoints));
    }

    private static String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private static Instant instant(ResultSet row, String column) throws SQLException {
        Timestamp timestamp = row.getTimestamp(column);
        return timestamp == null ? null : timestamp.toInstant();
    }

    private record IndexState(int indexVersion, long documentCount, Instant lastRebuildAt) implements SearchIndexState {
    }
}
