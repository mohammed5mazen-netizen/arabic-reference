package com.mrsoft.arabicreference.shared.kernel.id;

import java.util.UUID;

/**
 * Public identifiers are random UUIDs. Sequential database keys must not be exposed as public resource ids.
 */
public final class Ids {

    private Ids() {
    }

    public static UUID random() {
        return UUID.randomUUID();
    }
}
