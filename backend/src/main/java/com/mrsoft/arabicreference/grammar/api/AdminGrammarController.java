package com.mrsoft.arabicreference.grammar.api;

import com.mrsoft.arabicreference.grammar.application.GrammarAdminService;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.AliasDraft;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.AnnotationAdmin;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.AnnotationDraft;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.CitationRequest;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.ComponentDraft;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.ConceptAdmin;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.ConceptDraft;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.ConceptRuleRequest;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.ConceptSummary;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.ConceptUpdate;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.DependenciesRequest;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.ExampleDraft;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.ExampleSummary;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.PageResult;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.ParentChange;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.PrerequisiteRequest;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.ReasonRequest;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.RelationDraft;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.ReviewItem;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.RoleDraft;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.RoleView;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.RuleAdmin;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.RuleDraft;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.RuleSummary;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.RuleUpdate;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.TokensRequest;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.TopicAdmin;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.TopicDraft;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.TopicSummary;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.TopicUpdate;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.VersionRequest;
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
@RequestMapping("/api/v1/admin/grammar")
public class AdminGrammarController {

    private final GrammarAdminService grammar;
    private final TimeProvider timeProvider;

    public AdminGrammarController(GrammarAdminService grammar, TimeProvider timeProvider) {
        this.grammar = grammar;
        this.timeProvider = timeProvider;
    }

    @GetMapping("/topics")
    public ApiResponse<PageResult<TopicSummary>> topics(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponses.ok(grammar.topics(page, size), timeProvider);
    }

    @GetMapping("/topics/{id}")
    public ApiResponse<TopicAdmin> topic(@PathVariable UUID id) {
        return ApiResponses.ok(grammar.topic(id), timeProvider);
    }

    @PostMapping("/topics")
    public ApiResponse<TopicAdmin> createTopic(@RequestBody TopicDraft draft) {
        return ApiResponses.ok(grammar.createTopic(draft), timeProvider);
    }

    @PatchMapping("/topics/{id}")
    public ApiResponse<TopicAdmin> updateTopic(@PathVariable UUID id, @RequestBody TopicUpdate update) {
        return ApiResponses.ok(grammar.updateTopic(id, update), timeProvider);
    }

    @PostMapping("/topics/{id}/parent")
    public ApiResponse<TopicAdmin> moveTopic(@PathVariable UUID id, @RequestBody ParentChange change) {
        return ApiResponses.ok(grammar.moveTopic(id, change), timeProvider);
    }

    @PostMapping("/topics/{id}/prerequisites")
    public ApiResponse<TopicAdmin> prerequisite(@PathVariable UUID id, @RequestBody PrerequisiteRequest request) {
        return ApiResponses.ok(grammar.addPrerequisite(id, request), timeProvider);
    }

    @PostMapping("/topics/{id}/citations")
    public ApiResponse<TopicAdmin> citeTopic(@PathVariable UUID id, @RequestBody CitationRequest request) {
        return ApiResponses.ok(grammar.citeTopic(id, request.version(), request.citationId()), timeProvider);
    }

    @PostMapping("/topics/{id}/submit")
    public ApiResponse<TopicAdmin> submitTopic(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(grammar.submitTopic(id, request.version()), timeProvider);
    }

    @PostMapping("/topics/{id}/request-changes")
    public ApiResponse<TopicAdmin> returnTopic(@PathVariable UUID id, @RequestBody ReasonRequest request) {
        return ApiResponses.ok(grammar.requestTopicChanges(id, request.version(), request.reason()), timeProvider);
    }

    @PostMapping("/topics/{id}/verify")
    public ApiResponse<TopicAdmin> verifyTopic(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(grammar.verifyTopic(id, request.version()), timeProvider);
    }

    @PostMapping("/topics/{id}/publish")
    public ApiResponse<TopicAdmin> publishTopic(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(grammar.publishTopic(id, request.version()), timeProvider);
    }

    @PostMapping("/topics/{id}/archive")
    public ApiResponse<TopicAdmin> archiveTopic(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(grammar.archiveTopic(id, request.version()), timeProvider);
    }

    @GetMapping("/rules")
    public ApiResponse<PageResult<RuleSummary>> rules(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponses.ok(grammar.rules(page, size), timeProvider);
    }

    @GetMapping("/rules/{id}")
    public ApiResponse<RuleAdmin> rule(@PathVariable UUID id) {
        return ApiResponses.ok(grammar.rule(id), timeProvider);
    }

