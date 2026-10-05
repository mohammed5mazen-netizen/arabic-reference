package com.mrsoft.arabicreference.tools.api;

import com.mrsoft.arabicreference.identity.domain.PermissionCatalog;
import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import com.mrsoft.arabicreference.shared.web.api.ApiResponse;
import com.mrsoft.arabicreference.shared.web.api.ApiResponses;
import com.mrsoft.arabicreference.tools.application.LinguisticToolsService;
import com.mrsoft.arabicreference.tools.application.ToolViews.AdminTools;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminToolsController {

    private final LinguisticToolsService tools;
    private final TimeProvider time;

    public AdminToolsController(LinguisticToolsService tools, TimeProvider time) {
        this.tools = tools;
        this.time = time;
    }

    @GetMapping("/api/v1/admin/tools")
    @PreAuthorize("@authz.has('" + PermissionCatalog.TOOLS_VIEW + "')")
    public ApiResponse<AdminTools> status() {
        return ApiResponses.ok(tools.adminStatus(), time);
    }
}
