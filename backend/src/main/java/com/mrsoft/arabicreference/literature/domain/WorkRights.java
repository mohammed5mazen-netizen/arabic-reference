package com.mrsoft.arabicreference.literature.domain;

public enum WorkRights {
    PUBLIC_DOMAIN,
    LICENSED,
    RESTRICTED,
    UNKNOWN;

    public boolean allowsExcerpt() {
        return this == PUBLIC_DOMAIN || this == LICENSED;
    }
}
