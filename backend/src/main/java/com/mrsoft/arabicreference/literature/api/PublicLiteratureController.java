package com.mrsoft.arabicreference.literature.api;

import com.mrsoft.arabicreference.literature.application.LiteratureQueryService;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.PublicEra;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.PublicFigure;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.PublicLink;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.PublicWork;
import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import com.mrsoft.arabicreference.shared.web.api.ApiResponse;
import com.mrsoft.arabicreference.shared.web.api.ApiResponses;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/literature")
public class PublicLiteratureController {

    private final LiteratureQueryService literature;
    private final TimeProvider timeProvider;

    public PublicLiteratureController(LiteratureQueryService literature, TimeProvider timeProvider) {
        this.literature = literature;
        this.timeProvider = timeProvider;
    }

    @GetMapping("/eras")
    public ApiResponse<List<PublicEra>> eras() {
        return ApiResponses.ok(literature.eras(), timeProvider);
    }

    @GetMapping("/eras/{slug}")
    public ApiResponse<PublicEra> era(@PathVariable String slug) {
        return ApiResponses.ok(literature.era(slug), timeProvider);
    }

    @GetMapping("/genres")
    public ApiResponse<List<PublicLink>> genres() {
        return ApiResponses.ok(literature.genres(), timeProvider);
    }

    @GetMapping("/schools")
    public ApiResponse<List<PublicLink>> schools() {
        return ApiResponses.ok(literature.schools(), timeProvider);
    }

    @GetMapping("/figures/{slug}")
    public ApiResponse<PublicFigure> figure(@PathVariable String slug) {
        return ApiResponses.ok(literature.figure(slug), timeProvider);
    }

    @GetMapping("/works/{slug}")
    public ApiResponse<PublicWork> work(@PathVariable String slug) {
        return ApiResponses.ok(literature.work(slug), timeProvider);
    }
}
