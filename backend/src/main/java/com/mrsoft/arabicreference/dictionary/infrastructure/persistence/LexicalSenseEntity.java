package com.mrsoft.arabicreference.dictionary.infrastructure.persistence;

import com.mrsoft.arabicreference.dictionary.domain.SemanticDomain;
import com.mrsoft.arabicreference.dictionary.domain.UsageLabel;
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
@Table(name = "lexical_sense")
public class LexicalSenseEntity {

    @Id
    private UUID id;

    @Column(name = "lexical_entry_id", nullable = false)
    private UUID lexicalEntryId;

    @Column(nullable = false, length = 4000)
    private String definition;

    @Column(name = "short_definition", length = 280)
    private String shortDefinition;

    @Enumerated(EnumType.STRING)
    @Column(name = "usage_label", length = 32)
    private UsageLabel usageLabel;

    @Enumerated(EnumType.STRING)
    @Column(name = "domain_label", length = 32)
    private SemanticDomain domainLabel;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PublicationStatus status;

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
    public String getDefinition() { return definition; }
    public void setDefinition(String definition) { this.definition = definition; }
    public String getShortDefinition() { return shortDefinition; }
    public void setShortDefinition(String shortDefinition) { this.shortDefinition = shortDefinition; }
    public UsageLabel getUsageLabel() { return usageLabel; }
    public void setUsageLabel(UsageLabel usageLabel) { this.usageLabel = usageLabel; }
    public SemanticDomain getDomainLabel() { return domainLabel; }
    public void setDomainLabel(SemanticDomain domainLabel) { this.domainLabel = domainLabel; }
    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
    public PublicationStatus getStatus() { return status; }
    public void setStatus(PublicationStatus status) { this.status = status; }
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
