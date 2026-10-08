package com.mrsoft.arabicreference.learning.application;

import com.mrsoft.arabicreference.learning.domain.ReferenceKind;
import java.util.Optional;

public interface PublishedReferencePort {

    Optional<ResolvedReference> resolve(ReferenceKind kind, String slug);

    record ResolvedReference(String title, String href) {
    }
}
