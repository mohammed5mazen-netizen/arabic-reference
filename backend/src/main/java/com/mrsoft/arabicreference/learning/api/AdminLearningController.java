package com.mrsoft.arabicreference.learning.api;

import com.mrsoft.arabicreference.identity.domain.PermissionCatalog;
import com.mrsoft.arabicreference.learning.application.LearningService;
import com.mrsoft.arabicreference.learning.application.LearningService.OptionInput;
import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import com.mrsoft.arabicreference.shared.web.api.ApiResponse;
import com.mrsoft.arabicreference.shared.web.api.ApiResponses;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminLearningController {

    private final LearningService learning;
    private final TimeProvider time;

    public AdminLearningController(LearningService learning, TimeProvider time) {
        this.learning = learning;
        this.time = time;
    }

    @GetMapping("/api/v1/admin/learning/paths")
    @PreAuthorize("@authz.has('" + PermissionCatalog.LEARNING_PATH_VIEW + "')")
    public ApiResponse<List<Map<String, Object>>> paths() {
        return ApiResponses.ok(learning.adminPaths(), time);
    }

    @GetMapping("/api/v1/admin/learning/paths/{id}")
    @PreAuthorize("@authz.has('" + PermissionCatalog.LEARNING_LESSON_VIEW + "')")
    public ApiResponse<Map<String, Object>> path(@PathVariable UUID id) {
        return ApiResponses.ok(learning.adminPath(id), time);
    }

    @PostMapping("/api/v1/admin/learning/paths")
    @PreAuthorize("@authz.has('" + PermissionCatalog.LEARNING_PATH_MANAGE + "')")
    public ApiResponse<Map<String, Object>> create(@RequestBody PathRequest body) {
        return ApiResponses.ok(learning.createPath(body.title(), body.summary(), body.description(), body.difficulty(), body.estimatedMinutes(), body.displayOrder(), body.prerequisiteId()), time);
    }

    @PostMapping("/api/v1/admin/learning/paths/{id}/units")
    @PreAuthorize("@authz.has('" + PermissionCatalog.LEARNING_PATH_MANAGE + "')")
    public ApiResponse<Map<String, Object>> unit(@PathVariable UUID id, @RequestBody UnitRequest body) {
        return ApiResponses.ok(learning.addUnit(id, body.version(), body.title(), body.summary(), body.displayOrder()), time);
    }

    @PostMapping("/api/v1/admin/learning/units/{id}/lessons")
    @PreAuthorize("@authz.has('" + PermissionCatalog.LEARNING_LESSON_CREATE + "')")
    public ApiResponse<Map<String, Object>> lesson(@PathVariable UUID id, @RequestBody LessonRequest body) {
        return ApiResponses.ok(learning.addLesson(id, body.version(), body.title(), body.summary(), body.estimatedMinutes(), body.displayOrder()), time);
    }

    @PostMapping("/api/v1/admin/learning/lessons/{id}/objectives")
    @PreAuthorize("@authz.has('" + PermissionCatalog.LEARNING_LESSON_EDIT + "')")
    public ApiResponse<Map<String, Object>> objective(@PathVariable UUID id, @RequestBody TextRequest body) {
        return ApiResponses.ok(learning.addObjective(id, body.version(), body.text(), body.displayOrder()), time);
    }

    @PostMapping("/api/v1/admin/learning/lessons/{id}/sections")
    @PreAuthorize("@authz.has('" + PermissionCatalog.LEARNING_LESSON_EDIT + "')")
    public ApiResponse<Map<String, Object>> section(@PathVariable UUID id, @RequestBody SectionRequest body) {
        return ApiResponses.ok(learning.addSection(id, body.version(), body.type(), body.heading(), body.body(), body.exampleKind(), body.displayOrder()), time);
    }

    @PostMapping("/api/v1/admin/learning/lessons/{id}/references")
    @PreAuthorize("@authz.has('" + PermissionCatalog.LEARNING_LESSON_EDIT + "')")
    public ApiResponse<Map<String, Object>> reference(@PathVariable UUID id, @RequestBody ReferenceRequest body) {
        return ApiResponses.ok(learning.addReference(id, body.version(), body.kind(), body.slug(), body.note()), time);
    }

    @PostMapping("/api/v1/admin/learning/lessons/{id}/activities")
    @PreAuthorize("@authz.has('" + PermissionCatalog.LEARNING_LESSON_EDIT + "')")
    public ApiResponse<Map<String, Object>> activity(@PathVariable UUID id, @RequestBody ActivityRequest body) {
        return ApiResponses.ok(learning.addActivity(id, body.version(), body.type(), body.title(), body.instructions(), body.displayOrder()), time);
    }

    @PostMapping("/api/v1/admin/learning/lessons/{id}/quiz")
    @PreAuthorize("@authz.has('" + PermissionCatalog.LEARNING_QUIZ_MANAGE + "')")
    public ApiResponse<Map<String, Object>> quiz(@PathVariable UUID id, @RequestBody QuizRequest body) {
        return ApiResponses.ok(learning.addQuiz(id, null, body.version(), body.title(), body.passingScore(), body.maxAttempts()), time);
    }

    @PostMapping("/api/v1/admin/learning/quizzes/{id}/questions")
    @PreAuthorize("@authz.has('" + PermissionCatalog.LEARNING_QUESTION_MANAGE + "')")
    public ApiResponse<Map<String, Object>> question(@PathVariable UUID id, @RequestBody QuestionRequest body) {
        List<OptionInput> options = body.options() == null ? List.of() : body.options().stream().map(option -> new OptionInput(option.label(), option.correct())).toList();
        return ApiResponses.ok(learning.addQuestion(id, body.version(), body.prompt(), body.explanation(), body.type(), body.difficulty(), body.displayOrder(), body.knowledgeKind(), body.knowledgeSlug(), options), time);
    }

    @PostMapping("/api/v1/admin/learning/questions/{id}")
    @PreAuthorize("@authz.has('" + PermissionCatalog.LEARNING_QUESTION_MANAGE + "')")
    public ApiResponse<Map<String, Object>> revise(@PathVariable UUID id, @RequestBody ReviseRequest body) {
        return ApiResponses.ok(learning.reviseQuestion(id, body.version(), body.prompt()), time);
    }

    @PostMapping("/api/v1/admin/learning/paths/{id}/submit")
    @PreAuthorize("@authz.has('" + PermissionCatalog.LEARNING_LESSON_SUBMIT + "')")
    public ApiResponse<Map<String, Object>> submit(@PathVariable UUID id, @RequestBody VersionRequest body) {
        return ApiResponses.ok(learning.submit(id, body.version()), time);
    }

    @PostMapping("/api/v1/admin/learning/paths/{id}/changes")
    @PreAuthorize("@authz.has('" + PermissionCatalog.LEARNING_LESSON_REVIEW + "')")
    public ApiResponse<Map<String, Object>> changes(@PathVariable UUID id, @RequestBody VersionRequest body) {
        return ApiResponses.ok(learning.requestChanges(id, body.version()), time);
    }

    @PostMapping("/api/v1/admin/learning/paths/{id}/verify")
    @PreAuthorize("@authz.has('" + PermissionCatalog.LEARNING_LESSON_REVIEW + "')")
    public ApiResponse<Map<String, Object>> verify(@PathVariable UUID id, @RequestBody VersionRequest body) {
        return ApiResponses.ok(learning.verify(id, body.version()), time);
    }

    @PostMapping("/api/v1/admin/learning/paths/{id}/publish")
    @PreAuthorize("@authz.has('" + PermissionCatalog.LEARNING_LESSON_PUBLISH + "')")
    public ApiResponse<Map<String, Object>> publish(@PathVariable UUID id, @RequestBody VersionRequest body) {
        return ApiResponses.ok(learning.publish(id, body.version()), time);
    }

    @PostMapping("/api/v1/admin/learning/paths/{id}/archive")
    @PreAuthorize("@authz.has('" + PermissionCatalog.LEARNING_LESSON_ARCHIVE + "')")
    public ApiResponse<Map<String, Object>> archive(@PathVariable UUID id, @RequestBody VersionRequest body) {
        return ApiResponses.ok(learning.archive(id, body.version()), time);
    }

    public record PathRequest(String title, String summary, String description, String difficulty, Integer estimatedMinutes, int displayOrder, UUID prerequisiteId) {
    }

    public record UnitRequest(long version, String title, String summary, int displayOrder) {
    }

    public record LessonRequest(long version, String title, String summary, Integer estimatedMinutes, int displayOrder) {
    }

    public record TextRequest(long version, String text, int displayOrder) {
    }

    public record SectionRequest(long version, String type, String heading, String body, String exampleKind, int displayOrder) {
    }

    public record ReferenceRequest(long version, String kind, String slug, String note) {
    }

    public record ActivityRequest(long version, String type, String title, String instructions, int displayOrder) {
    }

    public record QuizRequest(long version, String title, int passingScore, Integer maxAttempts) {
    }

    public record QuestionRequest(long version, String prompt, String explanation, String type, String difficulty, int displayOrder, String knowledgeKind, String knowledgeSlug, List<OptionRequest> options) {
    }

    public record OptionRequest(String label, boolean correct) {
    }

    public record ReviseRequest(long version, String prompt) {
    }

    public record VersionRequest(long version) {
    }
}
