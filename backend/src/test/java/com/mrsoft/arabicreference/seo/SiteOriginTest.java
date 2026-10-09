package com.mrsoft.arabicreference.seo;

import static org.assertj.core.api.Assertions.assertThat;

import com.mrsoft.arabicreference.seo.domain.PublicLocation;
import com.mrsoft.arabicreference.seo.domain.SiteOrigin;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class SiteOriginTest {

    @Test
    void originIsAbsoluteAndIndexingRejectsLocalHosts() {
        assertThat(SiteOrigin.parse("https://reference.example/").orElseThrow().value()).isEqualTo("https://reference.example");
        assertThat(SiteOrigin.parse("https://reference.example/").orElseThrow().indexable()).isTrue();
        assertThat(SiteOrigin.parse("http://localhost:3000").orElseThrow().indexable()).isFalse();
        assertThat(SiteOrigin.parse("https://reference.example/ar")).isEmpty();
        assertThat(SiteOrigin.parse("https://reference.example/?q=1")).isEmpty();
        assertThat(SiteOrigin.parse("not a url")).isEmpty();
    }

    @Test
    void discoveryDropsPrivateAndQueryPaths() {
        assertThat(new PublicLocation("DICTIONARY_ENTRY", "/word/كتاب", Instant.parse("2026-10-09T00:00:00Z")).publishable()).isTrue();
        assertThat(new PublicLocation("DICTIONARY_ENTRY", "/admin/seo", null).publishable()).isFalse();
        assertThat(new PublicLocation("DICTIONARY_ENTRY", "/tools/root?q=كتب", null).publishable()).isFalse();
        assertThat(new PublicLocation("DICTIONARY_ENTRY", "/search", null).publishable()).isFalse();
        assertThat(new PublicLocation("LESSON", "/learn/path/lesson", null).publishable()).isTrue();
    }
}
