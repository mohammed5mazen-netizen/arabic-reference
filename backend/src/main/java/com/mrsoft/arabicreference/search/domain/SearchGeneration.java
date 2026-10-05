package com.mrsoft.arabicreference.search.domain;

/** Process-local generation bumped on every index write so caches can drop stale pages. */
public interface SearchGeneration {

    long current();
}
