package com.example.demo.dto.response;

import java.util.List;

public class EvaluationRoundSummaryResponse {
    private Integer roundId;
    private String roundName;
    private EvaluationSummaryHeaderResponse header;
    private EvaluationScoreSummaryResponse scoreSummary;
    private List<EvaluationScoreDistributionResponse> distribution;
    private List<EvaluationRankingRowResponse> ranking;

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

    public EvaluationSummaryHeaderResponse getHeader() {
        return header;
    }

    public void setHeader(EvaluationSummaryHeaderResponse header) {
        this.header = header;
    }

    public EvaluationScoreSummaryResponse getScoreSummary() {
        return scoreSummary;
    }

    public void setScoreSummary(EvaluationScoreSummaryResponse scoreSummary) {
        this.scoreSummary = scoreSummary;
    }

    public List<EvaluationScoreDistributionResponse> getDistribution() {
        return distribution;
    }

    public void setDistribution(List<EvaluationScoreDistributionResponse> distribution) {
        this.distribution = distribution;
    }

    public List<EvaluationRankingRowResponse> getRanking() {
        return ranking;
    }

    public void setRanking(List<EvaluationRankingRowResponse> ranking) {
        this.ranking = ranking;
    }
}
