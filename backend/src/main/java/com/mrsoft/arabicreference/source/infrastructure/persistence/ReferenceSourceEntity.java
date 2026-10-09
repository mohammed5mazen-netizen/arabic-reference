package com.mrsoft.arabicreference.source.infrastructure.persistence;

import com.mrsoft.arabicreference.linguistics.domain.editorial.PublicationStatus;
import com.mrsoft.arabicreference.source.domain.LicenseType;
import com.mrsoft.arabicreference.source.domain.SourceType;
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
@Table(name = "reference_source")
public class ReferenceSourceEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 32)
    private SourceType sourceType;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(length = 200)
    private String author;

    @Column(length = 200)
    private String publisher;

    @Column(length = 80)
    private String edition;

    @Column(name = "publication_year")
    private Integer publicationYear;

    @Column(length = 32)
    private String isbn;

    @Column(length = 500)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(name = "license_type", nullable = false, length = 32)
    private LicenseType licenseType;

    @Column(name = "public_domain", nullable = false)
    private boolean publicDomain;

    @Column(name = "attribution_text", nullable = false, length = 500)
    private String attributionText;

    @Column(length = 1000)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PublicationStatus status;

    @Column(nullable = false, length = 160)
    private String slug;

    @Column(name = "identity_key", nullable = false, length = 700)
    private String identityKey;

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
    public SourceType getSourceType() { return sourceType; }
    public void setSourceType(SourceType sourceType) { this.sourceType = sourceType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public String getEdition() { return edition; }
    public void setEdition(String edition) { this.edition = edition; }
    public Integer getPublicationYear() { return publicationYear; }
    public void setPublicationYear(Integer publicationYear) { this.publicationYear = publicationYear; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public LicenseType getLicenseType() { return licenseType; }
    public void setLicenseType(LicenseType licenseType) { this.licenseType = licenseType; }
    public boolean isPublicDomain() { return publicDomain; }
    public void setPublicDomain(boolean publicDomain) { this.publicDomain = publicDomain; }
    public String getAttributionText() { return attributionText; }
    public void setAttributionText(String attributionText) { this.attributionText = attributionText; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public PublicationStatus getStatus() { return status; }
    public void setStatus(PublicationStatus status) { this.status = status; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getIdentityKey() { return identityKey; }
    public void setIdentityKey(String identityKey) { this.identityKey = identityKey; }
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
