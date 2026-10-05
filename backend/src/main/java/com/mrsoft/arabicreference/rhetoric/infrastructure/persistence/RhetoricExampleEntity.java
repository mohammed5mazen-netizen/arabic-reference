package com.mrsoft.arabicreference.rhetoric.infrastructure.persistence;

import com.mrsoft.arabicreference.rhetoric.domain.RhetoricExampleKind;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "rhetoric_example")
public class RhetoricExampleEntity {
    @Id
    private UUID id;
    @Column(name = "device_id", nullable = false)
    private UUID deviceId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RhetoricExampleKind kind;
    @Column(name = "example_text", nullable = false, length = 2000)
    private String exampleText;
    @Column(nullable = false, length = 2000)
    private String explanation;
    @Column(name = "highlighted_segment", length = 300)
    private String highlightedSegment;
    @Column(length = 2000)
    private String interpretation;
    @Column(name = "scholarly_note", length = 2000)
    private String scholarlyNote;
    @Column(name = "alternative_interpretation", length = 2000)
    private String alternativeInterpretation;
    @Column(name = "citation_id")
    private UUID citationId;
    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getDeviceId() { return deviceId; }
    public void setDeviceId(UUID deviceId) { this.deviceId = deviceId; }
    public RhetoricExampleKind getKind() { return kind; }
    public void setKind(RhetoricExampleKind kind) { this.kind = kind; }
    public String getExampleText() { return exampleText; }
    public void setExampleText(String exampleText) { this.exampleText = exampleText; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public String getHighlightedSegment() { return highlightedSegment; }
    public void setHighlightedSegment(String highlightedSegment) { this.highlightedSegment = highlightedSegment; }
    public String getInterpretation() { return interpretation; }
    public void setInterpretation(String interpretation) { this.interpretation = interpretation; }
    public String getScholarlyNote() { return scholarlyNote; }
    public void setScholarlyNote(String scholarlyNote) { this.scholarlyNote = scholarlyNote; }
    public String getAlternativeInterpretation() { return alternativeInterpretation; }
    public void setAlternativeInterpretation(String alternativeInterpretation) { this.alternativeInterpretation = alternativeInterpretation; }
    public UUID getCitationId() { return citationId; }
    public void setCitationId(UUID citationId) { this.citationId = citationId; }
    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
}
