package com.mrsoft.arabicreference.morphology.domain;

import java.util.List;

public record AnalysisReport(
        String input,
        String normalizedInput,
        List<AnalysisCandidate> analyses,
        boolean truncated,
        int limit,
        String ruleSetVersion,
        AnalysisProvenance resultClass) {
}
