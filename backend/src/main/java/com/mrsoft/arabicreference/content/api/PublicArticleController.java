package com.mrsoft.arabicreference.content.api;

import com.mrsoft.arabicreference.content.application.ArticleQueryService;
import com.mrsoft.arabicreference.content.application.ArticleViews.PublicArticle;
import com.mrsoft.arabicreference.content.application.ArticleViews.PublicArticleLink;
import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import com.mrsoft.arabicreference.shared.web.api.ApiResponse;
import com.mrsoft.arabicreference.shared.web.api.ApiResponses;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/articles")
public class PublicArticleController {

    private final ArticleQueryService articles;
    private final TimeProvider timeProvider;

    public PublicArticleController(ArticleQueryService articles, TimeProvider timeProvider) {
        this.articles = articles;
        this.timeProvider = timeProvider;
    }

    @GetMapping
    public ApiResponse<List<PublicArticleLink>> articles() {
        return ApiResponses.ok(articles.articles(), timeProvider);
    }

    @GetMapping("/{slug}")
    public ApiResponse<PublicArticle> article(@PathVariable String slug) {
        return ApiResponses.ok(articles.article(slug), timeProvider);
    }
}
