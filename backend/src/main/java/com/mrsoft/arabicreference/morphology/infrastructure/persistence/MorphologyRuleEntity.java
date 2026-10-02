package com.mrsoft.arabicreference.morphology.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "morphology_rule")
public class MorphologyRuleEntity {

    @Id
    @Column(length = 40)
    private String code;

    @Column(name = "explanation_code", nullable = false, length = 80)
    private String explanationCode;

    @Column(nullable = false, length = 300)
    private String description;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "rule_set_version", nullable = false, length = 40)
    private String ruleSetVersion;

    @Version
    @Column(nullable = false)
    private long version;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getExplanationCode() { return explanationCode; }
    public void setExplanationCode(String explanationCode) { this.explanationCode = explanationCode; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getRuleSetVersion() { return ruleSetVersion; }
    public void setRuleSetVersion(String ruleSetVersion) { this.ruleSetVersion = ruleSetVersion; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
