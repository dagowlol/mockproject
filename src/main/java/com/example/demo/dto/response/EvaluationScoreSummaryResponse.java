package com.example.demo.dto.response;

public class EvaluationScoreSummaryResponse {
    private Double averageScore;
    private Integer highestScore;
    private Integer lowestScore;
    private Integer evaluatedCount;

    public Double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(Double averageScore) {
        this.averageScore = averageScore;
    }

    public Integer getHighestScore() {
        return highestScore;
    }

    public void setHighestScore(Integer highestScore) {
        this.highestScore = highestScore;
    }

    public Integer getLowestScore() {
        return lowestScore;
    }

    public void setLowestScore(Integer lowestScore) {
        this.lowestScore = lowestScore;
    }

    public Integer getEvaluatedCount() {
        return evaluatedCount;
    }

    public void setEvaluatedCount(Integer evaluatedCount) {
        this.evaluatedCount = evaluatedCount;
    }
}
