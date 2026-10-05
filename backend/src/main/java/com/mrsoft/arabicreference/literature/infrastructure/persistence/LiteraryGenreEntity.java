package com.mrsoft.arabicreference.literature.infrastructure.persistence;

import com.mrsoft.arabicreference.linguistics.infrastructure.persistence.EditorialEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "literary_genre")
public class LiteraryGenreEntity extends EditorialEntity {

    @Column(name = "name_original", nullable = false, length = 160)
    private String nameOriginal;

    @Column(name = "name_normalized", nullable = false, length = 160)
    private String nameNormalized;

    @Column(nullable = false, length = 180)
    private String slug;

    @Column(length = 2000)
    private String description;

    public String getNameOriginal() { return nameOriginal; }
    public void setNameOriginal(String nameOriginal) { this.nameOriginal = nameOriginal; }
    public String getNameNormalized() { return nameNormalized; }
    public void setNameNormalized(String nameNormalized) { this.nameNormalized = nameNormalized; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
