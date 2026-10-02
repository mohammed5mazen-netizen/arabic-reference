package com.mrsoft.arabicreference.morphology.api;

import com.mrsoft.arabicreference.morphology.application.MorphologyService;
import com.mrsoft.arabicreference.morphology.application.MorphologyViews.AnalysisDraft;
import com.mrsoft.arabicreference.morphology.application.MorphologyViews.AnalysisUpdate;
import com.mrsoft.arabicreference.morphology.application.MorphologyViews.AnalysisView;
import com.mrsoft.arabicreference.morphology.application.MorphologyViews.CitationRequest;
import com.mrsoft.arabicreference.morphology.application.MorphologyViews.CoverageView;
import com.mrsoft.arabicreference.morphology.application.MorphologyViews.PageResult;
import com.mrsoft.arabicreference.morphology.application.MorphologyViews.PatternDraft;
import com.mrsoft.arabicreference.morphology.application.MorphologyViews.PatternUpdate;
import com.mrsoft.arabicreference.morphology.application.MorphologyViews.PatternView;
import com.mrsoft.arabicreference.morphology.application.MorphologyViews.ReasonRequest;
import com.mrsoft.arabicreference.morphology.application.MorphologyViews.RuleChange;
import com.mrsoft.arabicreference.morphology.application.MorphologyViews.RuleView;
import com.mrsoft.arabicreference.morphology.application.MorphologyViews.VersionRequest;
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
@RequestMapping("/api/v1/admin/morphology")
public class AdminMorphologyController {

    private final MorphologyService morphology;
    private final TimeProvider timeProvider;

    public AdminMorphologyController(MorphologyService morphology, TimeProvider timeProvider) {
        this.morphology = morphology;
        this.timeProvider = timeProvider;
    }

    @GetMapping("/patterns")
    public ApiResponse<PageResult<PatternView>> patterns(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponses.ok(morphology.patterns(page, size), timeProvider);
    }

    @PostMapping("/patterns")
    public ApiResponse<PatternView> createPattern(@RequestBody PatternDraft draft) {
        return ApiResponses.ok(morphology.createPattern(draft), timeProvider);
    }

    @PatchMapping("/patterns/{id}")
    public ApiResponse<PatternView> updatePattern(@PathVariable UUID id, @RequestBody PatternUpdate update) {
        return ApiResponses.ok(morphology.updatePattern(id, update), timeProvider);
    }

    @PostMapping("/patterns/{id}/activate")
    public ApiResponse<PatternView> activate(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(morphology.setPatternStatus(id, request.version(), true), timeProvider);
    }

    @PostMapping("/patterns/{id}/deactivate")
    public ApiResponse<PatternView> deactivate(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(morphology.setPatternStatus(id, request.version(), false), timeProvider);
    }

    @GetMapping("/analyses")
    public ApiResponse<PageResult<AnalysisView>> analyses(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponses.ok(morphology.analyses(page, size), timeProvider);
    }

    @PostMapping("/analyses")
    public ApiResponse<AnalysisView> createAnalysis(@RequestBody AnalysisDraft draft) {
        return ApiResponses.ok(morphology.createAnalysis(draft), timeProvider);
    }

    @PatchMapping("/analyses/{id}")
    public ApiResponse<AnalysisView> updateAnalysis(@PathVariable UUID id, @RequestBody AnalysisUpdate update) {
        return ApiResponses.ok(morphology.updateAnalysis(id, update.version(), update.draft()), timeProvider);
    }

    @PostMapping("/analyses/{id}/submit")
    public ApiResponse<AnalysisView> submit(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(morphology.submit(id, request.version()), timeProvider);
    }

    @PostMapping("/analyses/{id}/request-changes")
    public ApiResponse<AnalysisView> requestChanges(@PathVariable UUID id, @RequestBody ReasonRequest request) {
        return ApiResponses.ok(morphology.requestChanges(id, request.version(), request.reason()), timeProvider);
    }

    @PostMapping("/analyses/{id}/verify")
    public ApiResponse<AnalysisView> verify(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(morphology.verify(id, request.version()), timeProvider);
    }

    @PostMapping("/analyses/{id}/publish")
    public ApiResponse<AnalysisView> publish(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(morphology.publish(id, request.version()), timeProvider);
    }

    @PostMapping("/analyses/{id}/archive")
    public ApiResponse<AnalysisView> archive(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(morphology.archive(id, request.version()), timeProvider);
    }

    @PostMapping("/analyses/{id}/citations")
    public ApiResponse<AnalysisView> cite(@PathVariable UUID id, @RequestBody CitationRequest request) {
        return ApiResponses.ok(morphology.attachCitation(id, request.version(), request.citationId()), timeProvider);
    }

    @GetMapping("/rules")
    public ApiResponse<List<RuleView>> rules() {
        return ApiResponses.ok(morphology.rules(), timeProvider);
    }

    @PostMapping("/rules/{code}")
    public ApiResponse<RuleView> rule(@PathVariable String code, @RequestBody RuleChange change) {
        return ApiResponses.ok(morphology.setRuleEnabled(code, change.enabled(), change.version()), timeProvider);
    }

    @GetMapping("/coverage")
    public ApiResponse<CoverageView> coverage() {
        return ApiResponses.ok(morphology.coverage(), timeProvider);
    }
}