    @PostMapping("/rules")
    public ApiResponse<RuleAdmin> createRule(@RequestBody RuleDraft draft) {
        return ApiResponses.ok(grammar.createRule(draft), timeProvider);
    }

    @PatchMapping("/rules/{id}")
    public ApiResponse<RuleAdmin> updateRule(@PathVariable UUID id, @RequestBody RuleUpdate update) {
        return ApiResponses.ok(grammar.updateRule(id, update), timeProvider);
    }

    @PostMapping("/rules/{id}/components")
    public ApiResponse<RuleAdmin> component(@PathVariable UUID id, @RequestBody ComponentDraft draft) {
        return ApiResponses.ok(grammar.addComponent(id, draft), timeProvider);
    }

    @PostMapping("/rules/{id}/components/{componentId}/citations")
    public ApiResponse<RuleAdmin> citeComponent(@PathVariable UUID id, @PathVariable UUID componentId, @RequestBody CitationRequest request) {
        return ApiResponses.ok(grammar.citeComponent(id, componentId, request.version(), request.citationId()), timeProvider);
    }

    @PostMapping("/rules/{id}/examples")
    public ApiResponse<RuleAdmin> example(@PathVariable UUID id, @RequestBody ExampleDraft draft) {
        return ApiResponses.ok(grammar.addExample(id, draft), timeProvider);
    }

    @PostMapping("/rules/{id}/relations")
    public ApiResponse<RuleAdmin> relation(@PathVariable UUID id, @RequestBody RelationDraft draft) {
        return ApiResponses.ok(grammar.addRelation(id, draft), timeProvider);
    }

    @PostMapping("/rules/{id}/citations")
    public ApiResponse<RuleAdmin> citeRule(@PathVariable UUID id, @RequestBody CitationRequest request) {
        return ApiResponses.ok(grammar.citeRule(id, request.version(), request.citationId()), timeProvider);
    }

    @PostMapping("/rules/{id}/submit")
    public ApiResponse<RuleAdmin> submitRule(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(grammar.submitRule(id, request.version()), timeProvider);
    }

    @PostMapping("/rules/{id}/request-changes")
    public ApiResponse<RuleAdmin> returnRule(@PathVariable UUID id, @RequestBody ReasonRequest request) {
        return ApiResponses.ok(grammar.requestRuleChanges(id, request.version(), request.reason()), timeProvider);
    }

    @PostMapping("/rules/{id}/verify")
    public ApiResponse<RuleAdmin> verifyRule(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(grammar.verifyRule(id, request.version()), timeProvider);
    }

    @PostMapping("/rules/{id}/publish")
    public ApiResponse<RuleAdmin> publishRule(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(grammar.publishRule(id, request.version()), timeProvider);
    }

    @PostMapping("/rules/{id}/archive")
    public ApiResponse<RuleAdmin> archiveRule(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(grammar.archiveRule(id, request.version()), timeProvider);
    }

    @GetMapping("/examples")
    public ApiResponse<PageResult<ExampleSummary>> examples(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponses.ok(grammar.examples(page, size), timeProvider);
    }

    @GetMapping("/concepts")
    public ApiResponse<PageResult<ConceptSummary>> concepts(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponses.ok(grammar.concepts(page, size), timeProvider);
    }

    @GetMapping("/concepts/{id}")
    public ApiResponse<ConceptAdmin> concept(@PathVariable UUID id) {
        return ApiResponses.ok(grammar.concept(id), timeProvider);
    }

    @PostMapping("/concepts")
    public ApiResponse<ConceptAdmin> createConcept(@RequestBody ConceptDraft draft) {
        return ApiResponses.ok(grammar.createConcept(draft), timeProvider);
    }

    @PatchMapping("/concepts/{id}")
    public ApiResponse<ConceptAdmin> updateConcept(@PathVariable UUID id, @RequestBody ConceptUpdate update) {
        return ApiResponses.ok(grammar.updateConcept(id, update), timeProvider);
    }

    @PostMapping("/concepts/{id}/aliases")
    public ApiResponse<ConceptAdmin> alias(@PathVariable UUID id, @RequestBody AliasDraft draft) {
        return ApiResponses.ok(grammar.addAlias(id, draft), timeProvider);
    }

