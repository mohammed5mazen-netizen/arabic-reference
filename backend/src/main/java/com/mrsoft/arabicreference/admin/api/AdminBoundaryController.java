package com.mrsoft.arabicreference.admin.api;

import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import com.mrsoft.arabicreference.shared.web.api.ApiResponse;
import com.mrsoft.arabicreference.shared.web.api.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Administrative boundary only. S0 has no admin identity, so this route stays closed.
 */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminBoundaryController {

    private final TimeProvider timeProvider;

    public AdminBoundaryController(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    @GetMapping("/foundation")
    public ApiResponse<AdminBoundaryResponse> foundation() {
        return ApiResponses.ok(
                new AdminBoundaryResponse("admin", "editorial-staff-only"),
                timeProvider);
    }

    public record AdminBoundaryResponse(String boundary, String access) {
    }
}
