package com.mrsoft.arabicreference.learning.domain;

public final class PathProgress {

    private PathProgress() {
    }

    public static int percent(int completedLessons, int requiredLessons) {
        if (requiredLessons <= 0 || completedLessons <= 0) {
            return 0;
        }
        int bounded = Math.min(completedLessons, requiredLessons);
        return bounded * 100 / requiredLessons;
    }
}
