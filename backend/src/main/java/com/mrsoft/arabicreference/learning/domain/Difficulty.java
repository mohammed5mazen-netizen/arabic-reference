package com.mrsoft.arabicreference.learning.domain;

public enum Difficulty {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED;

    public String label() {
        return switch (this) {
            case BEGINNER -> "مبتدئ";
            case INTERMEDIATE -> "متوسط";
            case ADVANCED -> "متقدم";
        };
    }

    public static Difficulty parse(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("difficulty");
        }
        return Difficulty.valueOf(raw.trim().toUpperCase());
    }
}
