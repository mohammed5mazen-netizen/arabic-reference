package com.mrsoft.arabicreference.editorial.application;

import com.mrsoft.arabicreference.editorial.domain.AssignmentRole;
import com.mrsoft.arabicreference.editorial.domain.CommentKind;
import com.mrsoft.arabicreference.editorial.domain.ContentType;
import com.mrsoft.arabicreference.editorial.domain.QualityProbe;
import com.mrsoft.arabicreference.editorial.domain.QualityRules;
import com.mrsoft.arabicreference.editorial.domain.StructuredDiff;
import com.mrsoft.arabicreference.editorial.infrastructure.EditorialDatabase;
import com.mrsoft.arabicreference.editorial.infrastructure.EditorialDatabase.QueueQuery;
import com.mrsoft.arabicreference.editorial.infrastructure.EditorialDatabase.QueueRow;
import com.mrsoft.arabicreference.identity.application.AuditRecorder;
import com.mrsoft.arabicreference.identity.application.AuthorizationService;
import com.mrsoft.arabicreference.identity.domain.AuditEventType;
import com.mrsoft.arabicreference.identity.domain.PermissionCatalog;
import com.mrsoft.arabicreference.linguistics.application.PublicationBarrier;
import com.mrsoft.arabicreference.search.domain.PublishedSearchSource;
import com.mrsoft.arabicreference.search.domain.SearchDocument;
import com.mrsoft.arabicreference.search.domain.SearchIndex;
import com.mrsoft.arabicreference.shared.kernel.exception.ConflictException;
import com.mrsoft.arabicreference.shared.kernel.exception.FieldErrorDetail;
import com.mrsoft.arabicreference.shared.kernel.exception.ForbiddenOperationException;
import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import com.mrsoft.arabicreference.shared.kernel.id.Ids;
import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EditorialService implements PublicationBarrier {

    public static final UUID SEARCH_FINDING_ID = UUID.fromString("00000000-0000-4000-8000-0000000000aa");

    private final EditorialDatabase database;
    private final AuthorizationService authorization;
    private final AuditRecorder audit;
    private final TimeProvider time;
    private final SearchIndex index;
    private final List<PublishedSearchSource> searchSources;
    private final int bulkLimit;
    private final int scanLimit;

    public EditorialService(
            EditorialDatabase database,
            AuthorizationService authorization,
            AuditRecorder audit,
            TimeProvider time,
            SearchIndex index,
            List<PublishedSearchSource> searchSources,
            @Value("${app.editorial.bulk-limit:100}") int bulkLimit,
            @Value("${app.editorial.scan-limit:200}") int scanLimit) {
        this.database = database;
        this.authorization = authorization;
        this.audit = audit;
        this.time = time;
        this.index = index;
        this.searchSources = searchSources;
        this.bulkLimit = Math.min(Math.max(bulkLimit, 1), 100);
        this.scanLimit = Math.min(Math.max(scanLimit, 1), 500);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> dashboard() {
        require(PermissionCatalog.EDITORIAL_DASHBOARD_VIEW);
        Map<String, Long> counts = database.statusCounts();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("draft", counts.get("DRAFT"));
        body.put("inReview", counts.get("IN_REVIEW"));
        body.put("changesRequested", counts.get("CHANGES_REQUESTED"));
        body.put("verified", counts.get("VERIFIED"));
        body.put("readyToPublish", database.readyToPublish());
        body.put("published", counts.get("PUBLISHED"));
        body.put("archived", counts.get("ARCHIVED"));
        body.put("qualityIssues", database.openQualityIssues());
        body.put("emptyReview", counts.get("IN_REVIEW") == 0L);
        return body;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> queue(String type, String status, UUID creator, UUID reviewer, UUID assignee, String quality, String text, String sort, String direction, int page, int size) {
        require(PermissionCatalog.EDITORIAL_QUEUE_VIEW);
        return page(query(type, status, creator, reviewer, assignee, quality, text, null, sort, direction, page, size));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> reviews(String section, int page, int size) {
        require(PermissionCatalog.EDITORIAL_QUEUE_VIEW);
        return page(query(null, null, null, null, null, null, null, section, "updatedAt", "desc", page, size));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> publishing(int page, int size) {
        require(PermissionCatalog.EDITORIAL_QUEUE_VIEW);
        Map<String, Object> body = page(query(null, null, null, null, null, null, null, "publishing", "updatedAt", "desc", page, size));
        body.put("note", "المواد المتحققة مع موانع الجودة والحقوق والاستشهاد الظاهرة في المؤشرات.");
        return body;
    }

    @Transactional
    public Map<String, Object> assign(ContentType type, UUID contentId, AssignmentRole role, UUID assignee, Long expectedVersion) {
        require(PermissionCatalog.EDITORIAL_REVIEW_ASSIGN);
        QueueRow record = database.requireRecord(type, contentId);
        String permission = role == AssignmentRole.REVIEWER ? PermissionCatalog.CONTENT_REVIEW : PermissionCatalog.CONTENT_PUBLISH;
        if (!database.assigneeHas(assignee, permission)) {
            throw new ForbiddenOperationException("المكلَّف لا يملك صلاحية هذا الدور.");
        }
        if (assignee.equals(record.createdBy()) || (role == AssignmentRole.PUBLISHER && assignee.equals(record.reviewedBy()))) {
            throw new ForbiddenOperationException("التعيين لا يتجاوز مبدأ العينين.");
        }
        boolean reassignment = expectedVersion != null && expectedVersion > 0;
        database.assign(Ids.random(), type, contentId, role.name(), assignee, authorization.requireAccess().userId(), time.now(), expectedVersion);
        audit.record(authorization.requireAccess().userId(), reassignment ? AuditEventType.REVIEW_REASSIGNED : AuditEventType.REVIEW_ASSIGNED,
                type.name(), contentId.toString(), Map.of("role", role.name(), "assignee", assignee.toString()));
        return Map.of("contentType", type.name(), "contentId", contentId, "role", role.name(), "assigneeId", assignee);
    }

    @Transactional
    public Map<String, Object> comment(ContentType type, UUID contentId, CommentKind kind, String body, long expectedVersion) {
        require(PermissionCatalog.EDITORIAL_COMMENT_CREATE);
        QueueRow record = database.requireRecord(type, contentId);
        if (record.version() != expectedVersion) {
            throw new ConflictException("The record was updated by someone else. Reload and try again.");
        }
        if (body == null || body.isBlank() || body.length() > 1000) {
            throw new ValidationException("Comment is invalid.", List.of(new FieldErrorDetail("body", "اكتب تعليقًا واضحًا في حدود ألف حرف.")));
        }
        UUID id = Ids.random();
        database.comment(id, type, contentId, kind.name(), body.trim(), authorization.requireAccess().userId(), time.now());
        audit.record(authorization.requireAccess().userId(), AuditEventType.EDITORIAL_COMMENT_ADDED, type.name(), contentId.toString(), Map.of("comment", id.toString()));
        return Map.of("id", id, "status", "OPEN");
    }

    @Transactional
    public Map<String, Object> resolve(UUID commentId, long version) {
        require(PermissionCatalog.EDITORIAL_COMMENT_RESOLVE);
        database.resolveComment(commentId, version, authorization.requireAccess().userId(), time.now());
        audit.record(authorization.requireAccess().userId(), AuditEventType.EDITORIAL_COMMENT_RESOLVED, "editorial_comment", commentId.toString(), Map.of("status", "RESOLVED"));
        return Map.of("id", commentId, "status", "RESOLVED");
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> comments(ContentType type, UUID contentId, int page, int size) {
        require(PermissionCatalog.EDITORIAL_QUEUE_VIEW);
        database.requireRecord(type, contentId);
        return database.comments(type, contentId, bound(size), page * bound(size));
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> timeline(ContentType type, UUID contentId) {
        require(PermissionCatalog.EDITORIAL_QUEUE_VIEW);
        database.requireRecord(type, contentId);
        return database.timeline(contentId);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> diff(ContentType type, UUID contentId, Integer fromRevision, Integer toRevision) {
        require(PermissionCatalog.EDITORIAL_DIFF_VIEW);
        database.requireRecord(type, contentId);
        Map<String, Object> before;
        Map<String, Object> after;
        if (fromRevision != null || toRevision != null) {
            if (fromRevision == null || toRevision == null) {
                throw new ValidationException("Diff needs both revisions.", List.of(new FieldErrorDetail("revision", "حدّد المراجعة الأولى والثانية.")));
            }
            before = database.revisionSnapshot(contentId, fromRevision);
            after = database.revisionSnapshot(contentId, toRevision);
        } else {
            before = database.publishedSnapshot(type, contentId);
            after = database.currentFields(type, contentId);
        }
        return Map.of(
                "fromRevision", fromRevision == null ? 0 : fromRevision,
                "toRevision", toRevision == null ? 0 : toRevision,
                "changes", StructuredDiff.compare(before, after));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> readiness(ContentType type, UUID contentId) {
        require(PermissionCatalog.EDITORIAL_QUEUE_VIEW);
        QueueRow record = database.requireRecord(type, contentId);
        List<QualityProbe> probes = database.probes("RECORD", type, contentId, 1);
        List<String> reasons = new ArrayList<>();
        if (!"VERIFIED".equals(record.status())) {
            reasons.add("لم تصل المادة إلى حالة تم التحقق.");
        }
        if (!probes.isEmpty()) {
            for (QualityRules.FindingDraft finding : QualityRules.evaluate(probes.get(0))) {
                if (finding.severity() != com.mrsoft.arabicreference.editorial.domain.QualitySeverity.BLOCKER) {
                    continue;
                }
                reasons.add(finding.message());
            }
        }
        if (database.openBlockers(type, contentId) > 0 && reasons.stream().noneMatch(reason -> reason.contains("جودة") || reason.contains("مانع"))) {
            reasons.add("توجد موانع جودة مفتوحة.");
        }
        String state = reasons.isEmpty() ? "READY" : "BLOCKED";
        return Map.of("state", state, "reasons", reasons, "checklist", List.of(
                Map.of("label", "تمت المراجعة", "done", "VERIFIED".equals(record.status()) || "PUBLISHED".equals(record.status())),
                Map.of("label", "لا توجد موانع جودة", "done", database.openBlockers(type, contentId) == 0),
                Map.of("label", "الحقوق والمصادر تسمح", "done", reasons.stream().noneMatch(reason -> reason.contains("حقوق") || reason.contains("استشهاد") || reason.contains("مصدر"))),
                Map.of("label", "اللقطة قابلة للبناء", "done", record.title() != null && !record.title().isBlank())));
    }

    @Transactional
    public Map<String, Object> scan(String scope, ContentType type, UUID id) {
        require(PermissionCatalog.EDITORIAL_QUALITY_RUN);
        String normalized = switch (scope == null ? "" : scope) {
            case "RECORD", "TYPE", "PUBLISHED" -> scope;
            default -> throw new ValidationException("Scan scope is invalid.", List.of(new FieldErrorDetail("scope", "النطاق يجب أن يكون سجلًا أو نوعًا أو المنشور.")));
        };
        if ("RECORD".equals(normalized) && (type == null || id == null)) {
            throw new ValidationException("Record scan needs a target.", List.of(new FieldErrorDetail("contentId", "حدّد السجل.")));
        }
        if ("TYPE".equals(normalized) && type == null) {
            throw new ValidationException("Type scan needs a type.", List.of(new FieldErrorDetail("contentType", "حدّد النوع.")));
        }
        List<QualityProbe> probes = database.probes(normalized, type, id, scanLimit);
        List<Object[]> findings = new ArrayList<>();
        for (QualityProbe probe : probes) {
            for (QualityRules.FindingDraft finding : QualityRules.evaluate(probe)) {
                findings.add(new Object[] {probe.type().name(), probe.id(), finding.code(), finding.severity().name(), finding.message(), finding.field()});
            }
        }
        if ("PUBLISHED".equals(normalized)) {
            int missing = missingSearchDocuments();
            findings.add(new Object[] {ContentType.SEARCH_INDEX.name(), SEARCH_FINDING_ID, "SEARCH_INDEX_INCONSISTENCY", missing == 0 ? "INFO" : "WARNING",
                    missing == 0 ? "فهرس البحث متوافق مع المنشور." : "فهرس البحث ينقصه " + missing + " وثيقة.", "search"});
        }
        UUID scanId = Ids.random();
        if ("PUBLISHED".equals(normalized)) {
            probes.add(new QualityProbe(ContentType.SEARCH_INDEX, SEARCH_FINDING_ID, "فهرس البحث", null, "PUBLISHED", false, false, true, missingSearchDocuments() == 0,
                    0, 0, false, false, 0, 0, 0, false, 0, 0, 0, null, 0, 0, 0, 0, 0, 0));
        }
        database.replaceFindings(scanId, authorization.requireAccess().userId(), time.now(), normalized, type, id, probes, findings);
        audit.record(authorization.requireAccess().userId(), AuditEventType.QUALITY_SCAN_RUN, "quality_scan", scanId.toString(),
                Map.of("records", Integer.toString(probes.size()), "findings", Integer.toString(findings.size())));
        return Map.of("scanId", scanId, "records", probes.size(), "findings", findings.size());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> findings(String severity, String type, String code, String sort, int page, int size) {
        require(PermissionCatalog.EDITORIAL_QUALITY_VIEW);
        if (severity != null && !Set.of("INFO", "WARNING", "BLOCKER").contains(severity)) {
            throw new ValidationException("Severity is invalid.", List.of(new FieldErrorDetail("severity", "الشدة غير معروفة.")));
        }
        if (sort != null && !Set.of("severity", "detectedAt").contains(sort)) {
            throw new ValidationException("Sort is invalid.", List.of(new FieldErrorDetail("sort", "الترتيب غير مسموح.")));
        }
        int bounded = bound(size);
        return Map.of("items", database.findings(severity, type, code, sort, bounded, page * bounded), "page", page, "size", bounded);
    }

    @Transactional
    public Map<String, Object> bulk(String action, List<BulkItem> items, UUID assignee) {
        if (items == null || items.isEmpty() || items.size() > bulkLimit) {
            throw new ValidationException("Bulk limit exceeded.", List.of(new FieldErrorDetail("items", "الحد الأقصى هو " + bulkLimit + " سجلًا.")));
        }
        if ("PUBLISH".equals(action) || "DELETE".equals(action) || "VERIFY".equals(action)) {
            throw new ForbiddenOperationException("هذه العملية الجماعية غير مسموحة.");
        }
        if ("ASSIGN_REVIEWER".equals(action)) {
            require(PermissionCatalog.EDITORIAL_REVIEW_ASSIGN);
        } else if ("QUALITY_CHECK".equals(action)) {
            require(PermissionCatalog.EDITORIAL_QUALITY_RUN);
        } else {
            throw new ValidationException("Action is invalid.", List.of(new FieldErrorDetail("action", "العملية غير معروفة.")));
        }
        List<Map<String, Object>> results = new ArrayList<>();
        for (BulkItem item : items) {
            try {
                if ("ASSIGN_REVIEWER".equals(action)) {
                    assign(item.type(), item.id(), AssignmentRole.REVIEWER, assignee, item.version());
                } else {
                    scan("RECORD", item.type(), item.id());
                }
                results.add(Map.of("contentId", item.id(), "result", "SUCCESS"));
            } catch (RuntimeException exception) {
                results.add(Map.of("contentId", item.id(), "result", "FAILED", "reason", exception.getMessage()));
            }
        }
        return Map.of("items", results);
    }

    @Override
    public void assertNoOpenBlocker(String contentType, UUID contentId) {
        ContentType type = ContentType.parse(contentType).orElse(null);
        if (type != null && database.openBlockers(type, contentId) > 0) {
            throw new ConflictException("النشر موقوف لوجود مانع جودة مفتوح.");
        }
    }

    private int missingSearchDocuments() {
        Set<String> expected = new java.util.HashSet<>();
        for (PublishedSearchSource source : searchSources) {
            for (SearchDocument document : source.publishedDocuments()) {
                expected.add(document.entityType().name() + ":" + document.entityId());
            }
        }
        Set<String> stored = new java.util.HashSet<>();
        for (SearchIndex.StoredDocument document : index.storedDocuments()) {
            stored.add(document.type().name() + ":" + document.entityId());
        }
        int missing = 0;
        for (String key : expected) {
            if (!stored.contains(key)) {
                missing++;
            }
        }
        return missing;
    }

    private Map<String, Object> page(QueueQuery query) {
        List<Map<String, Object>> items = new ArrayList<>();
        for (QueueRow row : database.queue(query)) {
            ContentType type = ContentType.parse(row.contentType()).orElseThrow();
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("contentType", row.contentType());
            item.put("id", row.id());
            item.put("title", row.title());
            item.put("slug", row.slug());
            item.put("status", row.status());
            item.put("createdBy", row.createdBy());
            item.put("reviewedBy", row.reviewedBy());
            item.put("updatedAt", row.updatedAt());
            item.put("version", row.version());
            item.put("assigneeId", row.assigneeId());
            item.put("blockers", row.blockers());
            item.put("href", type.adminPath(row.id().toString()));
            items.add(item);
        }
        return Map.of("items", items, "total", database.queueCount(query), "page", query.offset() / query.size(), "size", query.size());
    }

    private QueueQuery query(String type, String status, UUID creator, UUID reviewer, UUID assignee, String quality, String text, String section, String sort, String direction, int page, int size) {
        ContentType parsed = type == null || type.isBlank() ? null : ContentType.parse(type).orElseThrow(() -> new ValidationException("Type is invalid.", List.of(new FieldErrorDetail("type", "نوع المحتوى غير معروف."))));
        if (status != null && !status.isBlank() && !QualityRules.STATUSES.contains(status)) {
            throw new ValidationException("Status is invalid.", List.of(new FieldErrorDetail("status", "الحالة غير معروفة.")));
        }
        if (quality != null && !quality.isBlank() && !Set.of("BLOCKED", "CLEAR").contains(quality)) {
            throw new ValidationException("Quality filter is invalid.", List.of(new FieldErrorDetail("quality", "مرشح الجودة غير معروف.")));
        }
        String column = switch (sort == null ? "updatedAt" : sort) {
            case "createdAt" -> "r.created_at";
            case "title" -> "r.title";
            case "updatedAt" -> "r.updated_at";
            default -> throw new ValidationException("Sort is invalid.", List.of(new FieldErrorDetail("sort", "الترتيب غير مسموح.")));
        };
        int bounded = bound(size);
        int safePage = Math.max(page, 0);
        return new QueueQuery(parsed, blank(status), creator, reviewer, assignee, authorization.requireAccess().userId(), null, null, blank(quality), blank(text), section, column, "asc".equalsIgnoreCase(direction) ? "ASC" : "DESC", bounded, safePage * bounded);
    }

    private void require(String permission) {
        authorization.requireAccess();
        if (!authorization.has(permission)) {
            throw new ForbiddenOperationException("You do not have permission to perform this operation.");
        }
    }

    private static int bound(int size) {
        if (size < 1) {
            return 20;
        }
        return Math.min(size, 50);
    }

    private static String blank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public record BulkItem(ContentType type, UUID id, Long version) {
    }
}
