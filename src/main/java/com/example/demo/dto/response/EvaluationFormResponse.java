package com.example.demo.dto.response;

import java.util.List;

public class EvaluationFormResponse {
    private Integer assignmentId;
    private Integer formId;
    private String status;
    private Integer totalScore;
    private String comment;
    private List<EvaluationFormCriterionResponse> criteria;

    public Integer getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Integer assignmentId) {
        this.assignmentId = assignmentId;
    }

    public Integer getFormId() {
        return formId;
    }

    public void setFormId(Integer formId) {
        this.formId = formId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<EvaluationFormCriterionResponse> getCriteria() {
        return criteria;
    }

    public void setCriteria(List<EvaluationFormCriterionResponse> criteria) {
        this.criteria = criteria;
    }
}
