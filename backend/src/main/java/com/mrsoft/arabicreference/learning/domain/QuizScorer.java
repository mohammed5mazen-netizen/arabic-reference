package com.mrsoft.arabicreference.learning.domain;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class QuizScorer {

    private QuizScorer() {
    }

    public record Key(UUID questionId, QuestionType type, Set<UUID> correctOptionIds) {
    }

    public record Outcome(int score, boolean passed, int correctCount, int questionCount) {
    }

    public static Outcome score(int passingScore, List<Key> questions, List<List<UUID>> selected) {
        if (questions.isEmpty() || selected.size() != questions.size()) {
            throw new IllegalArgumentException("answers");
        }
        int correctCount = 0;
        for (int index = 0; index < questions.size(); index++) {
            if (matches(questions.get(index), selected.get(index))) {
                correctCount++;
            }
        }
        int score = correctCount * 100 / questions.size();
        return new Outcome(score, score >= passingScore, correctCount, questions.size());
    }

    public static boolean matches(Key question, List<UUID> selected) {
        Set<UUID> unique = new HashSet<>(selected);
        if (unique.size() != selected.size()) {
            return false;
        }
        return switch (question.type()) {
            case MULTIPLE_CHOICE, TRUE_FALSE -> unique.size() == 1 && question.correctOptionIds().equals(unique);
            case MULTIPLE_SELECT -> question.correctOptionIds().equals(unique);
        };
    }
}
