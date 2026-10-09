package com.mrsoft.arabicreference.contentimport.domain;

import java.util.List;

public record ImportReport(
        String sourceSlug,
        boolean dryRun,
        boolean committed,
        int recordsRead,
        int valid,
        int invalid,
        int duplicates,
        int newRecords,
        int updates,
        List<String> warnings,
        List<ImportIssue> issues) {

    public ImportReport {
        warnings = List.copyOf(warnings);
        issues = List.copyOf(issues);
    }
}
