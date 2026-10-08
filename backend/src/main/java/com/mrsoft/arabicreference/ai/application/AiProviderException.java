package com.mrsoft.arabicreference.ai.application;

public class AiProviderException extends RuntimeException {

    public enum Kind {
        TIMEOUT(true),
        UNAVAILABLE(true),
        RATE_LIMITED(false),
        MALFORMED(false);

        private final boolean retryable;

        Kind(boolean retryable) {
            this.retryable = retryable;
        }

        public boolean retryable() {
            return retryable;
        }
    }

    private final Kind kind;

    public AiProviderException(Kind kind) {
        super(kind.name());
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }
}
