package com.example.demo.dto.response;

import java.time.LocalDate;
import java.util.List;

public class MemberEvaluationResultResponse {
    private Integer roundId;
    private String roundName;
    private LocalDate periodFrom;
    private LocalDate periodTo;
    private Integer totalScore;
    private String evaluatorName;
    private LocalDate evaluationDate;
    private String note;
    private String rating;
    private List<EvaluationCriterionScoreResponse> criteriaScores;

    public Integer getRoundId() {
        return roundId; 
    }

    public void setRoundId(Integer roundId) {
        this.roundId = roundId;
    }

    public String getRoundName() {
        return roundName;
    }

    public void setRoundName(String roundName) {
        this.roundName = roundName;
    }

    public LocalDate getPeriodTo() {
        return periodTo;
    }

    public void setPeriodTo(LocalDate periodTo) {
        this.periodTo = periodTo;
    }

    public LocalDate getPeriodFrom() {
        return periodFrom;
    }

    public void setPeriodFrom(LocalDate periodFrom) {
        this.periodFrom = periodFrom;
    }

    public String getEvaluatorName() {
        return evaluatorName;
    }

    public void setEvaluatorName(String evaluatorName) {
        this.evaluatorName = evaluatorName;
    }

    public LocalDate getEvaluationDate() {
        return evaluationDate;
    }

    public void setEvaluationDate(LocalDate evaluationDate) {
        this.evaluationDate = evaluationDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public List<EvaluationCriterionScoreResponse> getCriteriaScores() {
        return criteriaScores;
    }

    public void setCriteriaScores(List<EvaluationCriterionScoreResponse> criteriaScores) {
        this.criteriaScores = criteriaScores;
    }
}
