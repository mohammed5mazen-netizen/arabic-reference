package com.mrsoft.arabicreference.dictionary.api;

import com.mrsoft.arabicreference.dictionary.application.DictionaryEntryService;
import com.mrsoft.arabicreference.dictionary.application.DictionaryViews.AdminEntry;
import com.mrsoft.arabicreference.dictionary.application.DictionaryViews.EntryDraft;
import com.mrsoft.arabicreference.dictionary.application.DictionaryViews.ExampleDraft;
import com.mrsoft.arabicreference.dictionary.application.DictionaryViews.FormDraft;
import com.mrsoft.arabicreference.dictionary.application.DictionaryViews.PageResult;
import com.mrsoft.arabicreference.dictionary.application.DictionaryViews.RelationDraft;
import com.mrsoft.arabicreference.dictionary.application.DictionaryViews.RevisionView;
import com.mrsoft.arabicreference.dictionary.application.DictionaryViews.SenseDraft;
import com.mrsoft.arabicreference.dictionary.application.RootAdminService;
import com.mrsoft.arabicreference.dictionary.application.DictionaryViews.AdminRoot;
import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import com.mrsoft.arabicreference.shared.web.api.ApiResponse;
import com.mrsoft.arabicreference.shared.web.api.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/dictionary")
public class AdminDictionaryController {

    private final DictionaryEntryService entries;
    private final RootAdminService roots;
    private final TimeProvider timeProvider;

    public AdminDictionaryController(DictionaryEntryService entries, RootAdminService roots, TimeProvider timeProvider) {
        this.entries = entries;
        this.roots = roots;
        this.timeProvider = timeProvider;
    }

    @GetMapping("/entries")
    public ApiResponse<PageResult<AdminEntry>> entries(
            @RequestParam(required = false) String word,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponses.ok(entries.list(word, page, size), timeProvider);
    }

    @GetMapping("/review")
    public ApiResponse<PageResult<AdminEntry>> review(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponses.ok(entries.reviewQueue(page, size), timeProvider);
    }

    @PostMapping("/entries")
    public ApiResponse<AdminEntry> create(@RequestBody EntryDraft draft) {
        return ApiResponses.ok(entries.create(draft), timeProvider);
    }

    @GetMapping("/entries/{id}")
    public ApiResponse<AdminEntry> get(@PathVariable UUID id) {
        return ApiResponses.ok(entries.get(id), timeProvider);
    }

    @PatchMapping("/entries/{id}")
    public ApiResponse<AdminEntry> update(@PathVariable UUID id, @Valid @RequestBody VersionedEntry request) {
        return ApiResponses.ok(entries.update(id, request.version(), request.draft()), timeProvider);
    }

    @PostMapping("/entries/{id}/senses")
    public ApiResponse<AdminEntry> addSense(@PathVariable UUID id, @Valid @RequestBody VersionedSense request) {
        return ApiResponses.ok(entries.addSense(id, request.version(), request.sense()), timeProvider);
    }

    @PostMapping("/entries/{id}/forms")
    public ApiResponse<AdminEntry> addForm(@PathVariable UUID id, @Valid @RequestBody VersionedForm request) {
        return ApiResponses.ok(entries.addForm(id, request.version(), request.form()), timeProvider);
    }

    @PostMapping("/senses/{id}/examples")
    public ApiResponse<AdminEntry> addExample(@PathVariable UUID id, @Valid @RequestBody VersionedExample request) {
        return ApiResponses.ok(entries.addExample(id, request.version(), request.example()), timeProvider);
    }

    @PostMapping("/entries/{id}/relations")
    public ApiResponse<AdminEntry> addRelation(@PathVariable UUID id, @Valid @RequestBody VersionedRelation request) {
        return ApiResponses.ok(entries.addRelation(id, request.version(), request.relation()), timeProvider);
    }

    @PostMapping("/senses/{id}/citations")
    public ApiResponse<AdminEntry> linkSense(@PathVariable UUID id, @Valid @RequestBody CitationLink request) {
        return ApiResponses.ok(entries.linkSenseCitation(id, request.version(), request.citationId()), timeProvider);
    }

    @PostMapping("/entries/{id}/citations")
    public ApiResponse<AdminEntry> linkEntry(@PathVariable UUID id, @Valid @RequestBody CitationLink request) {
        return ApiResponses.ok(entries.linkEntryCitation(id, request.version(), request.citationId()), timeProvider);
    }

    @PostMapping("/relations/{id}/citations")
    public ApiResponse<AdminEntry> linkRelation(@PathVariable UUID id, @Valid @RequestBody CitationOnly request) {
        return ApiResponses.ok(entries.linkRelationCitation(id, request.citationId()), timeProvider);
    }

