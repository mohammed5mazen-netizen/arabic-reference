package com.mrsoft.arabicreference.shared.kernel.exception;

public class UnauthorizedException extends RuntimeException {

    private final ErrorCode code;

    public UnauthorizedException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public ErrorCode code() {
        return code;
    }
}
