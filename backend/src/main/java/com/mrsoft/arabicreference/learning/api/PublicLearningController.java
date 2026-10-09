package com.mrsoft.arabicreference.learning.api;

import com.mrsoft.arabicreference.learning.application.LearningService;
import com.mrsoft.arabicreference.learning.application.LearningService.AnswerInput;
import com.mrsoft.arabicreference.shared.web.ClientAddresses;
import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import com.mrsoft.arabicreference.shared.web.api.ApiResponse;
import com.mrsoft.arabicreference.shared.web.api.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PublicLearningController {

    private final LearningService learning;
    private final TimeProvider time;

    public PublicLearningController(LearningService learning, TimeProvider time) {
        this.learning = learning;
        this.time = time;
    }

    @GetMapping("/api/v1/public/learning/paths")
    public ApiResponse<List<Map<String, Object>>> paths() {
        return ApiResponses.ok(learning.publicPaths(), time);
    }

    @GetMapping("/api/v1/public/learning/paths/{slug}")
    public ApiResponse<Map<String, Object>> path(@PathVariable String slug) {
        return ApiResponses.ok(learning.publicPath(slug), time);
    }

    @GetMapping("/api/v1/public/learning/lessons/{slug}")
    public ApiResponse<Map<String, Object>> lesson(@PathVariable String slug) {
        return ApiResponses.ok(learning.publicLesson(slug), time);
    }

    @GetMapping("/api/v1/public/learning/references")
    public ApiResponse<List<Map<String, Object>>> references(@RequestParam String kind, @RequestParam String slug) {
        return ApiResponses.ok(learning.publishedReferences(kind, slug), time);
    }

    @PostMapping("/api/v1/public/learning/quizzes/{id}/attempts")
    public ApiResponse<Map<String, Object>> start(@PathVariable UUID id, HttpServletRequest request) {
        return ApiResponses.ok(learning.startAttempt(id, ClientAddresses.read(request)), time);
    }

    @PostMapping("/api/v1/public/learning/attempts/{token}/submit")
    public ApiResponse<Map<String, Object>> submit(@PathVariable String token, @RequestBody SubmitRequest body, HttpServletRequest request) {
        List<AnswerInput> answers = body.answers() == null ? List.of() : body.answers().stream().map(answer -> new AnswerInput(answer.questionId(), answer.optionIds() == null ? List.of() : answer.optionIds())).toList();
        return ApiResponses.ok(learning.submitAttempt(token, body.idempotencyKey(), answers, ClientAddresses.read(request)), time);
    }

    public record SubmitRequest(String idempotencyKey, List<AnswerRequest> answers) {
    }

    public record AnswerRequest(UUID questionId, List<UUID> optionIds) {
    }
}
