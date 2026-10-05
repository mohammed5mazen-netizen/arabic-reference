package com.mrsoft.arabicreference.rhetoric.api;

import com.mrsoft.arabicreference.rhetoric.application.RhetoricAdminService;
import com.mrsoft.arabicreference.rhetoric.application.RhetoricViews.CitationRequest;
import com.mrsoft.arabicreference.rhetoric.application.RhetoricViews.ComponentDraft;
import com.mrsoft.arabicreference.rhetoric.application.RhetoricViews.DeviceAdmin;
import com.mrsoft.arabicreference.rhetoric.application.RhetoricViews.DeviceDraft;
import com.mrsoft.arabicreference.rhetoric.application.RhetoricViews.ExampleDraft;
import com.mrsoft.arabicreference.rhetoric.application.RhetoricViews.PageResult;
import com.mrsoft.arabicreference.rhetoric.application.RhetoricViews.ReasonRequest;
import com.mrsoft.arabicreference.rhetoric.application.RhetoricViews.RelationDraft;
import com.mrsoft.arabicreference.rhetoric.application.RhetoricViews.ReviewItem;
import com.mrsoft.arabicreference.rhetoric.application.RhetoricViews.TopicAdmin;
import com.mrsoft.arabicreference.rhetoric.application.RhetoricViews.TopicDraft;
import com.mrsoft.arabicreference.rhetoric.application.RhetoricViews.TopicSummary;
import com.mrsoft.arabicreference.rhetoric.application.RhetoricViews.VersionRequest;
import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import com.mrsoft.arabicreference.shared.web.api.ApiResponse;
import com.mrsoft.arabicreference.shared.web.api.ApiResponses;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/rhetoric")
public class AdminRhetoricController {
    private final RhetoricAdminService rhetoric;
    private final TimeProvider timeProvider;

    public AdminRhetoricController(RhetoricAdminService rhetoric, TimeProvider timeProvider) {
        this.rhetoric = rhetoric;
        this.timeProvider = timeProvider;
    }

    @GetMapping("/topics")
    public ApiResponse<PageResult<TopicSummary>> topics(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponses.ok(rhetoric.topics(page, size), timeProvider);
    }

    @PostMapping("/topics")
    public ApiResponse<TopicAdmin> createTopic(@RequestBody TopicDraft draft) { return ApiResponses.ok(rhetoric.createTopic(draft), timeProvider); }

    @PostMapping("/topics/{id}/citations")
    public ApiResponse<TopicAdmin> citeTopic(@PathVariable UUID id, @RequestBody CitationRequest request) { return ApiResponses.ok(rhetoric.citeTopic(id, request.version(), request.citationId()), timeProvider); }

    @PostMapping("/topics/{id}/submit")
    public ApiResponse<TopicAdmin> submitTopic(@PathVariable UUID id, @RequestBody VersionRequest request) { return ApiResponses.ok(rhetoric.submitTopic(id, request.version()), timeProvider); }

    @PostMapping("/topics/{id}/verify")
    public ApiResponse<TopicAdmin> verifyTopic(@PathVariable UUID id, @RequestBody VersionRequest request) { return ApiResponses.ok(rhetoric.verifyTopic(id, request.version()), timeProvider); }

    @PostMapping("/topics/{id}/publish")
    public ApiResponse<TopicAdmin> publishTopic(@PathVariable UUID id, @RequestBody VersionRequest request) { return ApiResponses.ok(rhetoric.publishTopic(id, request.version()), timeProvider); }

    @PostMapping("/topics/{id}/archive")
    public ApiResponse<TopicAdmin> archiveTopic(@PathVariable UUID id, @RequestBody VersionRequest request) { return ApiResponses.ok(rhetoric.archiveTopic(id, request.version()), timeProvider); }

    @PostMapping("/devices")
    public ApiResponse<DeviceAdmin> createDevice(@RequestBody DeviceDraft draft) { return ApiResponses.ok(rhetoric.createDevice(draft), timeProvider); }

    @GetMapping("/devices/{id}")
    public ApiResponse<DeviceAdmin> device(@PathVariable UUID id) { return ApiResponses.ok(rhetoric.device(id), timeProvider); }

    @PostMapping("/devices/{id}/components")
    public ApiResponse<DeviceAdmin> addComponent(@PathVariable UUID id, @RequestBody ComponentDraft draft) { return ApiResponses.ok(rhetoric.addComponent(id, draft), timeProvider); }

    @PostMapping("/devices/{id}/examples")
    public ApiResponse<DeviceAdmin> addExample(@PathVariable UUID id, @RequestBody ExampleDraft draft) { return ApiResponses.ok(rhetoric.addExample(id, draft), timeProvider); }

    @PostMapping("/devices/{id}/relations")
    public ApiResponse<DeviceAdmin> relate(@PathVariable UUID id, @RequestBody RelationDraft draft) { return ApiResponses.ok(rhetoric.relate(id, draft), timeProvider); }

    @PostMapping("/devices/{id}/citations")
    public ApiResponse<DeviceAdmin> citeDevice(@PathVariable UUID id, @RequestBody CitationRequest request) { return ApiResponses.ok(rhetoric.citeDevice(id, request.version(), request.citationId()), timeProvider); }

    @PostMapping("/devices/{id}/submit")
    public ApiResponse<DeviceAdmin> submitDevice(@PathVariable UUID id, @RequestBody VersionRequest request) { return ApiResponses.ok(rhetoric.submitDevice(id, request.version()), timeProvider); }

    @PostMapping("/devices/{id}/verify")
    public ApiResponse<DeviceAdmin> verifyDevice(@PathVariable UUID id, @RequestBody VersionRequest request) { return ApiResponses.ok(rhetoric.verifyDevice(id, request.version()), timeProvider); }

    @PostMapping("/devices/{id}/request-changes")
    public ApiResponse<DeviceAdmin> requestChanges(@PathVariable UUID id, @RequestBody ReasonRequest request) { return ApiResponses.ok(rhetoric.requestDeviceChanges(id, request.version(), request.reason()), timeProvider); }

    @PostMapping("/devices/{id}/publish")
    public ApiResponse<DeviceAdmin> publishDevice(@PathVariable UUID id, @RequestBody VersionRequest request) { return ApiResponses.ok(rhetoric.publishDevice(id, request.version()), timeProvider); }

    @PostMapping("/devices/{id}/archive")
    public ApiResponse<DeviceAdmin> archiveDevice(@PathVariable UUID id, @RequestBody VersionRequest request) { return ApiResponses.ok(rhetoric.archiveDevice(id, request.version()), timeProvider); }

    @GetMapping("/review")
    public ApiResponse<List<ReviewItem>> review() { return ApiResponses.ok(rhetoric.reviewQueue(), timeProvider); }
}
