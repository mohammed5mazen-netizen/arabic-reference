package com.mrsoft.arabicreference.spelling.api;

import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import com.mrsoft.arabicreference.shared.web.api.ApiResponse;
import com.mrsoft.arabicreference.shared.web.api.ApiResponses;
import com.mrsoft.arabicreference.spelling.application.SpellingAdminService;
import com.mrsoft.arabicreference.spelling.application.SpellingViews.CitationRequest;
import com.mrsoft.arabicreference.spelling.application.SpellingViews.ClauseDraft;
import com.mrsoft.arabicreference.spelling.application.SpellingViews.ExampleDraft;
import com.mrsoft.arabicreference.spelling.application.SpellingViews.PageResult;
import com.mrsoft.arabicreference.spelling.application.SpellingViews.ReasonRequest;
import com.mrsoft.arabicreference.spelling.application.SpellingViews.ReviewItem;
import com.mrsoft.arabicreference.spelling.application.SpellingViews.RuleAdmin;
import com.mrsoft.arabicreference.spelling.application.SpellingViews.RuleDraft;
import com.mrsoft.arabicreference.spelling.application.SpellingViews.RuleUpdate;
import com.mrsoft.arabicreference.spelling.application.SpellingViews.TopicAdmin;
import com.mrsoft.arabicreference.spelling.application.SpellingViews.TopicDraft;
import com.mrsoft.arabicreference.spelling.application.SpellingViews.TopicSummary;
import com.mrsoft.arabicreference.spelling.application.SpellingViews.TopicUpdate;
import com.mrsoft.arabicreference.spelling.application.SpellingViews.VersionRequest;
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
@RequestMapping("/api/v1/admin/spelling")
public class AdminSpellingController {

    private final SpellingAdminService spelling;
    private final TimeProvider timeProvider;

    public AdminSpellingController(SpellingAdminService spelling, TimeProvider timeProvider) {
        this.spelling = spelling;
        this.timeProvider = timeProvider;
    }

    @GetMapping("/topics")
    public ApiResponse<PageResult<TopicSummary>> topics(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponses.ok(spelling.topics(page, size), timeProvider);
    }

    @GetMapping("/topics/{id}")
    public ApiResponse<TopicAdmin> topic(@PathVariable UUID id) {
        return ApiResponses.ok(spelling.topic(id), timeProvider);
    }

    @PostMapping("/topics")
    public ApiResponse<TopicAdmin> createTopic(@RequestBody TopicDraft draft) {
        return ApiResponses.ok(spelling.createTopic(draft), timeProvider);
    }

    @PatchMapping("/topics/{id}")
    public ApiResponse<TopicAdmin> updateTopic(@PathVariable UUID id, @RequestBody TopicUpdate update) {
        return ApiResponses.ok(spelling.updateTopic(id, update), timeProvider);
    }

    @PostMapping("/topics/{id}/citations")
    public ApiResponse<TopicAdmin> citeTopic(@PathVariable UUID id, @RequestBody CitationRequest request) {
        return ApiResponses.ok(spelling.citeTopic(id, request.version(), request.citationId()), timeProvider);
    }

    @PostMapping("/topics/{id}/submit")
    public ApiResponse<TopicAdmin> submitTopic(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(spelling.submitTopic(id, request.version()), timeProvider);
    }

    @PostMapping("/topics/{id}/verify")
    public ApiResponse<TopicAdmin> verifyTopic(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(spelling.verifyTopic(id, request.version()), timeProvider);
    }

    @PostMapping("/topics/{id}/request-changes")
    public ApiResponse<TopicAdmin> requestTopicChanges(@PathVariable UUID id, @RequestBody ReasonRequest request) {
        return ApiResponses.ok(spelling.requestTopicChanges(id, request.version(), request.reason()), timeProvider);
    }

    @PostMapping("/topics/{id}/publish")
    public ApiResponse<TopicAdmin> publishTopic(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(spelling.publishTopic(id, request.version()), timeProvider);
    }

    @PostMapping("/topics/{id}/archive")
    public ApiResponse<TopicAdmin> archiveTopic(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(spelling.archiveTopic(id, request.version()), timeProvider);
    }

    @PostMapping("/rules")
    public ApiResponse<RuleAdmin> createRule(@RequestBody RuleDraft draft) {
        return ApiResponses.ok(spelling.createRule(draft), timeProvider);
    }

    @GetMapping("/rules/{id}")
    public ApiResponse<RuleAdmin> rule(@PathVariable UUID id) {
        return ApiResponses.ok(spelling.rule(id), timeProvider);
    }

    @PatchMapping("/rules/{id}")
    public ApiResponse<RuleAdmin> updateRule(@PathVariable UUID id, @RequestBody RuleUpdate update) {
        return ApiResponses.ok(spelling.updateRule(id, update), timeProvider);
    }

    @PostMapping("/rules/{id}/clauses")
    public ApiResponse<RuleAdmin> addClause(@PathVariable UUID id, @RequestBody ClauseDraft draft) {
        return ApiResponses.ok(spelling.addClause(id, draft), timeProvider);
    }

    @PostMapping("/rules/{id}/examples")
    public ApiResponse<RuleAdmin> addExample(@PathVariable UUID id, @RequestBody ExampleDraft draft) {
        return ApiResponses.ok(spelling.addExample(id, draft), timeProvider);
    }

    @PostMapping("/rules/{id}/citations")
    public ApiResponse<RuleAdmin> citeRule(@PathVariable UUID id, @RequestBody CitationRequest request) {
        return ApiResponses.ok(spelling.citeRule(id, request.version(), request.citationId()), timeProvider);
    }

    @PostMapping("/rules/{id}/submit")
    public ApiResponse<RuleAdmin> submitRule(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(spelling.submitRule(id, request.version()), timeProvider);
    }

    @PostMapping("/rules/{id}/verify")
    public ApiResponse<RuleAdmin> verifyRule(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(spelling.verifyRule(id, request.version()), timeProvider);
    }

    @PostMapping("/rules/{id}/request-changes")
    public ApiResponse<RuleAdmin> requestRuleChanges(@PathVariable UUID id, @RequestBody ReasonRequest request) {
        return ApiResponses.ok(spelling.requestRuleChanges(id, request.version(), request.reason()), timeProvider);
    }

    @PostMapping("/rules/{id}/publish")
    public ApiResponse<RuleAdmin> publishRule(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(spelling.publishRule(id, request.version()), timeProvider);
    }

    @PostMapping("/rules/{id}/archive")
    public ApiResponse<RuleAdmin> archiveRule(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(spelling.archiveRule(id, request.version()), timeProvider);
    }

    @GetMapping("/review")
    public ApiResponse<List<ReviewItem>> review() {
        return ApiResponses.ok(spelling.reviewQueue(), timeProvider);
    }
}
