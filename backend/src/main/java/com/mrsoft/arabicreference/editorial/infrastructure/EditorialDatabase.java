package com.mrsoft.arabicreference.editorial.infrastructure;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import com.mrsoft.arabicreference.editorial.domain.ContentType;
import com.mrsoft.arabicreference.editorial.domain.QualityProbe;
import com.mrsoft.arabicreference.shared.kernel.exception.ConflictException;
import com.mrsoft.arabicreference.shared.kernel.exception.ResourceNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class EditorialDatabase {

    private static final TypeReference<Map<String, Object>> MAP = new TypeReference<>() {
    };

    private final JdbcTemplate jdbc;
    private final JsonMapper json;

    public EditorialDatabase(JdbcTemplate jdbc, JsonMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    public Map<String, Long> statusCounts() {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (String status : List.of("DRAFT", "IN_REVIEW", "CHANGES_REQUESTED", "VERIFIED", "PUBLISHED", "ARCHIVED")) {
            counts.put(status, 0L);
        }
        jdbc.query("select status, count(*) from editorial_record group by status", rs -> {
            counts.put(rs.getString(1), rs.getLong(2));
        });
        return counts;
    }

    public long readyToPublish() {
        Long count = jdbc.queryForObject("""
                select count(*) from editorial_record r
                where r.status = 'VERIFIED'
                  and not exists (
                    select 1 from quality_finding f
                    where f.content_type = r.content_type and f.content_id = r.id
                      and f.status = 'OPEN' and f.severity = 'BLOCKER')
                """, Long.class);
        return count == null ? 0L : count;
    }

    public long openQualityIssues() {
        Long count = jdbc.queryForObject(
                "select count(*) from quality_finding where status = 'OPEN' and severity in ('WARNING', 'BLOCKER')",
                Long.class);
        return count == null ? 0L : count;
    }

    public long openBlockers(ContentType type, UUID id) {
        Long count = jdbc.queryForObject("""
                select count(*) from quality_finding
                where status = 'OPEN' and severity = 'BLOCKER' and content_type = ? and content_id = ?
                """, Long.class, type.name(), id);
        return count == null ? 0L : count;
    }

    public record QueueRow(
            String contentType,
            UUID id,
            String title,
            String slug,
            String status,
            UUID createdBy,
            UUID reviewedBy,
            Instant createdAt,
            Instant updatedAt,
            long version,
            UUID assigneeId,
            long blockers) {
    }

    public List<QueueRow> queue(QueueQuery query) {
        List<Object> args = new ArrayList<>();
        String where = filters(query, args);
        args.add(query.size());
        args.add(query.offset());
        return jdbc.query("""
                select r.content_type, r.id, r.title, r.slug, r.status, r.created_by, r.reviewed_by,
                       r.created_at, r.updated_at, r.version, a.assignee_id,
                       (select count(*) from quality_finding f
                         where f.content_type = r.content_type and f.content_id = r.id
                           and f.status = 'OPEN' and f.severity = 'BLOCKER') as blockers
                from editorial_record r
                left join editorial_assignment a
                  on a.content_type = r.content_type and a.content_id = r.id and a.assignment_role = 'REVIEWER'
                %s
                order by %s %s
                limit ? offset ?
                """.formatted(where, query.orderColumn(), query.direction()), (rs, index) -> new QueueRow(
                rs.getString("content_type"),
                rs.getObject("id", UUID.class),
                rs.getString("title"),
                rs.getString("slug"),
                rs.getString("status"),
                rs.getObject("created_by", UUID.class),
                rs.getObject("reviewed_by", UUID.class),
                instant(rs, "created_at"),
                instant(rs, "updated_at"),
                rs.getLong("version"),
                rs.getObject("assignee_id", UUID.class),
                rs.getLong("blockers")), args.toArray());
    }

    public long queueCount(QueueQuery query) {
        List<Object> args = new ArrayList<>();
        String where = filters(query, args);
        Long count = jdbc.queryForObject("select count(*) from editorial_record r left join editorial_assignment a on a.content_type = r.content_type and a.content_id = r.id and a.assignment_role = 'REVIEWER' " + where, Long.class, args.toArray());
        return count == null ? 0L : count;
    }

    public QueueRow requireRecord(ContentType type, UUID id) {
        List<QueueRow> rows = jdbc.query("""
                select content_type, id, title, slug, status, created_by, reviewed_by, created_at, updated_at, version,
                       null::uuid as assignee_id, 0::bigint as blockers
                from editorial_record where content_type = ? and id = ?
                """, (rs, index) -> new QueueRow(
                rs.getString("content_type"),
                rs.getObject("id", UUID.class),
                rs.getString("title"),
                rs.getString("slug"),
                rs.getString("status"),
                rs.getObject("created_by", UUID.class),
                rs.getObject("reviewed_by", UUID.class),
                instant(rs, "created_at"),
                instant(rs, "updated_at"),
                rs.getLong("version"),
                null,
                0L), type.name(), id);
        if (rows.isEmpty()) {
            throw new ResourceNotFoundException("Editorial record was not found.");
        }
        return rows.get(0);
    }

    public boolean assigneeHas(UUID userId, String permission) {
        Integer count = jdbc.queryForObject("""
                select count(*) from admin_user u
                join admin_user_role ur on ur.user_id = u.id
                join admin_role_permission rp on rp.role_id = ur.role_id
                where u.id = ? and u.status = 'ACTIVE' and rp.permission_code = ?
                """, Integer.class, userId, permission);
        return count != null && count > 0;
    }

    public void assign(UUID id, ContentType type, UUID contentId, String role, UUID assignee, UUID actor, Instant now, Long expectedVersion) {
        List<Long> versions = jdbc.query("""
                select version from editorial_assignment
                where content_type = ? and content_id = ? and assignment_role = ?
                """, (rs, index) -> rs.getLong(1), type.name(), contentId, role);
        if (versions.isEmpty()) {
            if (expectedVersion != null && expectedVersion != 0L) {
                throw new ConflictException("The assignment was updated by someone else. Reload and try again.");
            }
            jdbc.update("""
                    insert into editorial_assignment
                    (id, content_type, content_id, assignment_role, assignee_id, assigned_by, assigned_at, version)
                    values (?, ?, ?, ?, ?, ?, ?, 1)
                    """, id, type.name(), contentId, role, assignee, actor, Timestamp.from(now));
            return;
        }
        long current = versions.get(0);
        if (expectedVersion == null || expectedVersion != current) {
            throw new ConflictException("The assignment was updated by someone else. Reload and try again.");
        }
        int updated = jdbc.update("""
                update editorial_assignment
                set assignee_id = ?, assigned_by = ?, assigned_at = ?, version = version + 1
                where content_type = ? and content_id = ? and assignment_role = ? and version = ?
                """, assignee, actor, Timestamp.from(now), type.name(), contentId, role, expectedVersion);
        if (updated == 0) {
            throw new ConflictException("The assignment was updated by someone else. Reload and try again.");
        }
    }

    public void comment(UUID id, ContentType type, UUID contentId, String kind, String body, UUID actor, Instant now) {
        jdbc.update("""
                insert into editorial_comment
                (id, content_type, content_id, comment_type, body, status, created_by, created_at, version)
                values (?, ?, ?, ?, ?, 'OPEN', ?, ?, 0)
                """, id, type.name(), contentId, kind, body, actor, Timestamp.from(now));
    }

    public void resolveComment(UUID id, long version, UUID actor, Instant now) {
        int updated = jdbc.update("""
                update editorial_comment
                set status = 'RESOLVED', resolved_by = ?, resolved_at = ?, version = version + 1
                where id = ? and version = ? and status = 'OPEN'
                """, actor, Timestamp.from(now), id, version);
        if (updated == 0) {
            Integer exists = jdbc.queryForObject("select count(*) from editorial_comment where id = ?", Integer.class, id);
            if (exists == null || exists == 0) {
                throw new ResourceNotFoundException("Editorial comment was not found.");
            }
            throw new ConflictException("The comment was updated by someone else. Reload and try again.");
        }
    }

    public List<Map<String, Object>> comments(ContentType type, UUID contentId, int limit, int offset) {
        return jdbc.query("""
                select id, comment_type, body, status, created_by, created_at, resolved_by, resolved_at, version
                from editorial_comment
                where content_type = ? and content_id = ?
                order by created_at desc
                limit ? offset ?
                """, (rs, index) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", rs.getObject("id", UUID.class));
            row.put("type", rs.getString("comment_type"));
            row.put("body", rs.getString("body"));
            row.put("status", rs.getString("status"));
            row.put("createdBy", rs.getObject("created_by", UUID.class));
            row.put("createdAt", instant(rs, "created_at"));
            row.put("resolvedBy", rs.getObject("resolved_by", UUID.class));
            row.put("resolvedAt", instant(rs, "resolved_at"));
            row.put("version", rs.getLong("version"));
            return row;
        }, type.name(), contentId, limit, offset);
    }

    public List<Map<String, Object>> timeline(UUID contentId) {
        List<Map<String, Object>> events = new ArrayList<>();
        events.addAll(jdbc.query("""
                select e.event_type, e.occurred_at, e.actor_id, u.display_name, e.metadata::text as metadata
                from admin_audit_event e
                left join admin_user u on u.id = e.actor_id
                where e.target_id = ?
                order by e.occurred_at asc
                """, (rs, index) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("kind", rs.getString("event_type"));
            row.put("at", instant(rs, "occurred_at"));
            row.put("actorId", rs.getObject("actor_id", UUID.class));
            row.put("actor", rs.getString("display_name"));
            row.put("detail", safeMetadata(rs.getString("metadata")));
            return row;
        }, contentId.toString()));
        events.addAll(jdbc.query("""
                select revision_number, created_at, actor_id, change_reason
                from content_revision where target_id = ? order by revision_number asc
                """, (rs, index) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("kind", "REVISION");
            row.put("at", instant(rs, "created_at"));
            row.put("actorId", rs.getObject("actor_id", UUID.class));
            row.put("actor", null);
            row.put("detail", Map.of("revision", Integer.toString(rs.getInt("revision_number")), "reason", rs.getString("change_reason") == null ? "" : rs.getString("change_reason")));
            return row;
        }, contentId));
        events.sort((left, right) -> ((Instant) left.get("at")).compareTo((Instant) right.get("at")));
        return events;
    }

    public Map<String, Object> revisionSnapshot(UUID contentId, int revision) {
        List<String> rows = jdbc.query("select snapshot::text from content_revision where target_id = ? and revision_number = ?",
                (rs, index) -> rs.getString(1), contentId, revision);
        if (rows.isEmpty()) {
            throw new ResourceNotFoundException("Revision was not found.");
        }
        return parseMap(rows.get(0));
    }

    public Map<String, Object> currentFields(ContentType type, UUID id) {
        QueueRow row = requireRecord(type, id);
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("title", row.title());
        fields.put("slug", row.slug());
        fields.put("status", row.status());
        List<Map<String, Object>> summaries = jdbc.query(
                "select summary from editorial_record where content_type = ? and id = ?",
                (rs, index) -> {
                    Map<String, Object> value = new LinkedHashMap<>();
                    value.put("summary", rs.getString(1));
                    return value;
                }, type.name(), id);
        fields.put("summary", summaries.isEmpty() ? null : summaries.get(0).get("summary"));
        return fields;
    }

    public Map<String, Object> publishedSnapshot(ContentType type, UUID id) {
        String table = table(type);
        if (table == null) {
            return Map.of();
        }
        List<String> rows = jdbc.query("select published_snapshot::text from " + table + " where id = ?",
                (rs, index) -> rs.getString(1), id);
        if (rows.isEmpty() || rows.get(0) == null) {
            return Map.of();
        }
        return parseMap(rows.get(0));
    }

    public List<QualityProbe> probes(String scope, ContentType type, UUID id, int limit) {
        List<QualityProbe> probes = new ArrayList<>();
        List<ContentType> types = type == null ? List.of(ContentType.values()) : List.of(type);
        for (ContentType candidate : types) {
            if (candidate == ContentType.SEARCH_INDEX || probes.size() >= limit) {
                continue;
            }
            probes.addAll(load(candidate, scope, id, limit - probes.size()));
        }
        return probes;
    }

    public void replaceFindings(UUID scanId, UUID actor, Instant now, String scope, ContentType type, UUID id, List<QualityProbe> probes, List<Object[]> findings) {
        jdbc.update("""
                insert into quality_scan (id, scope, content_type, content_id, started_by, started_at, finished_at, record_count, finding_count)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, scanId, scope, type == null ? null : type.name(), id, actor, Timestamp.from(now), Timestamp.from(now), probes.size(), findings.size());
        for (QualityProbe probe : probes) {
            jdbc.update("""
                    update quality_finding set status = 'STALE', stale_at = ?
                    where status = 'OPEN' and content_type = ? and content_id = ?
                    """, Timestamp.from(now), probe.type().name(), probe.id());
        }
        for (Object[] finding : findings) {
            jdbc.update("""
                    insert into quality_finding
                    (id, scan_id, content_type, content_id, code, severity, message, field_name, status, detected_at)
                    values (?, ?, ?, ?, ?, ?, ?, ?, 'OPEN', ?)
                    """, UUID.randomUUID(), scanId, finding[0], finding[1], finding[2], finding[3], finding[4], finding[5], Timestamp.from(now));
        }
    }

    public List<Map<String, Object>> findings(String severity, String type, String code, String sort, int limit, int offset) {
        String order = "severity".equals(sort) ? "severity" : "detected_at";
        List<Object> args = new ArrayList<>();
        StringBuilder where = new StringBuilder(" where status = 'OPEN' ");
        if (severity != null) {
            where.append(" and severity = ? ");
            args.add(severity);
        }
        if (type != null) {
            where.append(" and content_type = ? ");
            args.add(type);
        }
        if (code != null) {
            where.append(" and code = ? ");
            args.add(code);
        }
        args.add(limit);
        args.add(offset);
        return jdbc.query("""
                select id, content_type, content_id, code, severity, message, field_name, detected_at
                from quality_finding
                %s
                order by %s desc
                limit ? offset ?
                """.formatted(where, order), (rs, index) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", rs.getObject("id", UUID.class));
            row.put("contentType", rs.getString("content_type"));
            row.put("contentId", rs.getObject("content_id", UUID.class));
            row.put("code", rs.getString("code"));
            row.put("severity", rs.getString("severity"));
            row.put("message", rs.getString("message"));
            row.put("field", rs.getString("field_name"));
            row.put("detectedAt", instant(rs, "detected_at"));
            return row;
        }, args.toArray());
    }

    private List<QualityProbe> load(ContentType type, String scope, UUID id, int limit) {
        return switch (type) {
            case DICTIONARY_ENTRY -> dictionary(scope, id, limit);
            case GRAMMAR_RULE -> grammar(scope, id, limit);
            case SPELLING_RULE -> spelling(scope, id, limit);
            case LITERARY_WORK -> literature(scope, id, limit);
            case ARTICLE -> articles(scope, id, limit);
            case LEARNING_PATH -> learning(scope, id, limit);
            default -> generic(type, scope, id, limit);
        };
    }

    private List<QualityProbe> dictionary(String scope, UUID id, int limit) {
        Prepared filter = filter("e", scope, id, limit);
        return jdbc.query("""
                select e.id, e.lemma_original as title, e.slug, cast(null as varchar) as summary, e.status, (e.published_snapshot is not null) as has_snapshot,
                       exists (select 1 from search_document d where d.entity_type = 'DICTIONARY_ENTRY' and d.entity_id = e.id) as indexed,
                       (select count(*) from lexical_sense s where s.lexical_entry_id = e.id) as senses,
                       (select count(*) from lexical_sense s where s.lexical_entry_id = e.id and not exists (select 1 from sense_citation c where c.sense_id = s.id)) as bare_senses,
                       (e.root_id is not null and not exists (select 1 from linguistic_root r where r.id = e.root_id)) as broken_root,
                       (e.status = 'PUBLISHED' and e.root_id is not null and exists (select 1 from linguistic_root r where r.id = e.root_id and (r.status <> 'PUBLISHED' or r.published_snapshot is null))) as unpublished_root,
                       (select count(*) from linguistic_relation rel join lexical_entry target on target.id = rel.target_entry_id where rel.source_entry_id = e.id and rel.status = 'PUBLISHED' and (target.status <> 'PUBLISHED' or target.published_snapshot is null)) as unpublished_relations,
                       (select count(*) from linguistic_relation rel where rel.source_entry_id = e.id and not exists (select 1 from lexical_entry target where target.id = rel.target_entry_id)) as orphans,
                       (select count(*) from sense_citation sc join lexical_sense s on s.id = sc.sense_id where s.lexical_entry_id = e.id and not exists (select 1 from source_citation c where c.id = sc.citation_id)) as broken_citations,
                       exists (select 1 from lexical_entry other where other.slug = e.slug and other.id <> e.id) as duplicate_slug,
                       (e.root_id is null and not exists (select 1 from linguistic_relation rel where rel.source_entry_id = e.id or rel.target_entry_id = e.id)) as unlinked
                from lexical_entry e
                %s
                """.formatted(filter.sql()), (rs, index) -> {
                    MutableProbe probe = base(ContentType.DICTIONARY_ENTRY, rs, false, false);
                    probe.unlinked(rs.getBoolean("unlinked"));
                    return probe.withDictionary(rs.getInt("senses"), rs.getInt("bare_senses"), rs.getBoolean("broken_root"), rs.getBoolean("unpublished_root"), rs.getInt("unpublished_relations"), rs.getInt("orphans"), rs.getInt("broken_citations"), rs.getBoolean("duplicate_slug"));
                }, filter.values());
    }

    private List<QualityProbe> grammar(String scope, UUID id, int limit) {
        Prepared filter = filter("r", scope, id, limit);
        return jdbc.query("""
                select r.id, r.title_original as title, r.slug, r.summary, r.status, (r.published_snapshot is not null) as has_snapshot,
                       exists (select 1 from search_document d where d.entity_type = 'GRAMMAR_RULE' and d.entity_id = r.id) as indexed,
                       (select count(*) from grammar_rule_component c where c.rule_id = r.id) as components,
                       (select count(*) from grammar_example ex where ex.rule_id = r.id and ex.example_type in ('QUOTED', 'QURANIC', 'POETRY', 'PROSE') and ex.citation_id is null) as bare_quotes,
                       (select count(*) from grammar_rule_citation c where c.rule_id = r.id) as citations,
                       exists (select 1 from grammar_rule other where other.slug = r.slug and other.id <> r.id) as duplicate_slug
                from grammar_rule r
                %s
                """.formatted(filter.sql()), (rs, index) -> base(ContentType.GRAMMAR_RULE, rs, true, publishable(rs))
                .withGrammar(rs.getInt("components"), rs.getInt("bare_quotes"), rs.getInt("citations"), rs.getBoolean("duplicate_slug")), filter.values());
    }

    private List<QualityProbe> spelling(String scope, UUID id, int limit) {
        Prepared filter = filter("r", scope, id, limit);
        return jdbc.query("""
                select r.id, r.title_original as title, r.slug, r.summary, r.status, (r.published_snapshot is not null) as has_snapshot,
                       exists (select 1 from search_document d where d.entity_type = 'SPELLING_RULE' and d.entity_id = r.id) as indexed,
                       (select count(*) from spelling_rule_citation c where c.owner_id = r.id) as citations,
                       (select count(*) from spelling_example ex where ex.rule_id = r.id and ex.kind = 'COMMON_MISTAKE' and ex.citation_id is null) as bare_mistakes,
                       exists (select 1 from spelling_rule other where other.slug = r.slug and other.id <> r.id) as duplicate_slug
                from spelling_rule r
                %s
                """.formatted(filter.sql()), (rs, index) -> base(ContentType.SPELLING_RULE, rs, true, "PUBLISHED".equals(rs.getString("status")))
                .withSpelling(rs.getInt("citations"), rs.getInt("bare_mistakes"), rs.getBoolean("duplicate_slug")), filter.values());
    }

    private List<QualityProbe> literature(String scope, UUID id, int limit) {
        Prepared filter = filter("w", scope, id, limit);
        return jdbc.query("""
                select w.id, w.title_original as title, w.slug, w.description as summary, w.status, w.rights_status,
                       (w.published_snapshot is not null) as has_snapshot,
                       exists (select 1 from search_document d where d.entity_type = 'LITERARY_WORK' and d.entity_id = w.id) as indexed,
                       (select count(*) from literary_excerpt x where x.work_id = w.id) as excerpts,
                       exists (select 1 from literary_work other where other.slug = w.slug and other.id <> w.id) as duplicate_slug
                from literary_work w
                %s
                """.formatted(filter.sql()), (rs, index) -> base(ContentType.LITERARY_WORK, rs, true, false)
                .withLiterature(rs.getString("rights_status"), rs.getInt("excerpts"), rs.getBoolean("duplicate_slug")), filter.values());
    }

    private List<QualityProbe> articles(String scope, UUID id, int limit) {
        Prepared filter = filter("a", scope, id, limit);
        return jdbc.query("""
                select a.id, a.title_original as title, a.slug, a.excerpt as summary, a.status, (a.published_snapshot is not null) as has_snapshot,
                       exists (select 1 from search_document d where d.entity_type = 'ARTICLE' and d.entity_id = a.id) as indexed,
                       (select count(*) from article_section s where s.article_id = a.id) as sections,
                       (select count(*) from article_citation c where c.article_id = a.id) as citations,
                       exists (select 1 from article other where other.slug = a.slug and other.id <> a.id) as duplicate_slug
                from article a
                %s
                """.formatted(filter.sql()), (rs, index) -> base(ContentType.ARTICLE, rs, true, publishable(rs))
                .withArticle(rs.getInt("sections"), rs.getInt("citations"), rs.getBoolean("duplicate_slug")), filter.values());
    }

    private List<QualityProbe> learning(String scope, UUID id, int limit) {
        Prepared filter = filter("p", scope, id, limit);
        return jdbc.query("""
                select p.id, p.title, p.slug, p.summary, p.status, (p.published_snapshot is not null) as has_snapshot,
                       exists (select 1 from search_document d where d.entity_type = 'LEARNING_PATH' and d.entity_id = p.id) as indexed,
                       (select count(*) from learning_lesson l join learning_unit u on u.id = l.unit_id
                         where u.path_id = p.id and not exists (select 1 from learning_objective o where o.lesson_id = l.id)) as bare_lessons,
                       (select count(*) from learning_question q
                         join learning_quiz z on z.id = q.quiz_id
                         join learning_lesson l on l.id = z.lesson_id
                         join learning_unit u on u.id = l.unit_id
                         where u.path_id = p.id and (
                           (select count(*) from learning_question_option o where o.question_id = q.id) < 2
                           or (select count(*) from learning_question_option o where o.question_id = q.id and o.correct) = 0
                         )) as invalid_questions,
                       (select count(*) from learning_reference r
                         join learning_lesson l on l.id = r.lesson_id
                         join learning_unit u on u.id = l.unit_id
                         where u.path_id = p.id and r.target_kind <> 'MORPHOLOGY_TOOL'
                           and not exists (select 1 from editorial_record er where er.slug = r.target_slug and er.status = 'PUBLISHED' and er.content_type = r.target_kind)) as unpublished_refs
                from learning_path p
                %s
                """.formatted(filter.sql()), (rs, index) -> base(ContentType.LEARNING_PATH, rs, true, false)
                .withLearning(rs.getInt("bare_lessons"), rs.getInt("invalid_questions"), rs.getInt("unpublished_refs")), filter.values());
    }

    private List<QualityProbe> generic(ContentType type, String scope, UUID id, int limit) {
        String where = " where r.content_type = ? ";
        List<Object> args = new ArrayList<>();
        args.add(type.name());
        if ("RECORD".equals(scope)) {
            where += " and r.id = ? ";
            args.add(id);
        } else if ("PUBLISHED".equals(scope)) {
            where += " and r.status = 'PUBLISHED' ";
        }
        args.add(limit);
        String extra = where + " order by r.updated_at desc limit ? ";
        return jdbc.query("""
                select r.id, r.title, r.slug, r.summary, r.status, r.has_snapshot,
                       exists (select 1 from search_document d where d.entity_type = r.content_type and d.entity_id = r.id) as indexed,
                       exists (select 1 from editorial_record other where other.content_type = r.content_type and other.slug = r.slug and other.id <> r.id) as duplicate_slug
                from editorial_record r
                %s
                """.formatted(extra), (rs, index) -> base(type, rs, summaryRequired(type), false)
                .withDuplicate(rs.getBoolean("duplicate_slug")), args.toArray());
    }

    private static boolean summaryRequired(ContentType type) {
        return switch (type) {
            case DICTIONARY_ENTRY, MORPHOLOGY_ANALYSIS, SEARCH_INDEX -> false;
            default -> true;
        };
    }

    private static boolean publishable(ResultSet rs) throws SQLException {
        String status = rs.getString("status");
        return "VERIFIED".equals(status) || "PUBLISHED".equals(status);
    }

    private Prepared filter(String alias, String scope, UUID id, int limit) {
        if ("RECORD".equals(scope)) {
            return new Prepared(" where " + alias + ".id = ? order by " + alias + ".updated_at desc limit ?", List.of(id, limit));
        }
        if ("PUBLISHED".equals(scope)) {
            return new Prepared(" where " + alias + ".status = 'PUBLISHED' order by " + alias + ".updated_at desc limit ?", List.of(limit));
        }
        return new Prepared(" order by " + alias + ".updated_at desc limit ?", List.of(limit));
    }

    private MutableProbe base(ContentType type, ResultSet rs, boolean summaryRequired, boolean citationRequired) throws SQLException {
        MutableProbe probe = new MutableProbe(type, rs.getObject("id", UUID.class), rs.getString("title"), text(rs, "summary"), rs.getString("status"),
                summaryRequired, citationRequired, flag(rs, "has_snapshot"), rs.getBoolean("indexed"));
        probe.invalidCanonical(invalidSlug(text(rs, "slug")));
        return probe;
    }

    static boolean invalidSlug(String slug) {
        return slug == null || slug.isBlank() || slug.chars().anyMatch(character -> character <= ' ' || character == '%' || character == '?' || character == '#' || character == '/');
    }

    private static String text(ResultSet rs, String column) throws SQLException {
        try {
            return rs.getString(column);
        } catch (SQLException exception) {
            return null;
        }
    }

    private static boolean flag(ResultSet rs, String column) throws SQLException {
        try {
            return rs.getBoolean(column);
        } catch (SQLException exception) {
            return false;
        }
    }

    private String filters(QueueQuery query, List<Object> args) {
        StringBuilder where = new StringBuilder(" where 1 = 1 ");
        if (query.type() != null) {
            where.append(" and r.content_type = ? ");
            args.add(query.type().name());
        }
        if (query.status() != null) {
            where.append(" and r.status = ? ");
            args.add(query.status());
        }
        if (query.creator() != null) {
            where.append(" and r.created_by = ? ");
            args.add(query.creator());
        }
        if (query.reviewer() != null) {
            where.append(" and r.reviewed_by = ? ");
            args.add(query.reviewer());
        }
        if (query.assignee() != null) {
            where.append(" and a.assignee_id = ? ");
            args.add(query.assignee());
        }
        if (query.updatedFrom() != null) {
            where.append(" and r.updated_at >= ? ");
            args.add(Timestamp.from(query.updatedFrom()));
        }
        if (query.updatedTo() != null) {
            where.append(" and r.updated_at <= ? ");
            args.add(Timestamp.from(query.updatedTo()));
        }
        if ("BLOCKED".equals(query.quality())) {
            where.append(" and exists (select 1 from quality_finding f where f.content_type = r.content_type and f.content_id = r.id and f.status = 'OPEN' and f.severity = 'BLOCKER') ");
        } else if ("CLEAR".equals(query.quality())) {
            where.append(" and not exists (select 1 from quality_finding f where f.content_type = r.content_type and f.content_id = r.id and f.status = 'OPEN' and f.severity = 'BLOCKER') ");
        }
        if (query.text() != null) {
            where.append(" and (r.title ilike ? or r.slug ilike ? or cast(r.id as varchar) = ?) ");
            args.add("%" + query.text() + "%");
            args.add("%" + query.text() + "%");
            args.add(query.text());
        }
        if ("waiting".equals(query.section())) {
            where.append(" and r.status = 'IN_REVIEW' ");
        } else if ("changes".equals(query.section())) {
            where.append(" and r.status = 'CHANGES_REQUESTED' ");
        } else if ("assigned".equals(query.section()) && query.actor() != null) {
            where.append(" and a.assignee_id = ? ");
            args.add(query.actor());
        } else if ("recent".equals(query.section()) && query.actor() != null) {
            where.append(" and r.reviewed_by = ? and r.updated_at >= ? ");
            args.add(query.actor());
            args.add(Timestamp.from(query.updatedFrom() == null ? Instant.now().minusSeconds(30L * 24 * 3600) : query.updatedFrom()));
        } else if ("publishing".equals(query.section())) {
            where.append(" and r.status = 'VERIFIED' ");
        }
        return where.toString();
    }

    private Map<String, String> safeMetadata(String raw) {
        Map<String, Object> parsed = parseMap(raw);
        Map<String, String> safe = new LinkedHashMap<>();
        for (String key : List.of("status", "reason", "license")) {
            if (parsed.get(key) != null) {
                safe.put(key, String.valueOf(parsed.get(key)));
            }
        }
        return safe;
    }

    private Map<String, Object> parseMap(String raw) {
        if (raw == null || raw.isBlank()) {
            return Map.of();
        }
        try {
            return json.readValue(raw, MAP);
        } catch (Exception exception) {
            return Map.of();
        }
    }

    private static Instant instant(ResultSet rs, String column) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(column);
        return timestamp == null ? null : timestamp.toInstant();
    }

    private static String table(ContentType type) {
        return switch (type) {
            case DICTIONARY_ENTRY -> "lexical_entry";
            case MORPHOLOGY_ANALYSIS -> "morphology_analysis";
            case GRAMMAR_TOPIC -> "grammar_topic";
            case GRAMMAR_RULE -> "grammar_rule";
            case GRAMMAR_CONCEPT -> "grammar_concept";
            case SPELLING_TOPIC -> "spelling_topic";
            case SPELLING_RULE -> "spelling_rule";
            case RHETORIC_TOPIC -> "rhetoric_topic";
            case RHETORIC_DEVICE -> "rhetoric_device";
            case LITERARY_ERA -> "literary_era";
            case LITERARY_GENRE -> "literary_genre";
            case LITERARY_SCHOOL -> "literary_school";
            case LITERARY_FIGURE -> "literary_figure";
            case LITERARY_WORK -> "literary_work";
            case ARTICLE -> "article";
            case LEARNING_PATH -> "learning_path";
            case SEARCH_INDEX -> null;
        };
    }

    public record QueueQuery(
            ContentType type,
            String status,
            UUID creator,
            UUID reviewer,
            UUID assignee,
            UUID actor,
            Instant updatedFrom,
            Instant updatedTo,
            String quality,
            String text,
            String section,
            String orderColumn,
            String direction,
            int size,
            int offset) {
    }

    private record Prepared(String sql, List<Object> parameters) {
        private Object[] values() {
            return parameters.toArray();
        }
    }

    private static final class MutableProbe {
        private final ContentType type;
        private final UUID id;
        private final String title;
        private final String summary;
        private final String status;
        private final boolean summaryRequired;
        private final boolean citationRequired;
        private final boolean hasSnapshot;
        private final boolean indexed;
        private int senseCount;
        private int sensesWithoutCitation;
        private boolean brokenRoot;
        private boolean unpublishedRoot;
        private int unpublishedRelations;
        private int orphanRelations;
        private int brokenCitations;
        private boolean duplicateSlug;
        private int componentCount;
        private int quotedExamplesWithoutCitation;
        private int citationCount;
        private String rights;
        private int excerptCount;
        private int sectionCount;
        private int lessonsWithoutObjective;
        private int invalidQuestions;
        private int unpublishedKnowledgeRefs;
        private int commonMistakesWithoutEvidence;
        private boolean invalidCanonical;
        private boolean unlinked;

        private MutableProbe(ContentType type, UUID id, String title, String summary, String status, boolean summaryRequired, boolean citationRequired, boolean hasSnapshot, boolean indexed) {
            this.type = type;
            this.id = id;
            this.title = title;
            this.summary = summary;
            this.status = status;
            this.summaryRequired = summaryRequired;
            this.citationRequired = citationRequired;
            this.hasSnapshot = hasSnapshot;
            this.indexed = indexed;
        }

        private QualityProbe withDictionary(int senses, int bare, boolean broken, boolean unpublishedRoot, int unpublishedRelations, int orphans, int brokenCitations, boolean duplicate) {
            this.senseCount = senses;
            this.sensesWithoutCitation = bare;
            this.brokenRoot = broken;
            this.unpublishedRoot = unpublishedRoot;
            this.unpublishedRelations = unpublishedRelations;
            this.orphanRelations = orphans;
            this.brokenCitations = brokenCitations;
            this.duplicateSlug = duplicate;
            return build();
        }

        private QualityProbe withGrammar(int components, int quotes, int citations, boolean duplicate) {
            this.componentCount = components;
            this.quotedExamplesWithoutCitation = quotes;
            this.citationCount = citations;
            this.duplicateSlug = duplicate;
            return build();
        }

        private QualityProbe withSpelling(int citations, int mistakes, boolean duplicate) {
            this.citationCount = citations;
            this.commonMistakesWithoutEvidence = mistakes;
            this.duplicateSlug = duplicate;
            return build();
        }

        private QualityProbe withLiterature(String rights, int excerpts, boolean duplicate) {
            this.rights = rights;
            this.excerptCount = excerpts;
            this.duplicateSlug = duplicate;
            return build();
        }

        private QualityProbe withArticle(int sections, int citations, boolean duplicate) {
            this.sectionCount = sections;
            this.citationCount = citations;
            this.duplicateSlug = duplicate;
            return build();
        }

        private QualityProbe withLearning(int bareLessons, int invalidQuestions, int unpublishedRefs) {
            this.lessonsWithoutObjective = bareLessons;
            this.invalidQuestions = invalidQuestions;
            this.unpublishedKnowledgeRefs = unpublishedRefs;
            return build();
        }

        private QualityProbe withDuplicate(boolean duplicate) {
            this.duplicateSlug = duplicate;
            return build();
        }

        private void unlinked(boolean value) {
            this.unlinked = value;
        }

        private void invalidCanonical(boolean value) {
            this.invalidCanonical = value;
        }

        private QualityProbe build() {
            return new QualityProbe(type, id, title, summary, status, summaryRequired, citationRequired, hasSnapshot, indexed,
                    senseCount, sensesWithoutCitation, brokenRoot, unpublishedRoot, unpublishedRelations, orphanRelations, brokenCitations,
                    duplicateSlug, componentCount, quotedExamplesWithoutCitation, citationCount, rights, excerptCount, sectionCount,
                    lessonsWithoutObjective, invalidQuestions, unpublishedKnowledgeRefs, commonMistakesWithoutEvidence, invalidCanonical, unlinked);
        }
    }
}
