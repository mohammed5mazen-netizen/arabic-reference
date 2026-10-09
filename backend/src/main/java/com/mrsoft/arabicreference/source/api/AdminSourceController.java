package com.mrsoft.arabicreference.source.api;

import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import com.mrsoft.arabicreference.shared.web.api.ApiResponse;
import com.mrsoft.arabicreference.shared.web.api.ApiResponses;
import com.mrsoft.arabicreference.source.application.SourceAdminService;
import com.mrsoft.arabicreference.source.application.SourceViews.CitationDraft;
import com.mrsoft.arabicreference.source.application.SourceViews.CitationView;
import com.mrsoft.arabicreference.source.application.SourceViews.PageResult;
import com.mrsoft.arabicreference.source.application.SourceViews.SourceDraft;
import com.mrsoft.arabicreference.source.application.SourceViews;
import com.mrsoft.arabicreference.source.application.SourceViews.SourceView;
import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/sources")
public class AdminSourceController {

    private final SourceAdminService sources;
    private final TimeProvider timeProvider;

    public AdminSourceController(SourceAdminService sources, TimeProvider timeProvider) {
        this.sources = sources;
        this.timeProvider = timeProvider;
    }

    @GetMapping
    public ApiResponse<PageResult<SourceView>> list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponses.ok(sources.list(page, size), timeProvider);
    }

    @GetMapping("/duplicates")
    public ApiResponse<List<SourceViews.DuplicateGroup>> duplicates() {
        return ApiResponses.ok(sources.duplicates(), timeProvider);
    }

    @GetMapping("/{id}")
    public ApiResponse<SourceView> get(@PathVariable UUID id) {
        return ApiResponses.ok(sources.get(id), timeProvider);
    }

    @GetMapping("/{id}/usage")
    public ApiResponse<List<SourceViews.SourceUsage>> usage(@PathVariable UUID id) {
        return ApiResponses.ok(sources.usage(id), timeProvider);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Map<String, String>> remove(@PathVariable UUID id, @Valid @RequestBody VersionBody request) {
        sources.remove(id, request.version());
        return ApiResponses.ok(Map.of("status", "DELETED"), timeProvider);
    }

    @PostMapping
    public ApiResponse<SourceView> create(@RequestBody SourceDraft draft) {
        return ApiResponses.ok(sources.create(draft), timeProvider);
    }

    @PatchMapping("/{id}")
    public ApiResponse<SourceView> update(@PathVariable UUID id, @Valid @RequestBody VersionedSource request) {
        return ApiResponses.ok(sources.update(id, request.version(), request.draft()), timeProvider);
    }

    @PostMapping("/{id}/citations")
    public ApiResponse<CitationView> cite(@PathVariable UUID id, @RequestBody CitationDraft draft) {
        return ApiResponses.ok(sources.cite(id, draft), timeProvider);
    }

    @PostMapping("/{id}/submit")
    public ApiResponse<SourceView> submit(@PathVariable UUID id, @Valid @RequestBody VersionBody request) {
        return ApiResponses.ok(sources.submit(id, request.version()), timeProvider);
    }

    @PostMapping("/{id}/verify")
    public ApiResponse<SourceView> verify(@PathVariable UUID id, @Valid @RequestBody VersionBody request) {
        return ApiResponses.ok(sources.verify(id, request.version()), timeProvider);
    }

    @PostMapping("/{id}/return")
    public ApiResponse<SourceView> requestChanges(@PathVariable UUID id, @Valid @RequestBody ReasonBody request) {
        return ApiResponses.ok(sources.requestChanges(id, request.version(), request.reason()), timeProvider);
    }

    @PostMapping("/{id}/publish")
    public ApiResponse<SourceView> publish(@PathVariable UUID id, @Valid @RequestBody VersionBody request) {
        return ApiResponses.ok(sources.publish(id, request.version()), timeProvider);
    }

    @PostMapping("/{id}/archive")
    public ApiResponse<SourceView> archive(@PathVariable UUID id, @Valid @RequestBody VersionBody request) {
        return ApiResponses.ok(sources.archive(id, request.version()), timeProvider);
    }

    public record VersionBody(@NotNull Long version) {
    }

    public record ReasonBody(@NotNull Long version, String reason) {
    }

    public record VersionedSource(@NotNull Long version, SourceDraft draft) {
    }
}
