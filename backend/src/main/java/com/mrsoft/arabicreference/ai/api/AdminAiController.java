package com.mrsoft.arabicreference.ai.api;

import com.mrsoft.arabicreference.ai.application.AiViews.AdminAiStatus;
import com.mrsoft.arabicreference.ai.application.AssistantService;
import com.mrsoft.arabicreference.identity.domain.PermissionCatalog;
import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import com.mrsoft.arabicreference.shared.web.api.ApiResponse;
import com.mrsoft.arabicreference.shared.web.api.ApiResponses;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminAiController {

    private final AssistantService assistant;
    private final TimeProvider time;

    public AdminAiController(AssistantService assistant, TimeProvider time) {
        this.assistant = assistant;
        this.time = time;
    }

    @GetMapping("/api/v1/admin/ai")
    @PreAuthorize("@authz.has('" + PermissionCatalog.AI_ADMIN_VIEW + "')")
    public ApiResponse<AdminAiStatus> status() {
        return ApiResponses.ok(assistant.adminStatus(), time);
    }
}
