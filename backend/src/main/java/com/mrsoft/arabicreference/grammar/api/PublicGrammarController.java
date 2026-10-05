package com.mrsoft.arabicreference.grammar.api;

import com.mrsoft.arabicreference.grammar.application.GrammarQueryService;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.PageResult;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.PublicAnnotation;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.PublicConcept;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.PublicIndex;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.PublicRule;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.PublicTopic;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.SearchHit;
import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import com.mrsoft.arabicreference.shared.web.api.ApiResponse;
import com.mrsoft.arabicreference.shared.web.api.ApiResponses;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/grammar")
public class PublicGrammarController {

    private final GrammarQueryService grammar;
    private final TimeProvider timeProvider;

    public PublicGrammarController(GrammarQueryService grammar, TimeProvider timeProvider) {
        this.grammar = grammar;
        this.timeProvider = timeProvider;
    }

    @GetMapping("/topics")
    public ApiResponse<PublicIndex> topics(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponses.ok(grammar.index(page, size), timeProvider);
    }

    @GetMapping("/topics/{slug}")
    public ApiResponse<PublicTopic> topic(@PathVariable String slug) {
        return ApiResponses.ok(grammar.topic(slug), timeProvider);
    }

    @GetMapping("/rules/{slug}")
    public ApiResponse<PublicRule> rule(@PathVariable String slug) {
        return ApiResponses.ok(grammar.rule(slug), timeProvider);
    }

    @GetMapping("/concepts/{slug}")
    public ApiResponse<PublicConcept> concept(@PathVariable String slug) {
        return ApiResponses.ok(grammar.concept(slug), timeProvider);
    }

    @GetMapping("/annotations/{id}")
    public ApiResponse<PublicAnnotation> annotation(@PathVariable UUID id) {
        return ApiResponses.ok(grammar.annotation(id), timeProvider);
    }

    @GetMapping("/search")
    public ApiResponse<PageResult<SearchHit>> search(@RequestParam String q, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponses.ok(grammar.search(q, page, size), timeProvider);
    }
}
