package com.mrsoft.arabicreference.literature.api;

import com.mrsoft.arabicreference.literature.application.LiteratureAdminService;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.AliasDraft;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.CatalogSummary;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.CitationRequest;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.EraAdmin;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.EraDraft;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.EraLinkDraft;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.EraSummary;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.EraUpdate;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.ExcerptDraft;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.FigureAdmin;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.FigureDraft;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.FigureUpdate;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.GenreAdmin;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.GenreDraft;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.GenreUpdate;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.PageResult;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.ReasonRequest;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.ReviewItem;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.RightsUpdate;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.RoleDraft;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.SchoolAdmin;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.SchoolDraft;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.SchoolLinkDraft;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.SchoolUpdate;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.VersionRequest;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.WorkAdmin;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.WorkAliasDraft;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.WorkDraft;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.WorkFigureDraft;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.WorkSummary;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.WorkUpdate;
import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import com.mrsoft.arabicreference.shared.web.api.ApiResponse;
import com.mrsoft.arabicreference.shared.web.api.ApiResponses;
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
@RequestMapping("/api/v1/admin/literature")
public class AdminLiteratureController {

    private final LiteratureAdminService literature;
    private final TimeProvider timeProvider;

    public AdminLiteratureController(LiteratureAdminService literature, TimeProvider timeProvider) {
        this.literature = literature;
        this.timeProvider = timeProvider;
    }

    @GetMapping("/review")
    public ApiResponse<List<ReviewItem>> review() {
        return ApiResponses.ok(literature.reviewQueue(), timeProvider);
    }

    @GetMapping("/eras")
    public ApiResponse<PageResult<EraSummary>> eras(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponses.ok(literature.eras(page, size), timeProvider);
    }

    @GetMapping("/eras/{id}")
    public ApiResponse<EraAdmin> era(@PathVariable UUID id) {
        return ApiResponses.ok(literature.era(id), timeProvider);
    }

    @PostMapping("/eras")
    public ApiResponse<EraAdmin> createEra(@RequestBody EraDraft draft) {
        return ApiResponses.ok(literature.createEra(draft), timeProvider);
    }

    @PatchMapping("/eras/{id}")
    public ApiResponse<EraAdmin> updateEra(@PathVariable UUID id, @RequestBody EraUpdate update) {
        return ApiResponses.ok(literature.updateEra(id, update), timeProvider);
    }

    @PostMapping("/eras/{id}/citations")
    public ApiResponse<EraAdmin> citeEra(@PathVariable UUID id, @RequestBody CitationRequest request) {
        return ApiResponses.ok(literature.citeEra(id, request.version(), request.citationId()), timeProvider);
    }

    @PostMapping("/eras/{id}/submit")
    public ApiResponse<EraAdmin> submitEra(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(literature.submitEra(id, request.version()), timeProvider);
    }

    @PostMapping("/eras/{id}/verify")
    public ApiResponse<EraAdmin> verifyEra(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(literature.verifyEra(id, request.version()), timeProvider);
    }

    @PostMapping("/eras/{id}/request-changes")
    public ApiResponse<EraAdmin> requestEraChanges(@PathVariable UUID id, @RequestBody ReasonRequest request) {
        return ApiResponses.ok(literature.requestEraChanges(id, request.version(), request.reason()), timeProvider);
    }

    @PostMapping("/eras/{id}/publish")
    public ApiResponse<EraAdmin> publishEra(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(literature.publishEra(id, request.version()), timeProvider);
    }

    @PostMapping("/eras/{id}/archive")
    public ApiResponse<EraAdmin> archiveEra(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(literature.archiveEra(id, request.version()), timeProvider);
    }

    @GetMapping("/genres")
    public ApiResponse<PageResult<CatalogSummary>> genres(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponses.ok(literature.genres(page, size), timeProvider);
    }

    @GetMapping("/genres/{id}")
    public ApiResponse<GenreAdmin> genre(@PathVariable UUID id) {
        return ApiResponses.ok(literature.genre(id), timeProvider);
    }

    @PostMapping("/genres")
    public ApiResponse<GenreAdmin> createGenre(@RequestBody GenreDraft draft) {
        return ApiResponses.ok(literature.createGenre(draft), timeProvider);
    }

    @PatchMapping("/genres/{id}")
    public ApiResponse<GenreAdmin> updateGenre(@PathVariable UUID id, @RequestBody GenreUpdate update) {
        return ApiResponses.ok(literature.updateGenre(id, update), timeProvider);
    }

    @PostMapping("/genres/{id}/citations")
    public ApiResponse<GenreAdmin> citeGenre(@PathVariable UUID id, @RequestBody CitationRequest request) {
        return ApiResponses.ok(literature.citeGenre(id, request.version(), request.citationId()), timeProvider);
    }

