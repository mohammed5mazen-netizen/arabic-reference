package com.mrsoft.arabicreference.shared.web.api;

import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import com.mrsoft.arabicreference.shared.kernel.trace.TraceIds;

public final class ApiResponses {

    private ApiResponses() {
    }

    public static <T> ApiResponse<T> ok(T data, TimeProvider timeProvider) {
        return new ApiResponse<>(data, TraceIds.current(), timeProvider.now());
    }
}
