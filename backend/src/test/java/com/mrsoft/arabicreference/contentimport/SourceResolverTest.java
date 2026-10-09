package com.mrsoft.arabicreference.contentimport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.mrsoft.arabicreference.contentimport.application.ContentImportException;
import com.mrsoft.arabicreference.contentimport.application.SourceResolver;
import com.mrsoft.arabicreference.linguistics.domain.editorial.PublicationStatus;
import com.mrsoft.arabicreference.source.domain.LicenseType;
import com.mrsoft.arabicreference.source.infrastructure.persistence.ReferenceSourceEntity;
import com.mrsoft.arabicreference.source.infrastructure.persistence.ReferenceSourceRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SourceResolverTest {
    @Mock private ReferenceSourceRepository sources;

    private SourceResolver resolver;
    private ReferenceSourceEntity source;

    @BeforeEach
    void setUp() {
        resolver = new SourceResolver(sources);
        source = new ReferenceSourceEntity();
        source.setSlug("approved-source");
        source.setStatus(PublicationStatus.PUBLISHED);
        source.setLicenseType(LicenseType.CC_BY_SA);
        source.setAttributionText("Required source credit.");
    }

    @Test
    void resolvesPublishedCreativeCommonsShareAlikeSourcesWithAttribution() {
        when(sources.findBySlug("approved-source")).thenReturn(Optional.of(source));

        assertThat(resolver.resolveForImport("approved-source")).isSameAs(source);
    }

    @Test
    void rejectsMissingUnpublishedAndDisallowedSources() {
        when(sources.findBySlug("missing")).thenReturn(Optional.empty());
        when(sources.findBySlug("approved-source")).thenReturn(Optional.of(source));

        assertThatThrownBy(() -> resolver.resolveForImport("missing"))
                .isInstanceOf(ContentImportException.class)
                .hasMessageContaining("not found");

        source.setStatus(PublicationStatus.DRAFT);
        assertThatThrownBy(() -> resolver.resolveForImport("approved-source"))
                .isInstanceOf(ContentImportException.class)
                .hasMessageContaining("PUBLISHED");

        source.setStatus(PublicationStatus.PUBLISHED);
        source.setLicenseType(LicenseType.UNKNOWN);
        assertThatThrownBy(() -> resolver.resolveForImport("approved-source"))
                .isInstanceOf(ContentImportException.class)
                .hasMessageContaining("license");

        source.setLicenseType(LicenseType.CC_BY_SA);
        source.setAttributionText(" ");
        assertThatThrownBy(() -> resolver.resolveForImport("approved-source"))
                .isInstanceOf(ContentImportException.class)
                .hasMessageContaining("license");
    }
}
