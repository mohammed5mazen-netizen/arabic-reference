package com.mrsoft.arabicreference.editorial.domain;

import java.util.UUID;

public record QualityProbe(
        ContentType type,
        UUID id,
        String title,
        String summary,
        String status,
        boolean summaryRequired,
        boolean citationRequired,
        boolean hasSnapshot,
        boolean indexed,
        int senseCount,
        int sensesWithoutCitation,
        boolean brokenRoot,
        boolean unpublishedRoot,
        int unpublishedRelations,
        int orphanRelations,
        int brokenCitations,
        boolean duplicateSlug,
        int componentCount,
        int quotedExamplesWithoutCitation,
        int citationCount,
        String rights,
        int excerptCount,
        int sectionCount,
        int lessonsWithoutObjective,
        int invalidQuestions,
        int unpublishedKnowledgeRefs,
        int commonMistakesWithoutEvidence,
        boolean invalidCanonical,
        boolean unlinked) {
}
