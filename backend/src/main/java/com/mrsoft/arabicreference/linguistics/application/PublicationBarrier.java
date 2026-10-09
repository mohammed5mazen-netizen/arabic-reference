package com.mrsoft.arabicreference.linguistics.application;

import java.util.UUID;

public interface PublicationBarrier {

    void assertNoOpenBlocker(String contentType, UUID contentId);
}
