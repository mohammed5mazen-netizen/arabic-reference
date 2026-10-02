package com.mrsoft.arabicreference.shared.kernel.exception;

public class RateLimitedException extends RuntimeException {

    public RateLimitedException(String message) {
        super(message);
    }
}
