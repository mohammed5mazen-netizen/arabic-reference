package com.mrsoft.arabicreference.linguistics.domain.time;

import com.mrsoft.arabicreference.shared.kernel.exception.FieldErrorDetail;
import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import java.time.LocalDate;
import java.util.List;

/**
 * A literary date that may be exact, a year, approximate, or unknown.
 * Hijri and Gregorian values are stored as entered. This type does not convert calendars.
 */
public record HistoricalDate(
        DatePrecision precision,
        CalendarSystem calendar,
        Integer year,
        LocalDate exactDate,
        String displayText,
        boolean circa) {

    public HistoricalDate {
        if (precision == null) {
            precision = DatePrecision.UNKNOWN;
        }
        if (calendar == null) {
            calendar = CalendarSystem.UNSPECIFIED;
        }
        if (displayText != null) {
            displayText = displayText.trim();
            if (displayText.isEmpty()) {
                displayText = null;
            } else if (displayText.codePointCount(0, displayText.length()) > 80) {
                throw invalid("displayText", "Keep the date label within 80 characters.");
            }
        }
        if (year != null && (year < 1 || year > 2500)) {
            throw invalid("year", "Enter a year between 1 and 2500, or leave it unknown.");
        }
        switch (precision) {
            case EXACT -> {
                if (exactDate == null && year == null && displayText == null) {
                    throw invalid("precision", "An exact date needs a day, a year, or a display label.");
                }
            }
            case YEAR, APPROXIMATE -> {
                if (year == null && displayText == null) {
                    throw invalid("year", "An uncertain date needs a year or a display label.");
                }
            }
            case UNKNOWN -> {
                // A missing date stays missing. No calendar conversion is implied.
            }
        }
    }

    public static HistoricalDate unknown() {
        return new HistoricalDate(DatePrecision.UNKNOWN, CalendarSystem.UNSPECIFIED, null, null, null, false);
    }

    public String publicLabel() {
        String calendarMark = calendar == CalendarSystem.HIJRI ? " هـ" : "";
        return switch (precision) {
            case UNKNOWN -> "التاريخ غير معروف";
            case APPROXIMATE -> displayText != null ? displayText : "نحو سنة " + year + calendarMark;
            case YEAR -> {
                if (circa) {
                    yield displayText != null ? displayText : "نحو سنة " + year + calendarMark;
                }
                yield displayText != null ? displayText : year + calendarMark;
            }
            case EXACT -> displayText != null ? displayText : (exactDate != null ? exactDate.toString() : String.valueOf(year));
        };
    }

    private static ValidationException invalid(String field, String message) {
        return new ValidationException(message, List.of(new FieldErrorDetail(field, message)));
    }
}
