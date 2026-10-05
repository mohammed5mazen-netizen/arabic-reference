package com.mrsoft.arabicreference.content.domain;

import java.util.Optional;
import java.util.UUID;

/**
 * A published target an article may cite by id.
 * The relation table stores the type and id without a database foreign key.
 * Each module implements the targets it owns.
 */
public interface KnowledgeTargetSource {

    boolean supports(KnowledgeTargetType type);

    Optional<KnowledgeTarget> published(UUID id);

    record KnowledgeTarget(KnowledgeTargetType type, UUID id, String title, String url) {
    }
}
