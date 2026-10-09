package com.mrsoft.arabicreference.contentimport.application;

public class ContentImportException extends RuntimeException {
    public ContentImportException(String message) {
        super(message);
    }

    public ContentImportException(String message, Throwable cause) {
        super(message, cause);
    }
}
