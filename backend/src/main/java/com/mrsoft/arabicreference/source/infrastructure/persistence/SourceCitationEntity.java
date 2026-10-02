package com.mrsoft.arabicreference.source.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "source_citation")
public class SourceCitationEntity {

    @Id
    private UUID id;

    @Column(name = "source_id", nullable = false)
    private UUID sourceId;

    @Column(name = "page_from")
    private Integer pageFrom;

    @Column(name = "page_to")
    private Integer pageTo;

    @Column(length = 40)
    private String volume;

    @Column(length = 120)
    private String chapter;

    @Column(name = "section_label", length = 120)
    private String sectionLabel;

    @Column(name = "entry_label", length = 120)
    private String entryLabel;

    @Column(name = "source_locator", length = 200)
    private String sourceLocator;

    @Column(name = "quoted_text", length = 2000)
    private String quotedText;

    @Column(length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getSourceId() { return sourceId; }
    public void setSourceId(UUID sourceId) { this.sourceId = sourceId; }
    public Integer getPageFrom() { return pageFrom; }
    public void setPageFrom(Integer pageFrom) { this.pageFrom = pageFrom; }
    public Integer getPageTo() { return pageTo; }
    public void setPageTo(Integer pageTo) { this.pageTo = pageTo; }
    public String getVolume() { return volume; }
    public void setVolume(String volume) { this.volume = volume; }
    public String getChapter() { return chapter; }
    public void setChapter(String chapter) { this.chapter = chapter; }
    public String getSectionLabel() { return sectionLabel; }
    public void setSectionLabel(String sectionLabel) { this.sectionLabel = sectionLabel; }
    public String getEntryLabel() { return entryLabel; }
    public void setEntryLabel(String entryLabel) { this.entryLabel = entryLabel; }
    public String getSourceLocator() { return sourceLocator; }
    public void setSourceLocator(String sourceLocator) { this.sourceLocator = sourceLocator; }
    public String getQuotedText() { return quotedText; }
    public void setQuotedText(String quotedText) { this.quotedText = quotedText; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
}
