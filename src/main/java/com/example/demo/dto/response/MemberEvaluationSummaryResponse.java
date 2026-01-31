package com.example.demo.dto.response;

import java.util.List;

public class MemberEvaluationSummaryResponse {
    private List<MemberEvaluationResultResponse> history;
    private MemberEvaluationResultResponse latest;
    private Double averageScore;
    private Integer evaluationCount;

    public List<MemberEvaluationResultResponse> getHistory() {
        return history;
    }

    public void setHistory(List<MemberEvaluationResultResponse> history) {
        this.history = history;
    }

    public MemberEvaluationResultResponse getLatest() {
        return latest;
    }

    public void setLatest(MemberEvaluationResultResponse latest) {
        this.latest = latest;
    }

    public Double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(Double averageScore) {
        this.averageScore = averageScore;
    }

    public Integer getEvaluationCount() {
        return evaluationCount;
    }

    public void setEvaluationCount(Integer evaluationCount) {
        this.evaluationCount = evaluationCount;
    }
}
