package com.mrsoft.arabicreference.shared.infrastructure.time;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;

class UtcTimeProviderTest {

    @Test
    void nowIsAUtcInstant() {
        Instant before = Instant.now().minusSeconds(2);
        Instant value = new UtcTimeProvider().now();
        Instant after = Instant.now().plusSeconds(2);

        assertThat(value).isBetween(before, after);
        assertThat(DateTimeFormatter.ISO_INSTANT.format(value)).endsWith("Z");
    }
}