    @PostMapping("/entries/{id}/submit")
    public ApiResponse<AdminEntry> submit(@PathVariable UUID id, @Valid @RequestBody VersionBody request) {
        return ApiResponses.ok(entries.submit(id, request.version()), timeProvider);
    }

    @PostMapping("/entries/{id}/return")
    public ApiResponse<AdminEntry> requestChanges(@PathVariable UUID id, @Valid @RequestBody ReasonBody request) {
        return ApiResponses.ok(entries.requestChanges(id, request.version(), request.reason()), timeProvider);
    }

    @PostMapping("/entries/{id}/verify")
    public ApiResponse<AdminEntry> verify(@PathVariable UUID id, @Valid @RequestBody VersionBody request) {
        return ApiResponses.ok(entries.verify(id, request.version()), timeProvider);
    }

    @PostMapping("/entries/{id}/publish")
    public ApiResponse<AdminEntry> publish(@PathVariable UUID id, @Valid @RequestBody VersionBody request) {
        return ApiResponses.ok(entries.publish(id, request.version()), timeProvider);
    }

    @PostMapping("/entries/{id}/archive")
    public ApiResponse<AdminEntry> archive(@PathVariable UUID id, @Valid @RequestBody VersionBody request) {
        return ApiResponses.ok(entries.archive(id, request.version()), timeProvider);
    }

    @GetMapping("/entries/{id}/revisions")
    public ApiResponse<List<RevisionView>> revisions(@PathVariable UUID id) {
        return ApiResponses.ok(entries.revisions(id), timeProvider);
    }

    @GetMapping("/roots")
    public ApiResponse<PageResult<AdminRoot>> roots(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponses.ok(roots.list(page, size), timeProvider);
    }

    @PostMapping("/roots")
    public ApiResponse<AdminRoot> createRoot(@RequestBody RootBody request) {
        return ApiResponses.ok(roots.create(request.original(), request.notes()), timeProvider);
    }

    @PatchMapping("/roots/{id}")
    public ApiResponse<AdminRoot> updateRoot(@PathVariable UUID id, @RequestBody VersionedRoot request) {
        return ApiResponses.ok(roots.update(id, request.version(), request.original(), request.notes()), timeProvider);
    }

    @PostMapping("/roots/{id}/citations")
    public ApiResponse<AdminRoot> linkRoot(@PathVariable UUID id, @Valid @RequestBody CitationLink request) {
        return ApiResponses.ok(roots.linkCitation(id, request.version(), request.citationId()), timeProvider);
    }

    @PostMapping("/roots/{id}/submit")
    public ApiResponse<AdminRoot> submitRoot(@PathVariable UUID id, @Valid @RequestBody VersionBody request) {
        return ApiResponses.ok(roots.submit(id, request.version()), timeProvider);
    }

    @PostMapping("/roots/{id}/verify")
    public ApiResponse<AdminRoot> verifyRoot(@PathVariable UUID id, @Valid @RequestBody VersionBody request) {
        return ApiResponses.ok(roots.verify(id, request.version()), timeProvider);
    }

    @PostMapping("/roots/{id}/publish")
    public ApiResponse<AdminRoot> publishRoot(@PathVariable UUID id, @Valid @RequestBody VersionBody request) {
        return ApiResponses.ok(roots.publish(id, request.version()), timeProvider);
    }

    @PostMapping("/roots/{id}/archive")
    public ApiResponse<AdminRoot> archiveRoot(@PathVariable UUID id, @Valid @RequestBody VersionBody request) {
        return ApiResponses.ok(roots.archive(id, request.version()), timeProvider);
    }

    public record VersionBody(@NotNull Long version) {
    }

    public record ReasonBody(@NotNull Long version, String reason) {
    }

    public record VersionedEntry(@NotNull Long version, EntryDraft draft) {
    }

    public record VersionedSense(@NotNull Long version, SenseDraft sense) {
    }

    public record VersionedForm(@NotNull Long version, FormDraft form) {
    }

    public record VersionedExample(@NotNull Long version, ExampleDraft example) {
    }

    public record VersionedRelation(@NotNull Long version, RelationDraft relation) {
    }

    public record CitationLink(@NotNull Long version, @NotNull UUID citationId) {
    }

    public record CitationOnly(@NotNull UUID citationId) {
    }

    public record RootBody(String original, String notes) {
    }

    public record VersionedRoot(@NotNull Long version, String original, String notes) {
    }
}
