package com.mrsoft.arabicreference.ai.application;

import java.util.List;

public interface AiModelPort {

    ModelCompletion complete(ModelRequest request);

    record ModelRequest(String systemPrompt, String evidenceBlock, String question, List<PriorTurn> priorTurns, int maxOutputTokens) {
    }

    record PriorTurn(String question, String answer) {
    }

    record ModelCompletion(String answer, List<String> citedEvidenceIds, List<String> limitations, Integer inputTokens, Integer outputTokens) {
    }
}
