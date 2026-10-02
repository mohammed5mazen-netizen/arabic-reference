package com.mrsoft.arabicreference.search.domain;

/**
 * Port for a future Arabic linguistic search adapter.
 * S0 registers no implementation. PostgreSQL can back the first adapter, and a dedicated
 * search engine can replace that adapter without rewriting the linguistic domain.
 * This port must stay read-only. It is not a source of linguistic truth.
 */
public interface LinguisticSearchPort {
}
