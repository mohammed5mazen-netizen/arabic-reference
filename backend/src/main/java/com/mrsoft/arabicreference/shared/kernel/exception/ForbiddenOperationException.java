package com.mrsoft.arabicreference.shared.kernel.exception;

public class ForbiddenOperationException extends RuntimeException {

    private final ErrorCode code;

    public ForbiddenOperationException(String message) {
        this(ErrorCode.FORBIDDEN_OPERATION, message);
    }

    public ForbiddenOperationException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public ErrorCode code() {
        return code;
    }
}
