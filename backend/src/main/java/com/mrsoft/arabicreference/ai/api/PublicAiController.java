package com.mrsoft.arabicreference.ai.api;

import com.mrsoft.arabicreference.ai.application.AiViews.AssistantAnswer;
import com.mrsoft.arabicreference.ai.application.AiViews.AssistantStatus;
import com.mrsoft.arabicreference.ai.application.AiViews.PriorTurn;
import com.mrsoft.arabicreference.ai.application.AssistantService;
import com.mrsoft.arabicreference.shared.web.ClientAddresses;
import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import com.mrsoft.arabicreference.shared.web.api.ApiResponse;
import com.mrsoft.arabicreference.shared.web.api.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PublicAiController {

    private final AssistantService assistant;
    private final TimeProvider time;

    public PublicAiController(AssistantService assistant, TimeProvider time) {
        this.assistant = assistant;
        this.time = time;
    }

    @GetMapping("/api/v1/public/ai/status")
    public ApiResponse<AssistantStatus> status() {
        return ApiResponses.ok(assistant.status(), time);
    }

    @PostMapping("/api/v1/public/ai/ask")
    public ApiResponse<AssistantAnswer> ask(@RequestBody AskRequest request, HttpServletRequest http) {
        return ApiResponses.ok(assistant.ask(request.question(), request.priorTurns(), ClientAddresses.read(http)), time);
    }

    public record AskRequest(String question, List<PriorTurn> priorTurns) {
    }
}