    @PostMapping("/concepts/{id}/rules")
    public ApiResponse<ConceptAdmin> conceptRule(@PathVariable UUID id, @RequestBody ConceptRuleRequest request) {
        return ApiResponses.ok(grammar.linkConceptRule(id, request), timeProvider);
    }

    @PostMapping("/concepts/{id}/citations")
    public ApiResponse<ConceptAdmin> citeConcept(@PathVariable UUID id, @RequestBody CitationRequest request) {
        return ApiResponses.ok(grammar.citeConcept(id, request.version(), request.citationId()), timeProvider);
    }

    @PostMapping("/concepts/{id}/submit")
    public ApiResponse<ConceptAdmin> submitConcept(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(grammar.submitConcept(id, request.version()), timeProvider);
    }

    @PostMapping("/concepts/{id}/request-changes")
    public ApiResponse<ConceptAdmin> returnConcept(@PathVariable UUID id, @RequestBody ReasonRequest request) {
        return ApiResponses.ok(grammar.requestConceptChanges(id, request.version(), request.reason()), timeProvider);
    }

    @PostMapping("/concepts/{id}/verify")
    public ApiResponse<ConceptAdmin> verifyConcept(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(grammar.verifyConcept(id, request.version()), timeProvider);
    }

    @PostMapping("/concepts/{id}/publish")
    public ApiResponse<ConceptAdmin> publishConcept(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(grammar.publishConcept(id, request.version()), timeProvider);
    }

    @PostMapping("/concepts/{id}/archive")
    public ApiResponse<ConceptAdmin> archiveConcept(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(grammar.archiveConcept(id, request.version()), timeProvider);
    }

    @GetMapping("/annotations/{id}")
    public ApiResponse<AnnotationAdmin> annotation(@PathVariable UUID id) {
        return ApiResponses.ok(grammar.annotation(id), timeProvider);
    }

    @PostMapping("/annotations")
    public ApiResponse<AnnotationAdmin> createAnnotation(@RequestBody AnnotationDraft draft) {
        return ApiResponses.ok(grammar.createAnnotation(draft), timeProvider);
    }

    @PostMapping("/annotations/{id}/tokens")
    public ApiResponse<AnnotationAdmin> tokens(@PathVariable UUID id, @RequestBody TokensRequest request) {
        return ApiResponses.ok(grammar.replaceTokens(id, request), timeProvider);
    }

    @PostMapping("/annotations/{id}/dependencies")
    public ApiResponse<AnnotationAdmin> dependencies(@PathVariable UUID id, @RequestBody DependenciesRequest request) {
        return ApiResponses.ok(grammar.replaceDependencies(id, request), timeProvider);
    }

    @PostMapping("/annotations/{id}/submit")
    public ApiResponse<AnnotationAdmin> submitAnnotation(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(grammar.submitAnnotation(id, request.version()), timeProvider);
    }

    @PostMapping("/annotations/{id}/request-changes")
    public ApiResponse<AnnotationAdmin> returnAnnotation(@PathVariable UUID id, @RequestBody ReasonRequest request) {
        return ApiResponses.ok(grammar.requestAnnotationChanges(id, request.version(), request.reason()), timeProvider);
    }

    @PostMapping("/annotations/{id}/verify")
    public ApiResponse<AnnotationAdmin> verifyAnnotation(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(grammar.verifyAnnotation(id, request.version()), timeProvider);
    }

    @PostMapping("/annotations/{id}/publish")
    public ApiResponse<AnnotationAdmin> publishAnnotation(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(grammar.publishAnnotation(id, request.version()), timeProvider);
    }

    @PostMapping("/annotations/{id}/archive")
    public ApiResponse<AnnotationAdmin> archiveAnnotation(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(grammar.archiveAnnotation(id, request.version()), timeProvider);
    }

    @GetMapping("/roles")
    public ApiResponse<List<RoleView>> roles() {
        return ApiResponses.ok(grammar.roles(), timeProvider);
    }

    @PostMapping("/roles")
    public ApiResponse<RoleView> createRole(@RequestBody RoleDraft draft) {
        return ApiResponses.ok(grammar.createRole(draft), timeProvider);
    }

    @GetMapping("/review")
    public ApiResponse<PageResult<ReviewItem>> review(@RequestParam(defaultValue = "IN_REVIEW") String status, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponses.ok(grammar.review(status, page, size), timeProvider);
    }
}
