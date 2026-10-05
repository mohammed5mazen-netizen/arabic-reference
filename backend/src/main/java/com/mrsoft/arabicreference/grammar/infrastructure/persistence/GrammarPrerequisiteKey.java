package com.mrsoft.arabicreference.grammar.infrastructure.persistence;

import java.io.Serializable;
import java.util.UUID;

public class GrammarPrerequisiteKey implements Serializable {

    private UUID topicId;
    private UUID requiredTopicId;

    public GrammarPrerequisiteKey() {
    }

    public GrammarPrerequisiteKey(UUID topicId, UUID requiredTopicId) {
        this.topicId = topicId;
        this.requiredTopicId = requiredTopicId;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof GrammarPrerequisiteKey key && topicId.equals(key.topicId) && requiredTopicId.equals(key.requiredTopicId);
    }

    @Override
    public int hashCode() {
        return topicId.hashCode() * 31 + requiredTopicId.hashCode();
    }
}
