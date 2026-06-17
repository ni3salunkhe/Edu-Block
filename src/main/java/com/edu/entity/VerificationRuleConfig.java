package com.edu.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "verification_rule_configs")
public class VerificationRuleConfig {

    @Id
    @Column(length = 100)
    private String ruleName;

    @Column(nullable = false)
    private Integer scoreModifier;

    @Column(nullable = false)
    private Boolean enabled;

    public VerificationRuleConfig() {}

    public VerificationRuleConfig(String ruleName, Integer scoreModifier, Boolean enabled) {
        this.ruleName = ruleName;
        this.scoreModifier = scoreModifier;
        this.enabled = enabled;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public Integer getScoreModifier() {
        return scoreModifier;
    }

    public void setScoreModifier(Integer scoreModifier) {
        this.scoreModifier = scoreModifier;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
