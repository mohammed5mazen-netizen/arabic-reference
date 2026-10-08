package com.mrsoft.arabicreference.ai.domain;

public record RetrievedEvidence(
        String entityType,
        String entityId,
        String title,
        String excerpt,
        String canonicalUrl,
        String sourceLabel,
        String matchReason,
        String provenance,
        String publishedVersion,
        int score) {

    public boolean usable() {
        return score >= EvidenceScores.USABLE;
    }

    public boolean ruleDerived() {
        return "RULE_DERIVED".equals(provenance);
    }
}
