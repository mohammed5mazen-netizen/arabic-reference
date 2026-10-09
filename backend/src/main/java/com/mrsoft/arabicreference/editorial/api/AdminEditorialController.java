package com.mrsoft.arabicreference.editorial.api;

import com.mrsoft.arabicreference.editorial.application.EditorialService;
import com.mrsoft.arabicreference.editorial.application.EditorialService.BulkItem;
import com.mrsoft.arabicreference.editorial.domain.AssignmentRole;
import com.mrsoft.arabicreference.editorial.domain.CommentKind;
import com.mrsoft.arabicreference.editorial.domain.ContentType;
import com.mrsoft.arabicreference.identity.domain.PermissionCatalog;
import com.mrsoft.arabicreference.shared.kernel.exception.FieldErrorDetail;
import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import com.mrsoft.arabicreference.shared.web.api.ApiResponse;
import com.mrsoft.arabicreference.shared.web.api.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/editorial")
public class AdminEditorialController {

    private final EditorialService editorial;
    private final TimeProvider time;

    public AdminEditorialController(EditorialService editorial, TimeProvider time) {
        this.editorial = editorial;
        this.time = time;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("@authz.has('" + PermissionCatalog.EDITORIAL_DASHBOARD_VIEW + "')")
    public ApiResponse<Map<String, Object>> dashboard() {
        return ApiResponses.ok(editorial.dashboard(), time);
    }

    @GetMapping("/queue")
    @PreAuthorize("@authz.has('" + PermissionCatalog.EDITORIAL_QUEUE_VIEW + "')")
    public ApiResponse<Map<String, Object>> queue(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID creator,
            @RequestParam(required = false) UUID reviewer,
            @RequestParam(required = false) UUID assignee,
            @RequestParam(required = false) String quality,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "updatedAt") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponses.ok(editorial.queue(type, status, creator, reviewer, assignee, quality, q, sort, direction, page, size), time);
    }

    @GetMapping("/reviews")
    @PreAuthorize("@authz.has('" + PermissionCatalog.EDITORIAL_QUEUE_VIEW + "')")
    public ApiResponse<Map<String, Object>> reviews(@RequestParam(defaultValue = "waiting") String section, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponses.ok(editorial.reviews(section, page, size), time);
    }

    @GetMapping("/publishing")
    @PreAuthorize("@authz.has('" + PermissionCatalog.EDITORIAL_QUEUE_VIEW + "')")
    public ApiResponse<Map<String, Object>> publishing(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponses.ok(editorial.publishing(page, size), time);
    }

    @PostMapping("/records/{type}/{id}/assign")
    @PreAuthorize("@authz.has('" + PermissionCatalog.EDITORIAL_REVIEW_ASSIGN + "')")
    public ApiResponse<Map<String, Object>> assign(@PathVariable String type, @PathVariable UUID id, @Valid @RequestBody AssignBody body) {
        return ApiResponses.ok(editorial.assign(contentType(type), id, body.role(), body.assigneeId(), body.version()), time);
    }

    @PostMapping("/records/{type}/{id}/comments")
    @PreAuthorize("@authz.has('" + PermissionCatalog.EDITORIAL_COMMENT_CREATE + "')")
    public ApiResponse<Map<String, Object>> comment(@PathVariable String type, @PathVariable UUID id, @Valid @RequestBody CommentBody body) {
        return ApiResponses.ok(editorial.comment(contentType(type), id, body.kind(), body.body(), body.expectedVersion()), time);
    }

    @GetMapping("/records/{type}/{id}/comments")
    @PreAuthorize("@authz.has('" + PermissionCatalog.EDITORIAL_QUEUE_VIEW + "')")
    public ApiResponse<List<Map<String, Object>>> comments(@PathVariable String type, @PathVariable UUID id, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponses.ok(editorial.comments(contentType(type), id, page, size), time);
    }

    @PostMapping("/comments/{id}/resolve")
    @PreAuthorize("@authz.has('" + PermissionCatalog.EDITORIAL_COMMENT_RESOLVE + "')")
    public ApiResponse<Map<String, Object>> resolve(@PathVariable UUID id, @Valid @RequestBody VersionBody body) {
        return ApiResponses.ok(editorial.resolve(id, body.version()), time);
    }

    @GetMapping("/records/{type}/{id}/timeline")
    @PreAuthorize("@authz.has('" + PermissionCatalog.EDITORIAL_QUEUE_VIEW + "')")
    public ApiResponse<List<Map<String, Object>>> timeline(@PathVariable String type, @PathVariable UUID id) {
        return ApiResponses.ok(editorial.timeline(contentType(type), id), time);
    }

    @GetMapping("/records/{type}/{id}/diff")
    @PreAuthorize("@authz.has('" + PermissionCatalog.EDITORIAL_DIFF_VIEW + "')")
    public ApiResponse<Map<String, Object>> diff(@PathVariable String type, @PathVariable UUID id, @RequestParam(required = false) Integer fromRevision, @RequestParam(required = false) Integer toRevision) {
        return ApiResponses.ok(editorial.diff(contentType(type), id, fromRevision, toRevision), time);
    }

    @GetMapping("/records/{type}/{id}/readiness")
    @PreAuthorize("@authz.has('" + PermissionCatalog.EDITORIAL_QUEUE_VIEW + "')")
    public ApiResponse<Map<String, Object>> readiness(@PathVariable String type, @PathVariable UUID id) {
        return ApiResponses.ok(editorial.readiness(contentType(type), id), time);
    }

    @PostMapping("/quality/scans")
    @PreAuthorize("@authz.has('" + PermissionCatalog.EDITORIAL_QUALITY_RUN + "')")
    public ApiResponse<Map<String, Object>> scan(@RequestBody ScanBody body) {
        return ApiResponses.ok(editorial.scan(body.scope(), body.contentType() == null ? null : contentType(body.contentType()), body.contentId()), time);
    }

    @GetMapping("/quality/findings")
    @PreAuthorize("@authz.has('" + PermissionCatalog.EDITORIAL_QUALITY_VIEW + "')")
    public ApiResponse<Map<String, Object>> findings(
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String code,
            @RequestParam(defaultValue = "detectedAt") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponses.ok(editorial.findings(severity, type, code, sort, page, size), time);
    }

    @PostMapping("/bulk")
    public ApiResponse<Map<String, Object>> bulk(@RequestBody BulkBody body) {
        List<BulkItem> items = body.items() == null ? List.of() : body.items().stream()
                .map(item -> new BulkItem(contentType(item.type()), item.id(), item.version()))
                .toList();
        return ApiResponses.ok(editorial.bulk(body.action(), items, body.assigneeId()), time);
    }

    private static ContentType contentType(String raw) {
        return ContentType.parse(raw).orElseThrow(() -> new ValidationException("Type is invalid.", List.of(new FieldErrorDetail("type", "نوع المحتوى غير معروف."))));
    }

    public record AssignBody(@NotNull AssignmentRole role, @NotNull UUID assigneeId, Long version) {
    }

    public record CommentBody(@NotNull CommentKind kind, String body, @NotNull Long expectedVersion) {
    }

    public record VersionBody(@NotNull Long version) {
    }

    public record ScanBody(String scope, String contentType, UUID contentId) {
    }

    public record BulkBody(String action, UUID assigneeId, List<BulkRecord> items) {
    }

    public record BulkRecord(String type, UUID id, Long version) {
    }
}
