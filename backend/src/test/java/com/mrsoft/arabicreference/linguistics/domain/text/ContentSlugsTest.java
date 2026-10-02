package com.mrsoft.arabicreference.linguistics.domain.text;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class ContentSlugsTest {

    @Test
    void slugKeepsTheLabelAndAStableIdPrefix() {
        UUID id = UUID.fromString("12345678-aaaa-4000-8000-000000000000");
        assertThat(ContentSlugs.of("كتاب كبير", id)).isEqualTo("كتاب-كبير-12345678");
        assertThat(ContentSlugs.of("  ", id)).isEqualTo("item-12345678");
    }
}
