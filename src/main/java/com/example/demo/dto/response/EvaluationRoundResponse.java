package com.example.demo.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.demo.model.EvaluationRoundStatus;
import com.example.demo.model.EvaluationRoundType;

public class EvaluationRoundResponse {
    private Integer id;
    private String name;
    private EvaluationRoundType type;
    private LocalDate periodFrom;
    private LocalDate periodTo;
    private Integer projectId;
    private EvaluationRoundStatus status;
    private Integer scoringScale;
    private String description;
    private Boolean isTemplateRound;
    private LocalDateTime createdAt;
    private Integer createdBy;
    private LocalDateTime updatedAt;
    private Integer updatedBy;
    private Integer totalEvaluations;
    private Integer completedEvaluations;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public EvaluationRoundStatus getStatus() {
        return status;
    }

    public void setStatus(EvaluationRoundStatus status) {
        this.status = status;
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

    public Boolean getIsTemplateRound() {
        return isTemplateRound;
    }

    public void setIsTemplateRound(Boolean isTemplateRound) {
        this.isTemplateRound = isTemplateRound;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Integer updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Integer getTotalEvaluations() {
        return totalEvaluations;
    }

    public void setTotalEvaluations(Integer totalEvaluations) {
        this.totalEvaluations = totalEvaluations;
    }

    public Integer getCompletedEvaluations() {
        return completedEvaluations;
    }

    public void setCompletedEvaluations(Integer completedEvaluations) {
        this.completedEvaluations = completedEvaluations;
    }
}
