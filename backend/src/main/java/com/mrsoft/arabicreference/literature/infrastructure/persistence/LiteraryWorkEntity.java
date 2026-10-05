package com.mrsoft.arabicreference.literature.infrastructure.persistence;

import com.mrsoft.arabicreference.literature.domain.WorkRights;
import com.mrsoft.arabicreference.linguistics.infrastructure.persistence.EditorialEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "literary_work")
public class LiteraryWorkEntity extends EditorialEntity {

    @Column(name = "title_original", nullable = false, length = 200)
    private String titleOriginal;

    @Column(name = "title_normalized", nullable = false, length = 200)
    private String titleNormalized;

    @Column(nullable = false, length = 220)
    private String slug;

    @Column(length = 4000)
    private String description;

    @Column(name = "language_code", nullable = false, length = 16)
    private String languageCode;

    @Column(name = "genre_id")
    private UUID genreId;

    @Column(name = "era_id")
    private UUID eraId;

    @Column(name = "composition_display", length = 200)
    private String compositionDisplay;

    @Enumerated(EnumType.STRING)
    @Column(name = "rights_status", nullable = false, length = 20)
    private WorkRights rightsStatus;

    @Column(name = "rights_note", length = 1000)
    private String rightsNote;

    @Column(length = 500)
    private String attribution;

    public String getTitleOriginal() { return titleOriginal; }
    public void setTitleOriginal(String titleOriginal) { this.titleOriginal = titleOriginal; }
    public String getTitleNormalized() { return titleNormalized; }
    public void setTitleNormalized(String titleNormalized) { this.titleNormalized = titleNormalized; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLanguageCode() { return languageCode; }
    public void setLanguageCode(String languageCode) { this.languageCode = languageCode; }
    public UUID getGenreId() { return genreId; }
    public void setGenreId(UUID genreId) { this.genreId = genreId; }
    public UUID getEraId() { return eraId; }
    public void setEraId(UUID eraId) { this.eraId = eraId; }
    public String getCompositionDisplay() { return compositionDisplay; }
    public void setCompositionDisplay(String compositionDisplay) { this.compositionDisplay = compositionDisplay; }
    public WorkRights getRightsStatus() { return rightsStatus; }
    public void setRightsStatus(WorkRights rightsStatus) { this.rightsStatus = rightsStatus; }
    public String getRightsNote() { return rightsNote; }
    public void setRightsNote(String rightsNote) { this.rightsNote = rightsNote; }
    public String getAttribution() { return attribution; }
    public void setAttribution(String attribution) { this.attribution = attribution; }
}
