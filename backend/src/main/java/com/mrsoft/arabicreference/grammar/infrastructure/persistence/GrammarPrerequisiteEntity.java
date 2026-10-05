package com.mrsoft.arabicreference.grammar.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "grammar_topic_prerequisite")
@IdClass(GrammarPrerequisiteKey.class)
public class GrammarPrerequisiteEntity {

    @Id
    @Column(name = "topic_id")
    private UUID topicId;

    @Id
    @Column(name = "required_topic_id")
    private UUID requiredTopicId;

    public GrammarPrerequisiteEntity() {
    }

    public GrammarPrerequisiteEntity(UUID topicId, UUID requiredTopicId) {
        this.topicId = topicId;
        this.requiredTopicId = requiredTopicId;
    }

    public UUID getTopicId() { return topicId; }
    public UUID getRequiredTopicId() { return requiredTopicId; }
}
