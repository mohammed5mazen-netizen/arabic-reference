package com.mrsoft.arabicreference.linguistics.application;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Publish methods call {@link #assertNoOpenBlocker} without taking a dependency on the editorial module.
 * The barrier implementation is optional until that module is on the classpath.
 */
@Component
public class PublicationChecks {

    private static final AtomicReference<PublicationChecks> CURRENT = new AtomicReference<>();

    private final ObjectProvider<PublicationBarrier> barrier;

    public PublicationChecks(ObjectProvider<PublicationBarrier> barrier) {
        this.barrier = barrier;
        CURRENT.set(this);
    }

    public static void assertNoOpenBlocker(String contentType, UUID contentId) {
        PublicationChecks checks = CURRENT.get();
        if (checks == null || contentType == null || contentId == null) {
            return;
        }
        PublicationBarrier active = checks.barrier.getIfAvailable();
        if (active != null) {
            active.assertNoOpenBlocker(contentType, contentId);
        }
    }
}