    @PostMapping("/genres/{id}/submit")
    public ApiResponse<GenreAdmin> submitGenre(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(literature.submitGenre(id, request.version()), timeProvider);
    }

    @PostMapping("/genres/{id}/verify")
    public ApiResponse<GenreAdmin> verifyGenre(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(literature.verifyGenre(id, request.version()), timeProvider);
    }

    @PostMapping("/genres/{id}/request-changes")
    public ApiResponse<GenreAdmin> requestGenreChanges(@PathVariable UUID id, @RequestBody ReasonRequest request) {
        return ApiResponses.ok(literature.requestGenreChanges(id, request.version(), request.reason()), timeProvider);
    }

    @PostMapping("/genres/{id}/publish")
    public ApiResponse<GenreAdmin> publishGenre(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(literature.publishGenre(id, request.version()), timeProvider);
    }

    @PostMapping("/genres/{id}/archive")
    public ApiResponse<GenreAdmin> archiveGenre(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(literature.archiveGenre(id, request.version()), timeProvider);
    }

    @GetMapping("/schools")
    public ApiResponse<PageResult<CatalogSummary>> schools(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponses.ok(literature.schools(page, size), timeProvider);
    }

    @GetMapping("/schools/{id}")
    public ApiResponse<SchoolAdmin> school(@PathVariable UUID id) {
        return ApiResponses.ok(literature.school(id), timeProvider);
    }

    @PostMapping("/schools")
    public ApiResponse<SchoolAdmin> createSchool(@RequestBody SchoolDraft draft) {
        return ApiResponses.ok(literature.createSchool(draft), timeProvider);
    }

    @PatchMapping("/schools/{id}")
    public ApiResponse<SchoolAdmin> updateSchool(@PathVariable UUID id, @RequestBody SchoolUpdate update) {
        return ApiResponses.ok(literature.updateSchool(id, update), timeProvider);
    }

    @PostMapping("/schools/{id}/citations")
    public ApiResponse<SchoolAdmin> citeSchool(@PathVariable UUID id, @RequestBody CitationRequest request) {
        return ApiResponses.ok(literature.citeSchool(id, request.version(), request.citationId()), timeProvider);
    }

    @PostMapping("/schools/{id}/submit")
    public ApiResponse<SchoolAdmin> submitSchool(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(literature.submitSchool(id, request.version()), timeProvider);
    }

    @PostMapping("/schools/{id}/verify")
    public ApiResponse<SchoolAdmin> verifySchool(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(literature.verifySchool(id, request.version()), timeProvider);
    }

    @PostMapping("/schools/{id}/request-changes")
    public ApiResponse<SchoolAdmin> requestSchoolChanges(@PathVariable UUID id, @RequestBody ReasonRequest request) {
        return ApiResponses.ok(literature.requestSchoolChanges(id, request.version(), request.reason()), timeProvider);
    }

    @PostMapping("/schools/{id}/publish")
    public ApiResponse<SchoolAdmin> publishSchool(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(literature.publishSchool(id, request.version()), timeProvider);
    }

    @PostMapping("/schools/{id}/archive")
    public ApiResponse<SchoolAdmin> archiveSchool(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(literature.archiveSchool(id, request.version()), timeProvider);
    }

    @GetMapping("/figures")
    public ApiResponse<PageResult<CatalogSummary>> figures(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponses.ok(literature.figures(page, size), timeProvider);
    }

    @GetMapping("/figures/{id}")
    public ApiResponse<FigureAdmin> figure(@PathVariable UUID id) {
        return ApiResponses.ok(literature.figure(id), timeProvider);
    }

    @PostMapping("/figures")
    public ApiResponse<FigureAdmin> createFigure(@RequestBody FigureDraft draft) {
        return ApiResponses.ok(literature.createFigure(draft), timeProvider);
    }

    @PatchMapping("/figures/{id}")
    public ApiResponse<FigureAdmin> updateFigure(@PathVariable UUID id, @RequestBody FigureUpdate update) {
        return ApiResponses.ok(literature.updateFigure(id, update), timeProvider);
    }

    @PostMapping("/figures/{id}/aliases")
    public ApiResponse<FigureAdmin> addAlias(@PathVariable UUID id, @RequestBody AliasDraft draft) {
        return ApiResponses.ok(literature.addAlias(id, draft), timeProvider);
    }

    @PostMapping("/figures/{id}/roles")
    public ApiResponse<FigureAdmin> addRole(@PathVariable UUID id, @RequestBody RoleDraft draft) {
        return ApiResponses.ok(literature.addRole(id, draft), timeProvider);
    }

    @PostMapping("/figures/{id}/eras")
    public ApiResponse<FigureAdmin> linkEra(@PathVariable UUID id, @RequestBody EraLinkDraft draft) {
        return ApiResponses.ok(literature.linkEra(id, draft), timeProvider);
    }

