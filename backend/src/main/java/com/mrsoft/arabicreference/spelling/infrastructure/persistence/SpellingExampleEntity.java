package com.mrsoft.arabicreference.spelling.infrastructure.persistence;

import com.mrsoft.arabicreference.spelling.domain.SpellingExampleKind;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "spelling_example")
public class SpellingExampleEntity {

    @Id
    private UUID id;

    @Column(name = "rule_id", nullable = false)
    private UUID ruleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SpellingExampleKind kind;

    @Column(name = "correct_form", length = 300)
    private String correctForm;

    @Column(name = "incorrect_form", length = 300)
    private String incorrectForm;

    @Column(length = 1000)
    private String explanation;

    @Column(name = "context_note", length = 500)
    private String contextNote;

    @Column(name = "common_form", length = 300)
    private String commonForm;

    @Column(length = 1000)
    private String reason;

    @Column(name = "citation_id")
    private UUID citationId;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getRuleId() { return ruleId; }
    public void setRuleId(UUID ruleId) { this.ruleId = ruleId; }
    public SpellingExampleKind getKind() { return kind; }
    public void setKind(SpellingExampleKind kind) { this.kind = kind; }
    public String getCorrectForm() { return correctForm; }
    public void setCorrectForm(String correctForm) { this.correctForm = correctForm; }
    public String getIncorrectForm() { return incorrectForm; }
    public void setIncorrectForm(String incorrectForm) { this.incorrectForm = incorrectForm; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public String getContextNote() { return contextNote; }
    public void setContextNote(String contextNote) { this.contextNote = contextNote; }
    public String getCommonForm() { return commonForm; }
    public void setCommonForm(String commonForm) { this.commonForm = commonForm; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public UUID getCitationId() { return citationId; }
    public void setCitationId(UUID citationId) { this.citationId = citationId; }
    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
}
