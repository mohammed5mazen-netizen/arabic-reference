package com.mrsoft.arabicreference.search.domain;

import java.util.List;
import java.util.UUID;

/** Capped morphology lookup used only when the index has no stronger title, form, alias, or root hit. */
public interface MorphologySearchAssist {

    List<UUID> entryIds(String word);
}