    @PostMapping("/figures/{id}/schools")
    public ApiResponse<FigureAdmin> linkSchool(@PathVariable UUID id, @RequestBody SchoolLinkDraft draft) {
        return ApiResponses.ok(literature.linkSchool(id, draft), timeProvider);
    }

    @PostMapping("/figures/{id}/citations")
    public ApiResponse<FigureAdmin> citeFigure(@PathVariable UUID id, @RequestBody CitationRequest request) {
        return ApiResponses.ok(literature.citeFigure(id, request.version(), request.citationId()), timeProvider);
    }

    @PostMapping("/figures/{id}/submit")
    public ApiResponse<FigureAdmin> submitFigure(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(literature.submitFigure(id, request.version()), timeProvider);
    }

    @PostMapping("/figures/{id}/verify")
    public ApiResponse<FigureAdmin> verifyFigure(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(literature.verifyFigure(id, request.version()), timeProvider);
    }

    @PostMapping("/figures/{id}/request-changes")
    public ApiResponse<FigureAdmin> requestFigureChanges(@PathVariable UUID id, @RequestBody ReasonRequest request) {
        return ApiResponses.ok(literature.requestFigureChanges(id, request.version(), request.reason()), timeProvider);
    }

    @PostMapping("/figures/{id}/publish")
    public ApiResponse<FigureAdmin> publishFigure(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(literature.publishFigure(id, request.version()), timeProvider);
    }

    @PostMapping("/figures/{id}/archive")
    public ApiResponse<FigureAdmin> archiveFigure(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(literature.archiveFigure(id, request.version()), timeProvider);
    }

    @GetMapping("/works")
    public ApiResponse<PageResult<WorkSummary>> works(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponses.ok(literature.works(page, size), timeProvider);
    }

    @GetMapping("/works/{id}")
    public ApiResponse<WorkAdmin> work(@PathVariable UUID id) {
        return ApiResponses.ok(literature.work(id), timeProvider);
    }

    @PostMapping("/works")
    public ApiResponse<WorkAdmin> createWork(@RequestBody WorkDraft draft) {
        return ApiResponses.ok(literature.createWork(draft), timeProvider);
    }

    @PatchMapping("/works/{id}")
    public ApiResponse<WorkAdmin> updateWork(@PathVariable UUID id, @RequestBody WorkUpdate update) {
        return ApiResponses.ok(literature.updateWork(id, update), timeProvider);
    }

    @PostMapping("/works/{id}/aliases")
    public ApiResponse<WorkAdmin> addWorkAlias(@PathVariable UUID id, @RequestBody WorkAliasDraft draft) {
        return ApiResponses.ok(literature.addWorkAlias(id, draft), timeProvider);
    }

    @PostMapping("/works/{id}/figures")
    public ApiResponse<WorkAdmin> linkFigure(@PathVariable UUID id, @RequestBody WorkFigureDraft draft) {
        return ApiResponses.ok(literature.linkFigure(id, draft), timeProvider);
    }

    @PatchMapping("/works/{id}/rights")
    public ApiResponse<WorkAdmin> setRights(@PathVariable UUID id, @RequestBody RightsUpdate update) {
        return ApiResponses.ok(literature.setRights(id, update), timeProvider);
    }

    @PostMapping("/works/{id}/excerpts")
    public ApiResponse<WorkAdmin> addExcerpt(@PathVariable UUID id, @RequestBody ExcerptDraft draft) {
        return ApiResponses.ok(literature.addExcerpt(id, draft), timeProvider);
    }

    @PostMapping("/works/{id}/citations")
    public ApiResponse<WorkAdmin> citeWork(@PathVariable UUID id, @RequestBody CitationRequest request) {
        return ApiResponses.ok(literature.citeWork(id, request.version(), request.citationId()), timeProvider);
    }

    @PostMapping("/works/{id}/submit")
    public ApiResponse<WorkAdmin> submitWork(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(literature.submitWork(id, request.version()), timeProvider);
    }

    @PostMapping("/works/{id}/verify")
    public ApiResponse<WorkAdmin> verifyWork(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(literature.verifyWork(id, request.version()), timeProvider);
    }

    @PostMapping("/works/{id}/request-changes")
    public ApiResponse<WorkAdmin> requestWorkChanges(@PathVariable UUID id, @RequestBody ReasonRequest request) {
        return ApiResponses.ok(literature.requestWorkChanges(id, request.version(), request.reason()), timeProvider);
    }

    @PostMapping("/works/{id}/publish")
    public ApiResponse<WorkAdmin> publishWork(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(literature.publishWork(id, request.version()), timeProvider);
    }

    @PostMapping("/works/{id}/archive")
    public ApiResponse<WorkAdmin> archiveWork(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(literature.archiveWork(id, request.version()), timeProvider);
    }
}
