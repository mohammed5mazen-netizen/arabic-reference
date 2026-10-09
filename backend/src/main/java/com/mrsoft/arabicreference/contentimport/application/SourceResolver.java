package com.mrsoft.arabicreference.contentimport.application;

import com.mrsoft.arabicreference.linguistics.domain.editorial.PublicationStatus;
import com.mrsoft.arabicreference.source.domain.LicensePolicy;
import com.mrsoft.arabicreference.source.domain.LicenseType;
import com.mrsoft.arabicreference.source.infrastructure.persistence.ReferenceSourceEntity;
import com.mrsoft.arabicreference.source.infrastructure.persistence.ReferenceSourceRepository;
import org.springframework.stereotype.Component;

@Component
public class SourceResolver {
    private final ReferenceSourceRepository sources;

    public SourceResolver(ReferenceSourceRepository sources) {
        this.sources = sources;
    }

    public ReferenceSourceEntity resolveForImport(String sourceSlug) {
        ReferenceSourceEntity source = sources.findBySlug(sourceSlug)
                .orElseThrow(() -> new ContentImportException("Published source was not found: " + sourceSlug));
        if (source.getStatus() != PublicationStatus.PUBLISHED) {
            throw new ContentImportException("Source must be PUBLISHED before it can be imported.");
        }
        LicenseType license = source.getLicenseType();
        if (!LicensePolicy.allowsPublicAttribution(license)
                || (license == LicenseType.PUBLIC_DOMAIN && !source.isPublicDomain())
                || source.getAttributionText() == null
                || source.getAttributionText().isBlank()) {
            throw new ContentImportException("Source license or attribution does not permit import.");
        }
        return source;
    }
}
