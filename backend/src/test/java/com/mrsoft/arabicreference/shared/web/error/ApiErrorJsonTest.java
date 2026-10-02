package com.mrsoft.arabicreference.shared.web.error;

import static org.assertj.core.api.Assertions.assertThat;

import com.mrsoft.arabicreference.shared.kernel.exception.FieldErrorDetail;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class ApiErrorJsonTest {

    @Test
    void escapesControlCharacters() {
        ApiErrorResponse error = new ApiErrorResponse(
                "VALIDATION_FAILED",
                "line\n\"quoted\"",
                List.of(),
                List.of(new FieldErrorDetail("q", "required")),
                "trace",
                Instant.parse("2026-10-02T00:00:00Z"));

        String json = ApiErrorJson.write(error);

        assertThat(json).contains("line\\n\\\"quoted\\\"");
        assertThat(json).contains("\"field\":\"q\"");
        assertThat(json).contains("\"timestamp\":\"2026-10-02T00:00:00Z\"");
        assertThat(json).doesNotContain("\n");
    }
}
