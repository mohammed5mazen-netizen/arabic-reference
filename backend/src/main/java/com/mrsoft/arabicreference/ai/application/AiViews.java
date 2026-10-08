package com.mrsoft.arabicreference.ai.application;

import com.mrsoft.arabicreference.ai.domain.GroundingStatus;
import java.util.List;

public final class AiViews {

    private AiViews() {
    }

    public record PriorTurn(String question, String answer) {
    }

    public record CitationCard(String evidenceId, String title, String href, String sourceLabel) {
    }

    public record EvidenceCard(
            String evidenceId,
            String title,
            String typeLabel,
            String excerpt,
            String sourceLabel,
            String href,
            String provenanceLabel) {
    }

    public record NearbyLink(String title, String href, String note) {
    }

    public record ToolHint(String label, String href) {
    }

    public record AssistantAnswer(
            String requestId,
            String answer,
            String grounding,
            String groundingLabel,
            boolean grounded,
            boolean uncertain,
            List<CitationCard> citations,
            List<EvidenceCard> evidence,
            List<String> limitations,
            List<NearbyLink> nearby,
            List<ToolHint> tools) {

        public AssistantAnswer withRequestId(String nextId) {
            return new AssistantAnswer(nextId, answer, grounding, groundingLabel, grounded, uncertain, citations, evidence, limitations, nearby, tools);
        }
    }

    public record AssistantStatus(boolean available, String message, List<String> suggestions) {
    }

    public record AdminAiStatus(
            boolean enabled,
            String provider,
            String model,
            boolean keyConfigured,
            String promptVersion,
            long requests,
            long grounded,
            long partial,
            long insufficient,
            long providerErrors,
            double averageLatencyMs) {
    }

    public static AssistantAnswer insufficient(String requestId, String answer, List<String> limitations, List<NearbyLink> nearby, List<ToolHint> tools) {
        return new AssistantAnswer(
                requestId,
                answer,
                GroundingStatus.INSUFFICIENT_EVIDENCE.name(),
                GroundingStatus.INSUFFICIENT_EVIDENCE.label(),
                false,
                false,
                List.of(),
                List.of(),
                limitations,
                nearby,
                tools);
    }
}
