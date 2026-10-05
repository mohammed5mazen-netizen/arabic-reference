package com.mrsoft.arabicreference.literature.infrastructure.persistence;

import com.mrsoft.arabicreference.linguistics.domain.time.CalendarSystem;
import com.mrsoft.arabicreference.linguistics.domain.time.DatePrecision;
import com.mrsoft.arabicreference.linguistics.infrastructure.persistence.EditorialEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "literary_figure")
public class LiteraryFigureEntity extends EditorialEntity {

    @Column(name = "canonical_name", nullable = false, length = 160)
    private String canonicalName;

    @Column(name = "normalized_name", nullable = false, length = 160)
    private String normalizedName;

    @Column(nullable = false, length = 180)
    private String slug;

    @Column(name = "biography_summary", length = 4000)
    private String biographySummary;

    @Enumerated(EnumType.STRING)
    @Column(name = "birth_precision", nullable = false, length = 16)
    private DatePrecision birthPrecision;

    @Enumerated(EnumType.STRING)
    @Column(name = "birth_calendar", nullable = false, length = 16)
    private CalendarSystem birthCalendar;

    @Column(name = "birth_year")
    private Integer birthYear;

    @Column(name = "birth_exact_date")
    private LocalDate birthExactDate;

    @Column(name = "birth_display", length = 80)
    private String birthDisplay;

    @Column(name = "birth_circa", nullable = false)
    private boolean birthCirca;

    @Enumerated(EnumType.STRING)
    @Column(name = "death_precision", nullable = false, length = 16)
    private DatePrecision deathPrecision;

    @Enumerated(EnumType.STRING)
    @Column(name = "death_calendar", nullable = false, length = 16)
    private CalendarSystem deathCalendar;

    @Column(name = "death_year")
    private Integer deathYear;

    @Column(name = "death_exact_date")
    private LocalDate deathExactDate;

    @Column(name = "death_display", length = 80)
    private String deathDisplay;

    @Column(name = "death_circa", nullable = false)
    private boolean deathCirca;

    public String getCanonicalName() { return canonicalName; }
    public void setCanonicalName(String canonicalName) { this.canonicalName = canonicalName; }
    public String getNormalizedName() { return normalizedName; }
    public void setNormalizedName(String normalizedName) { this.normalizedName = normalizedName; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getBiographySummary() { return biographySummary; }
    public void setBiographySummary(String biographySummary) { this.biographySummary = biographySummary; }
    public DatePrecision getBirthPrecision() { return birthPrecision; }
    public void setBirthPrecision(DatePrecision birthPrecision) { this.birthPrecision = birthPrecision; }
    public CalendarSystem getBirthCalendar() { return birthCalendar; }
    public void setBirthCalendar(CalendarSystem birthCalendar) { this.birthCalendar = birthCalendar; }
    public Integer getBirthYear() { return birthYear; }
    public void setBirthYear(Integer birthYear) { this.birthYear = birthYear; }
    public LocalDate getBirthExactDate() { return birthExactDate; }
    public void setBirthExactDate(LocalDate birthExactDate) { this.birthExactDate = birthExactDate; }
    public String getBirthDisplay() { return birthDisplay; }
    public void setBirthDisplay(String birthDisplay) { this.birthDisplay = birthDisplay; }
    public boolean isBirthCirca() { return birthCirca; }
    public void setBirthCirca(boolean birthCirca) { this.birthCirca = birthCirca; }
    public DatePrecision getDeathPrecision() { return deathPrecision; }
    public void setDeathPrecision(DatePrecision deathPrecision) { this.deathPrecision = deathPrecision; }
    public CalendarSystem getDeathCalendar() { return deathCalendar; }
    public void setDeathCalendar(CalendarSystem deathCalendar) { this.deathCalendar = deathCalendar; }
    public Integer getDeathYear() { return deathYear; }
    public void setDeathYear(Integer deathYear) { this.deathYear = deathYear; }
    public LocalDate getDeathExactDate() { return deathExactDate; }
    public void setDeathExactDate(LocalDate deathExactDate) { this.deathExactDate = deathExactDate; }
    public String getDeathDisplay() { return deathDisplay; }
    public void setDeathDisplay(String deathDisplay) { this.deathDisplay = deathDisplay; }
    public boolean isDeathCirca() { return deathCirca; }
    public void setDeathCirca(boolean deathCirca) { this.deathCirca = deathCirca; }
}
