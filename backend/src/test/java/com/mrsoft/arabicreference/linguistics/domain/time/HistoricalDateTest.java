package com.mrsoft.arabicreference.linguistics.domain.time;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class HistoricalDateTest {

    @Test
    void approximateAndUnknownDatesStayUncertain() {
        HistoricalDate approximate = new HistoricalDate(DatePrecision.APPROXIMATE, CalendarSystem.HIJRI, 150, null, null, true);
        assertThat(approximate.publicLabel()).isEqualTo("نحو سنة 150 هـ");
        assertThat(HistoricalDate.unknown().publicLabel()).isEqualTo("التاريخ غير معروف");
        HistoricalDate exact = new HistoricalDate(DatePrecision.EXACT, CalendarSystem.GREGORIAN, null, LocalDate.of(2020, 1, 2), null, false);
        assertThat(exact.publicLabel()).isEqualTo("2020-01-02");
        assertThat(new HistoricalDate(DatePrecision.YEAR, CalendarSystem.HIJRI, 1320, null, null, false).publicLabel()).isEqualTo("1320 هـ");
    }

    @Test
    void invalidYearsAreRejectedAndCalendarsAreNotConverted() {
        assertThatThrownBy(() -> new HistoricalDate(DatePrecision.YEAR, CalendarSystem.GREGORIAN, 0, null, null, false))
                .isInstanceOf(ValidationException.class);
        HistoricalDate hijri = new HistoricalDate(DatePrecision.YEAR, CalendarSystem.HIJRI, 1400, null, "نحو سنة 1400 هـ", true);
        assertThat(hijri.calendar()).isEqualTo(CalendarSystem.HIJRI);
        assertThat(hijri.publicLabel()).isEqualTo("نحو سنة 1400 هـ");
    }
}
