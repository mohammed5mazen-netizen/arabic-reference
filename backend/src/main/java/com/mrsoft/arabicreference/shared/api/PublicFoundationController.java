package com.mrsoft.arabicreference.shared.api;

import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import com.mrsoft.arabicreference.shared.web.api.ApiResponse;
import com.mrsoft.arabicreference.shared.web.api.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public")
public class PublicFoundationController {

    private final TimeProvider timeProvider;

    public PublicFoundationController(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    @GetMapping("/foundation")
    public ApiResponse<PublicFoundationResponse> foundation() {
        return ApiResponses.ok(
                new PublicFoundationResponse(
                        "Arabic Reference",
                        "S0",
                        "public-anonymous",
                        "المرجع العربي متاح للقراءة العامة دون تسجيل دخول."),
                timeProvider);
    }

    public record PublicFoundationResponse(String project, String stage, String access, String message) {
    }
}
