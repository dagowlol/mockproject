package com.example.demo.dto.request;

import java.time.LocalDate;

import com.example.demo.model.EvaluationRoundType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class    CreateEvaluationRoundRequest {
    @NotBlank
    private String name;

    @NotNull
    private EvaluationRoundType type;

    @NotNull
    private LocalDate periodFrom;

    @NotNull
    private LocalDate periodTo;

    @NotNull
    @Min(1)
    private Integer scoringScale;

    @NotBlank
    private String description;

    private Integer projectId;

    private Integer criteriaRoundId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EvaluationRoundType getType() {
        return type;
    }

    public void setType(EvaluationRoundType type) {
        this.type = type;
    }

    public LocalDate getPeriodFrom() {
        return periodFrom;
    }

    public void setPeriodFrom(LocalDate periodFrom) {
        this.periodFrom = periodFrom;
    }

    public LocalDate getPeriodTo() {
        return periodTo;
    }

    public void setPeriodTo(LocalDate periodTo) {
        this.periodTo = periodTo;
    }

    public Integer getScoringScale() {
        return scoringScale;
    }

    public void setScoringScale(Integer scoringScale) {
        this.scoringScale = scoringScale;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Integer getCriteriaRoundId() {
        return criteriaRoundId;
    }

    public void setCriteriaRoundId(Integer criteriaRoundId) {
        this.criteriaRoundId = criteriaRoundId;
    }
}
