package com.mrsoft.arabicreference.grammar.domain;

import com.mrsoft.arabicreference.shared.kernel.exception.ConflictException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

public final class TopicGraph {

    private TopicGraph() {
    }

    public static void assertNoParentCycle(UUID topicId, UUID proposedParent, Function<UUID, Optional<UUID>> parentOf) {
        if (proposedParent == null) {
            return;
        }
        if (topicId.equals(proposedParent)) {
            throw new ConflictException("A topic cannot be its own parent.");
        }
        UUID cursor = proposedParent;
        int guard = 0;
        while (cursor != null) {
            if (cursor.equals(topicId)) {
                throw new ConflictException("That parent would create a cycle.");
            }
            cursor = parentOf.apply(cursor).orElse(null);
            if (++guard > 64) {
                throw new ConflictException("The topic hierarchy is too deep.");
            }
        }
    }

    public static void assertNoPrerequisiteCycle(UUID topicId, UUID requiredTopicId, Function<UUID, List<UUID>> requirementsOf) {
        if (topicId.equals(requiredTopicId)) {
            throw new ConflictException("A topic cannot be a prerequisite of itself.");
        }
        Set<UUID> seen = new HashSet<>();
        List<UUID> pending = List.of(requiredTopicId);
        int guard = 0;
        while (!pending.isEmpty()) {
            if (++guard > 256) {
                throw new ConflictException("The prerequisite graph is too deep.");
            }
            UUID current = pending.get(0);
            pending = pending.subList(1, pending.size());
            if (!seen.add(current)) {
                continue;
            }
            if (current.equals(topicId)) {
                throw new ConflictException("That prerequisite would create a cycle.");
            }
            List<UUID> next = requirementsOf.apply(current);
            if (next != null && !next.isEmpty()) {
                java.util.ArrayList<UUID> merged = new java.util.ArrayList<>(next);
                merged.addAll(pending);
                pending = merged;
            }
        }
    }
}
