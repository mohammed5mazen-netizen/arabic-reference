package com.mrsoft.arabicreference.admin.api;

import com.mrsoft.arabicreference.identity.application.AdminAuditService;
import com.mrsoft.arabicreference.identity.application.AdminDashboardService;
import com.mrsoft.arabicreference.identity.application.StaffViews.AuditView;
import com.mrsoft.arabicreference.identity.application.StaffViews.DashboardView;
import com.mrsoft.arabicreference.identity.application.StaffViews.PageResult;
import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import com.mrsoft.arabicreference.shared.web.api.ApiResponse;
import com.mrsoft.arabicreference.shared.web.api.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminAuditController {

    private final AdminAuditService audit;
    private final AdminDashboardService dashboard;
    private final TimeProvider timeProvider;

    public AdminAuditController(AdminAuditService audit, AdminDashboardService dashboard, TimeProvider timeProvider) {
        this.audit = audit;
        this.dashboard = dashboard;
        this.timeProvider = timeProvider;
    }

    @GetMapping("/audit")
    public ApiResponse<PageResult<AuditView>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponses.ok(audit.list(page, size), timeProvider);
    }

    @GetMapping("/dashboard")
    public ApiResponse<DashboardView> dashboard() {
        return ApiResponses.ok(dashboard.dashboard(), timeProvider);
    }
}
