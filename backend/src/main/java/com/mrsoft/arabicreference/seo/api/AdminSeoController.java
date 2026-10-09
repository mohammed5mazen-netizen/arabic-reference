package com.mrsoft.arabicreference.seo.api;

import com.mrsoft.arabicreference.seo.application.SeoDiscoveryService;
import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import com.mrsoft.arabicreference.shared.web.api.ApiResponse;
import com.mrsoft.arabicreference.shared.web.api.ApiResponses;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminSeoController {

    private final SeoDiscoveryService discovery;
    private final TimeProvider time;

    public AdminSeoController(SeoDiscoveryService discovery, TimeProvider time) {
        this.discovery = discovery;
        this.time = time;
    }

    @GetMapping("/api/v1/admin/seo/status")
    public ApiResponse<Map<String, Object>> status() {
        return ApiResponses.ok(discovery.status(), time);
    }
}
