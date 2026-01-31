package com.example.demo.dto.request;

import jakarta.validation.constraints.NotNull;

public class EvaluationScoreRequestItem {
    @NotNull
    private Integer criterionId;

    @NotNull
    private Integer scoreValue;

    private String note;

    public Integer getCriterionId() {
        return criterionId;
    }

    public void setCriterionId(Integer criterionId) {
        this.criterionId = criterionId;
    }

    public Integer getScoreValue() {
        return scoreValue;
    }

    public void setScoreValue(Integer scoreValue) {
        this.scoreValue = scoreValue;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
