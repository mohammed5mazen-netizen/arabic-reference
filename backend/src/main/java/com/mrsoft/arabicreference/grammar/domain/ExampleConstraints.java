package com.mrsoft.arabicreference.grammar.domain;

import com.mrsoft.arabicreference.shared.kernel.exception.FieldErrorDetail;
import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import java.util.List;
import java.util.UUID;

public final class ExampleConstraints {

    public static final String MISSING_ANALYSIS = "لا يتوفر تحليل نحوي موثق لهذه الجملة.";
    public static final String EDITORIAL_NOTE = "مثال تحريري، وليس شاهدًا من مصدر.";

    private ExampleConstraints() {
    }

    public static void assertValid(ExampleType type, UUID citationId, Integer surah, Integer ayah, String poet, String workTitle) {
        if (type == null) {
            throw invalid("exampleType", "Choose an example type.");
        }
        if (type.citationRequired() && citationId == null) {
            throw invalid("citationId", "A quoted example needs a citation.");
        }
        if (type == ExampleType.QURANIC && (surah == null || ayah == null || surah < 1 || surah > 114 || ayah < 1)) {
            throw invalid("surah", "A Quranic example needs a surah from 1 to 114 and an ayah number. The Quran text is not imported.");
        }
        if (type == ExampleType.POETRY && (poet == null || poet.isBlank() || workTitle == null || workTitle.isBlank())) {
            throw invalid("poet", "A poetry example needs the poet and the work. Protected collections are not copied.");
        }
    }

    private static ValidationException invalid(String field, String message) {
        return new ValidationException(message, List.of(new FieldErrorDetail(field, message)));
    }
}
