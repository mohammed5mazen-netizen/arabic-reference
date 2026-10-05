package com.mrsoft.arabicreference.rhetoric.api;

import com.mrsoft.arabicreference.rhetoric.application.RhetoricQueryService;
import com.mrsoft.arabicreference.rhetoric.application.RhetoricViews.PublicDevice;
import com.mrsoft.arabicreference.rhetoric.application.RhetoricViews.PublicLink;
import com.mrsoft.arabicreference.rhetoric.application.RhetoricViews.PublicTopic;
import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import com.mrsoft.arabicreference.shared.web.api.ApiResponse;
import com.mrsoft.arabicreference.shared.web.api.ApiResponses;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/rhetoric")
public class PublicRhetoricController {
    private final RhetoricQueryService rhetoric;
    private final TimeProvider timeProvider;

    public PublicRhetoricController(RhetoricQueryService rhetoric, TimeProvider timeProvider) {
        this.rhetoric = rhetoric;
        this.timeProvider = timeProvider;
    }

    @GetMapping("/topics")
    public ApiResponse<List<PublicLink>> topics() { return ApiResponses.ok(rhetoric.topics(), timeProvider); }

    @GetMapping("/topics/{slug}")
    public ApiResponse<PublicTopic> topic(@PathVariable String slug) { return ApiResponses.ok(rhetoric.topic(slug), timeProvider); }

    @GetMapping("/devices/{slug}")
    public ApiResponse<PublicDevice> device(@PathVariable String slug) { return ApiResponses.ok(rhetoric.device(slug), timeProvider); }
}
