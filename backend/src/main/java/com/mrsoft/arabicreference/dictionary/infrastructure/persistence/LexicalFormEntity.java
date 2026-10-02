package com.mrsoft.arabicreference.dictionary.infrastructure.persistence;

import com.mrsoft.arabicreference.dictionary.domain.FormType;
import com.mrsoft.arabicreference.linguistics.domain.editorial.PublicationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "lexical_form")
public class LexicalFormEntity {

    @Id
    private UUID id;

    @Column(name = "lexical_entry_id", nullable = false)
    private UUID lexicalEntryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "form_type", nullable = false, length = 32)
    private FormType formType;

    @Column(name = "original_form", nullable = false, length = 80)
    private String originalForm;

    @Column(name = "normalized_form", nullable = false, length = 80)
    private String normalizedForm;

    @Column(length = 300)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PublicationStatus status;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

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
    public UUID getLexicalEntryId() { return lexicalEntryId; }
    public void setLexicalEntryId(UUID lexicalEntryId) { this.lexicalEntryId = lexicalEntryId; }
    public FormType getFormType() { return formType; }
    public void setFormType(FormType formType) { this.formType = formType; }
    public String getOriginalForm() { return originalForm; }
    public void setOriginalForm(String originalForm) { this.originalForm = originalForm; }
    public String getNormalizedForm() { return normalizedForm; }
    public void setNormalizedForm(String normalizedForm) { this.normalizedForm = normalizedForm; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public PublicationStatus getStatus() { return status; }
    public void setStatus(PublicationStatus status) { this.status = status; }
    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
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
