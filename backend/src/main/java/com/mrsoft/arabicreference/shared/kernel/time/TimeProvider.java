package com.mrsoft.arabicreference.shared.kernel.time;

import java.time.Instant;

/**
 * Clock port. Domain and API code must use this instead of the machine time zone.
 * Implementations return UTC instants.
 */
public interface TimeProvider {

    Instant now();
}
