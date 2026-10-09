package com.mrsoft.arabicreference.seo.api;

import com.mrsoft.arabicreference.seo.application.SeoDiscoveryService;
import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import com.mrsoft.arabicreference.shared.web.api.ApiResponse;
import com.mrsoft.arabicreference.shared.web.api.ApiResponses;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PublicDiscoveryController {

    private final SeoDiscoveryService discovery;
    private final TimeProvider time;

    public PublicDiscoveryController(SeoDiscoveryService discovery, TimeProvider time) {
        this.discovery = discovery;
        this.time = time;
    }

    @GetMapping("/api/v1/public/discovery")
    public ApiResponse<Map<String, Object>> locations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        return ApiResponses.ok(discovery.locations(page, size), time);
    }
}
