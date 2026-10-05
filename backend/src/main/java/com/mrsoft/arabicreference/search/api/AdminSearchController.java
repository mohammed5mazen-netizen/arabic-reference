package com.mrsoft.arabicreference.search.api;

import com.mrsoft.arabicreference.search.application.SearchAdminService;
import com.mrsoft.arabicreference.search.application.SearchViews.SearchStatusView;
import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import com.mrsoft.arabicreference.shared.web.api.ApiResponse;
import com.mrsoft.arabicreference.shared.web.api.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/search")
public class AdminSearchController {

    private final SearchAdminService search;
    private final TimeProvider timeProvider;

    public AdminSearchController(SearchAdminService search, TimeProvider timeProvider) {
        this.search = search;
        this.timeProvider = timeProvider;
    }

    @GetMapping("/status")
    public ApiResponse<SearchStatusView> status() {
        return ApiResponses.ok(search.status(), timeProvider);
    }

    @PostMapping("/reindex")
    public ApiResponse<SearchStatusView> reindex() {
        return ApiResponses.ok(search.rebuild(), timeProvider);
    }

    @PostMapping("/repair")
    public ApiResponse<SearchStatusView> repair() {
        return ApiResponses.ok(search.repair(), timeProvider);
    }
}
