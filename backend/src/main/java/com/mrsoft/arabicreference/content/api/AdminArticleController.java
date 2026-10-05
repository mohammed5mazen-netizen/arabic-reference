package com.mrsoft.arabicreference.content.api;

import com.mrsoft.arabicreference.content.application.ArticleAdminService;
import com.mrsoft.arabicreference.content.application.ArticleViews.ArticleAdmin;
import com.mrsoft.arabicreference.content.application.ArticleViews.ArticleDraft;
import com.mrsoft.arabicreference.content.application.ArticleViews.ArticleSummary;
import com.mrsoft.arabicreference.content.application.ArticleViews.ArticleUpdate;
import com.mrsoft.arabicreference.content.application.ArticleViews.CitationDraft;
import com.mrsoft.arabicreference.content.application.ArticleViews.PageResult;
import com.mrsoft.arabicreference.content.application.ArticleViews.ReasonRequest;
import com.mrsoft.arabicreference.content.application.ArticleViews.RelationDraft;
import com.mrsoft.arabicreference.content.application.ArticleViews.ReviewItem;
import com.mrsoft.arabicreference.content.application.ArticleViews.SectionDraft;
import com.mrsoft.arabicreference.content.application.ArticleViews.TagDraft;
import com.mrsoft.arabicreference.content.application.ArticleViews.VersionRequest;
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
@RequestMapping("/api/v1/admin/articles")
public class AdminArticleController {

    private final ArticleAdminService articles;
    private final TimeProvider timeProvider;

    public AdminArticleController(ArticleAdminService articles, TimeProvider timeProvider) {
        this.articles = articles;
        this.timeProvider = timeProvider;
    }

    @GetMapping
    public ApiResponse<PageResult<ArticleSummary>> articles(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponses.ok(articles.articles(page, size), timeProvider);
    }

    @GetMapping("/review")
    public ApiResponse<List<ReviewItem>> review() {
        return ApiResponses.ok(articles.reviewQueue(), timeProvider);
    }

    @GetMapping("/{id}")
    public ApiResponse<ArticleAdmin> article(@PathVariable UUID id) {
        return ApiResponses.ok(articles.article(id), timeProvider);
    }

    @PostMapping
    public ApiResponse<ArticleAdmin> create(@RequestBody ArticleDraft draft) {
        return ApiResponses.ok(articles.create(draft), timeProvider);
    }

    @PatchMapping("/{id}")
    public ApiResponse<ArticleAdmin> update(@PathVariable UUID id, @RequestBody ArticleUpdate update) {
        return ApiResponses.ok(articles.update(id, update), timeProvider);
    }

    @PostMapping("/{id}/sections")
    public ApiResponse<ArticleAdmin> addSection(@PathVariable UUID id, @RequestBody SectionDraft draft) {
        return ApiResponses.ok(articles.addSection(id, draft), timeProvider);
    }

    @PostMapping("/{id}/tags")
    public ApiResponse<ArticleAdmin> addTag(@PathVariable UUID id, @RequestBody TagDraft draft) {
        return ApiResponses.ok(articles.addTag(id, draft), timeProvider);
    }

    @PostMapping("/{id}/citations")
    public ApiResponse<ArticleAdmin> cite(@PathVariable UUID id, @RequestBody CitationDraft draft) {
        return ApiResponses.ok(articles.cite(id, draft), timeProvider);
    }

    @PostMapping("/{id}/relations")
    public ApiResponse<ArticleAdmin> addRelation(@PathVariable UUID id, @RequestBody RelationDraft draft) {
        return ApiResponses.ok(articles.addRelation(id, draft), timeProvider);
    }

    @PostMapping("/{id}/submit")
    public ApiResponse<ArticleAdmin> submit(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(articles.submit(id, request.version()), timeProvider);
    }

    @PostMapping("/{id}/verify")
    public ApiResponse<ArticleAdmin> verify(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(articles.verify(id, request.version()), timeProvider);
    }

    @PostMapping("/{id}/request-changes")
    public ApiResponse<ArticleAdmin> requestChanges(@PathVariable UUID id, @RequestBody ReasonRequest request) {
        return ApiResponses.ok(articles.requestChanges(id, request.version(), request.reason()), timeProvider);
    }

    @PostMapping("/{id}/publish")
    public ApiResponse<ArticleAdmin> publish(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(articles.publish(id, request.version()), timeProvider);
    }

    @PostMapping("/{id}/archive")
    public ApiResponse<ArticleAdmin> archive(@PathVariable UUID id, @RequestBody VersionRequest request) {
        return ApiResponses.ok(articles.archive(id, request.version()), timeProvider);
    }
}
