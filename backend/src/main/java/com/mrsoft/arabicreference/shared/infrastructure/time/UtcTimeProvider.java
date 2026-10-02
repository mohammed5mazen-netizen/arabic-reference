package com.mrsoft.arabicreference.shared.infrastructure.time;

import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public final class UtcTimeProvider implements TimeProvider {

    @Override
    public Instant now() {
        return Instant.now();
    }
}
