package com.mrsoft.arabicreference.ai.application;

public interface AiRateLimitPort {

    void acquire(String client);
}
