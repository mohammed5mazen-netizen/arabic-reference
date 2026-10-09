package com.mrsoft.arabicreference.search.api;

import com.mrsoft.arabicreference.search.application.SearchQueryService;
import com.mrsoft.arabicreference.search.application.SearchViews.SearchPageView;
import com.mrsoft.arabicreference.search.application.SearchViews.SuggestionView;
import com.mrsoft.arabicreference.search.domain.SearchTuning;
import com.mrsoft.arabicreference.shared.web.ClientAddresses;
import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import com.mrsoft.arabicreference.shared.web.api.ApiResponse;
import com.mrsoft.arabicreference.shared.web.api.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/search")
public class PublicSearchController {

    private final SearchQueryService search;
    private final TimeProvider timeProvider;

    public PublicSearchController(SearchQueryService search, TimeProvider timeProvider) {
        this.search = search;
        this.timeProvider = timeProvider;
    }

    @GetMapping
    public ApiResponse<SearchPageView> search(
            @RequestParam String q,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String partOfSpeech,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "" + SearchTuning.DEFAULT_PAGE_SIZE) int size,
            HttpServletRequest request) {
        return ApiResponses.ok(search.search(q, type, partOfSpeech, page, size, ClientAddresses.read(request)), timeProvider);
    }

    @GetMapping("/suggestions")
    public ApiResponse<List<SuggestionView>> suggestions(@RequestParam String q, HttpServletRequest request) {
        return ApiResponses.ok(search.suggest(q, ClientAddresses.read(request)), timeProvider);
    }
}
