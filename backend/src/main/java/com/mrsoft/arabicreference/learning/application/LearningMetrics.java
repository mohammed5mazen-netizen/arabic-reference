package com.mrsoft.arabicreference.learning.application;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class LearningMetrics {

    private final MeterRegistry meters;

    public LearningMetrics(MeterRegistry meters) {
        this.meters = meters;
    }

    public void lessonView() {
        meters.counter("learning.lesson.views").increment();
    }

    public void quizStarted() {
        meters.counter("learning.quiz.started").increment();
    }

    public void quizSubmitted(boolean passed) {
        meters.counter("learning.quiz.submitted").increment();
        if (passed) {
            meters.counter("learning.quiz.passed").increment();
        }
    }
}
