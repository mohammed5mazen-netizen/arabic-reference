package com.mrsoft.arabicreference.linguistics.domain.editorial;

import com.mrsoft.arabicreference.shared.kernel.exception.ForbiddenOperationException;
import java.util.UUID;

public final class EditorialGuards {

    private EditorialGuards() {
    }

    public static void requireDifferentPerson(UUID existingPerson, UUID actor, String message) {
        if (existingPerson != null && existingPerson.equals(actor)) {
            throw new ForbiddenOperationException(message);
        }
    }
}
