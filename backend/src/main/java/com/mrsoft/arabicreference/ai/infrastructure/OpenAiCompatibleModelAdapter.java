package com.mrsoft.arabicreference.ai.infrastructure;

import com.mrsoft.arabicreference.ai.application.AiModelPort;
import com.mrsoft.arabicreference.ai.application.AiProviderException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "openai-compatible")
public class OpenAiCompatibleModelAdapter implements AiModelPort {

    private static final Logger log = LoggerFactory.getLogger(OpenAiCompatibleModelAdapter.class);
    private static final int OPEN_AFTER = 3;

    private final HttpClient http;
    private final JsonMapper json;
    private final AiProperties properties;
    private final AtomicInteger consecutiveFailures = new AtomicInteger();
    private volatile Instant openUntil = Instant.EPOCH;

    public OpenAiCompatibleModelAdapter(JsonMapper json, AiProperties properties) {
        this.json = json;
        this.properties = properties;
        this.http = HttpClient.newBuilder().connectTimeout(properties.getTimeout()).build();
    }

    @Override
    public ModelCompletion complete(ModelRequest request) {
        if (Instant.now().isBefore(openUntil)) {
            throw new AiProviderException(AiProviderException.Kind.UNAVAILABLE);
        }
        AiProviderException last = null;
        for (int attempt = 0; attempt < 2; attempt++) {
            try {
                ModelCompletion completion = call(request);
                consecutiveFailures.set(0);
                return completion;
            } catch (AiProviderException exception) {
                last = exception;
                if (attempt == 1 || !exception.kind().retryable()) {
                    noteFailure();
                    throw exception;
                }
            }
        }
        noteFailure();
        throw last == null ? new AiProviderException(AiProviderException.Kind.UNAVAILABLE) : last;
    }

    private ModelCompletion call(ModelRequest request) {
        ObjectNode body = json.createObjectNode();
        body.put("model", properties.model());
        body.put("temperature", 0);
        body.put("max_tokens", request.maxOutputTokens());
        body.putObject("response_format").put("type", "json_object");
        var messages = body.putArray("messages");
        messages.addObject().put("role", "system").put("content", request.systemPrompt());
        StringBuilder user = new StringBuilder();
        user.append(request.evidenceBlock()).append("\n<question>\n").append(request.question()).append("\n</question>");
        for (PriorTurn turn : request.priorTurns()) {
            user.append("\n<prior-untrusted>\n").append(turn.question()).append("\n").append(turn.answer()).append("\n</prior-untrusted>");
        }
        messages.addObject().put("role", "user").put("content", user.toString());
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(properties.getBaseUrl()))
                .timeout(properties.getTimeout())
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + properties.getApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)))
                .build();
        try {
            HttpResponse<String> response = http.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 429) {
                throw new AiProviderException(AiProviderException.Kind.RATE_LIMITED);
            }
            if (response.statusCode() >= 500) {
                throw new AiProviderException(AiProviderException.Kind.UNAVAILABLE);
            }
            if (response.statusCode() >= 400) {
                throw new AiProviderException(AiProviderException.Kind.MALFORMED);
            }
            return parse(response.body());
        } catch (AiProviderException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new AiProviderException(AiProviderException.Kind.TIMEOUT);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AiProviderException(AiProviderException.Kind.TIMEOUT);
        } catch (RuntimeException exception) {
            log.warn("Assistant provider response could not be read");
            throw new AiProviderException(AiProviderException.Kind.MALFORMED);
        }
    }

    private ModelCompletion parse(String body) {
        JsonNode root = json.readTree(body);
        JsonNode content = root.path("choices").path(0).path("message").path("content");
        if (content.isMissingNode() || content.asString().isBlank()) {
            throw new AiProviderException(AiProviderException.Kind.MALFORMED);
        }
        String payload = content.asString().trim();
        if (payload.startsWith("```")) {
            int first = payload.indexOf('\n');
            int last = payload.lastIndexOf("```");
            if (first > 0 && last > first) {
                payload = payload.substring(first + 1, last).trim();
            }
        }
        JsonNode answerNode = json.readTree(payload);
        if (!answerNode.hasNonNull("answer") || !answerNode.path("citedEvidenceIds").isArray()) {
            throw new AiProviderException(AiProviderException.Kind.MALFORMED);
        }
        List<String> ids = new ArrayList<>();
        answerNode.path("citedEvidenceIds").forEach(node -> ids.add(node.asString()));
        List<String> limitations = new ArrayList<>();
        if (answerNode.path("limitations").isArray()) {
            answerNode.path("limitations").forEach(node -> limitations.add(node.asString()));
        }
        Integer input = tokenCount(root, "prompt_tokens");
        Integer output = tokenCount(root, "completion_tokens");
        return new ModelCompletion(answerNode.path("answer").asString(), ids, limitations, input, output);
    }

    private static Integer tokenCount(JsonNode root, String field) {
        JsonNode node = root.path("usage").path(field);
        return node.isNumber() ? node.asInt() : null;
    }

    private void noteFailure() {
        if (consecutiveFailures.incrementAndGet() >= OPEN_AFTER) {
            openUntil = Instant.now().plus(Duration.ofSeconds(30));
            consecutiveFailures.set(0);
            log.warn("Assistant provider circuit opened");
        }
    }
}
