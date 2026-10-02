package com.mrsoft.arabicreference.source.application;

import com.mrsoft.arabicreference.source.domain.LicenseType;
import com.mrsoft.arabicreference.source.domain.SourceType;
import java.util.List;
import java.util.UUID;

public final class SourceViews {

    private SourceViews() {
    }

    public record PageResult<T>(List<T> items, int page, int size, long total) {
    }

    public record SourceDraft(
            SourceType sourceType,
            String title,
            String author,
            String publisher,
            String edition,
            Integer publicationYear,
            String isbn,
            String url,
            LicenseType licenseType,
            boolean publicDomain,
            String attributionText,
            String notes) {
    }

    public record SourceView(
            UUID id,
            String sourceType,
            String title,
            String author,
            String publisher,
            String edition,
            Integer publicationYear,
            String isbn,
            String url,
            String licenseType,
            boolean publicDomain,
            String attributionText,
            String notes,
            String status,
            String slug,
            long version,
            boolean publishableLicense) {
    }

    public record CitationDraft(
            Integer pageFrom,
            Integer pageTo,
            String volume,
            String chapter,
            String sectionLabel,
            String entryLabel,
            String sourceLocator,
            String quotedText,
            String notes) {
    }

    public record CitationView(
            UUID id,
            UUID sourceId,
            String title,
            String author,
            String edition,
            Integer publicationYear,
            Integer pageFrom,
            Integer pageTo,
            String volume,
            String chapter,
            String sectionLabel,
            String entryLabel,
            String sourceLocator,
            String quotedText,
            String attributionText,
            String licenseType,
            String sourceStatus,
            boolean publishableLicense) {
    }
}
