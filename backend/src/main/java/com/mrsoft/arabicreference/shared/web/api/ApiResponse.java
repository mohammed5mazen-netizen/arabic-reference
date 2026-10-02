package com.mrsoft.arabicreference.shared.web.api;

import java.time.Instant;

public record ApiResponse<T>(T data, String traceId, Instant timestamp) {
}
