package com.mrsoft.arabicreference.ai.application;

import com.mrsoft.arabicreference.ai.domain.AssistantIntent;
import com.mrsoft.arabicreference.ai.domain.RetrievedEvidence;
import java.util.List;

public interface KnowledgeRetrievalPort {

    RetrievalBatch retrieve(RetrievalRequest request, String client);

    List<String> suggestions();

    KnowledgeStamp stamp();

    record RetrievalRequest(
            AssistantIntent intent,
            String question,
            String normalized,
            List<String> terms,
            boolean asksForParsing) {
    }

    record RetrievalBatch(List<RetrievedEvidence> evidence, boolean semanticRelation) {
    }

    record KnowledgeStamp(String contentStamp, long ruleGeneration, long searchGeneration, int indexVersion) {
    }
}
