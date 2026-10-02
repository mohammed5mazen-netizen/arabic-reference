package com.mrsoft.arabicreference.dictionary.infrastructure.persistence;

import com.mrsoft.arabicreference.dictionary.domain.GrammaticalGender;
import com.mrsoft.arabicreference.dictionary.domain.PartOfSpeech;
import com.mrsoft.arabicreference.linguistics.domain.editorial.PublicationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "lexical_entry")
public class LexicalEntryEntity {

    @Id
    private UUID id;

    @Column(name = "lemma_original", nullable = false, length = 80)
    private String lemmaOriginal;

    @Column(name = "lemma_normalized", nullable = false, length = 80)
    private String lemmaNormalized;

    @Column(name = "vocalized_form", length = 80)
    private String vocalizedForm;

    @Column(name = "root_id")
    private UUID rootId;

    @Enumerated(EnumType.STRING)
    @Column(name = "part_of_speech", nullable = false, length = 32)
    private PartOfSpeech partOfSpeech;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private GrammaticalGender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PublicationStatus status;

    @Column(nullable = false, length = 120)
    private String slug;

    @Column(name = "published_lemma_normalized", length = 80)
    private String publishedLemmaNormalized;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "published_snapshot", columnDefinition = "jsonb")
    private Map<String, Object> publishedSnapshot;

    @Column(name = "reviewed_by")
    private UUID reviewedBy;

    @Column(name = "change_reason", length = 500)
    private String changeReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "updated_by", nullable = false)
    private UUID updatedBy;

    @Version
    @Column(nullable = false)
    private long version;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getLemmaOriginal() { return lemmaOriginal; }
    public void setLemmaOriginal(String lemmaOriginal) { this.lemmaOriginal = lemmaOriginal; }
    public String getLemmaNormalized() { return lemmaNormalized; }
    public void setLemmaNormalized(String lemmaNormalized) { this.lemmaNormalized = lemmaNormalized; }
    public String getVocalizedForm() { return vocalizedForm; }
    public void setVocalizedForm(String vocalizedForm) { this.vocalizedForm = vocalizedForm; }
    public UUID getRootId() { return rootId; }
    public void setRootId(UUID rootId) { this.rootId = rootId; }
    public PartOfSpeech getPartOfSpeech() { return partOfSpeech; }
    public void setPartOfSpeech(PartOfSpeech partOfSpeech) { this.partOfSpeech = partOfSpeech; }
    public GrammaticalGender getGender() { return gender; }
    public void setGender(GrammaticalGender gender) { this.gender = gender; }
    public PublicationStatus getStatus() { return status; }
    public void setStatus(PublicationStatus status) { this.status = status; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getPublishedLemmaNormalized() { return publishedLemmaNormalized; }
    public void setPublishedLemmaNormalized(String publishedLemmaNormalized) { this.publishedLemmaNormalized = publishedLemmaNormalized; }
    public Map<String, Object> getPublishedSnapshot() { return publishedSnapshot; }
    public void setPublishedSnapshot(Map<String, Object> publishedSnapshot) { this.publishedSnapshot = publishedSnapshot; }
    public UUID getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(UUID reviewedBy) { this.reviewedBy = reviewedBy; }
    public String getChangeReason() { return changeReason; }
    public void setChangeReason(String changeReason) { this.changeReason = changeReason; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
    public UUID getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(UUID updatedBy) { this.updatedBy = updatedBy; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
