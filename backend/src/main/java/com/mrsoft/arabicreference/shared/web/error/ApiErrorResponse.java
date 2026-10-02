package com.mrsoft.arabicreference.shared.web.error;

import com.mrsoft.arabicreference.shared.kernel.exception.FieldErrorDetail;
import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
        String code,
        String message,
        List<String> details,
        List<FieldErrorDetail> fieldErrors,
        String traceId,
        Instant timestamp) {

    public ApiErrorResponse {
        details = details == null ? List.of() : List.copyOf(details);
        fieldErrors = fieldErrors == null ? List.of() : List.copyOf(fieldErrors);
    }
}
