package com.mrsoft.arabicreference.contentimport.domain;

public class ContentPackFormatException extends RuntimeException {
    public ContentPackFormatException(String message) {
        super(message);
    }

    public ContentPackFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
