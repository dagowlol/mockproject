package com.example.demo.dto.response;

import java.util.List;

public class EvaluationRankingRowResponse {
    private Integer rank;
    private Integer userId;
    private String fullName;
    private String positionTitle;
    private Integer totalScore;
    private List<EvaluationCriterionScoreResponse> criteriaScores;

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPositionTitle() {
        return positionTitle;
    }

    public void setPositionTitle(String positionTitle) {
        this.positionTitle = positionTitle;
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
