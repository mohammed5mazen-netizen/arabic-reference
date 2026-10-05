package com.mrsoft.arabicreference.search.domain;

public interface SearchThrottle {

    void acquire(String clientAddress);
}
