package com.mrsoft.arabicreference.shared.kernel.exception;

import java.util.List;

public class ValidationException extends RuntimeException {

    private final List<FieldErrorDetail> fieldErrors;

    public ValidationException(String message, List<FieldErrorDetail> fieldErrors) {
        super(message);
        this.fieldErrors = List.copyOf(fieldErrors);
    }

    public List<FieldErrorDetail> fieldErrors() {
        return fieldErrors;
    }
}
