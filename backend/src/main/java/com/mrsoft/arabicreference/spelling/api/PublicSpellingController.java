package com.mrsoft.arabicreference.spelling.api;

import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import com.mrsoft.arabicreference.shared.web.api.ApiResponse;
import com.mrsoft.arabicreference.shared.web.api.ApiResponses;
import com.mrsoft.arabicreference.spelling.application.SpellingQueryService;
import com.mrsoft.arabicreference.spelling.application.SpellingViews.PublicLink;
import com.mrsoft.arabicreference.spelling.application.SpellingViews.PublicRule;
import com.mrsoft.arabicreference.spelling.application.SpellingViews.PublicTopic;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/spelling")
public class PublicSpellingController {

    private final SpellingQueryService spelling;
    private final TimeProvider timeProvider;

    public PublicSpellingController(SpellingQueryService spelling, TimeProvider timeProvider) {
        this.spelling = spelling;
        this.timeProvider = timeProvider;
    }

    @GetMapping("/topics")
    public ApiResponse<List<PublicLink>> topics() {
        return ApiResponses.ok(spelling.topics(), timeProvider);
    }

    @GetMapping("/topics/{slug}")
    public ApiResponse<PublicTopic> topic(@PathVariable String slug) {
        return ApiResponses.ok(spelling.topic(slug), timeProvider);
    }

    @GetMapping("/rules/{slug}")
    public ApiResponse<PublicRule> rule(@PathVariable String slug) {
        return ApiResponses.ok(spelling.rule(slug), timeProvider);
    }
}
